package hub

import (
	"testing"
	"time"

	"github.com/google/uuid"
)

func TestHubDeliversMessagesToRegisteredUser(t *testing.T) {
	h := NewHub()
	go h.Run()

	userID := uuid.New()
	client := &Client{
		UserID: userID,
		Send:   make(chan []byte, 1),
	}
	h.Register(client)

	want := []byte(`{"type":"NOTIFICATION"}`)
	h.SendToUser(userID, want)

	select {
	case got := <-client.Send:
		if string(got) != string(want) {
			t.Fatalf("expected %s, got %s", want, got)
		}
	case <-time.After(time.Second):
		t.Fatal("timed out waiting for hub delivery")
	}

	h.Unregister(client)
	select {
	case _, ok := <-client.Send:
		if ok {
			t.Fatal("expected send channel to be closed after unregister")
		}
	case <-time.After(time.Second):
		t.Fatal("timed out waiting for unregister")
	}
}

func TestHubReplacesExistingConnectionForUser(t *testing.T) {
	h := NewHub()
	go h.Run()

	userID := uuid.New()
	first := &Client{
		UserID: userID,
		Send:   make(chan []byte, 1),
	}
	second := &Client{
		UserID: userID,
		Send:   make(chan []byte, 1),
	}

	h.Register(first)
	h.Register(second)

	select {
	case _, ok := <-first.Send:
		if ok {
			t.Fatal("expected first connection channel to close when replaced")
		}
	case <-time.After(time.Second):
		t.Fatal("timed out waiting for first connection replacement")
	}

	want := []byte("latest")
	h.SendToUser(userID, want)

	select {
	case got := <-second.Send:
		if string(got) != string(want) {
			t.Fatalf("expected %s, got %s", want, got)
		}
	case <-time.After(time.Second):
		t.Fatal("timed out waiting for delivery to replacement connection")
	}
}
