package logger

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net/http"
	"os"
	"sync"
	"time"
)

type lokiStream struct {
	Stream map[string]string `json:"stream"`
	Values [][]string        `json:"values"`
}

type lokiPush struct {
	Streams []lokiStream `json:"streams"`
}

type lokiWriter struct {
	url    string
	labels map[string]string
	client *http.Client
	mu     sync.Mutex
	batch  [][]string
	quit   chan struct{}
}

var defaultWriter *lokiWriter

func Init(serviceName string) {
	lokiURL := os.Getenv("LOKI_URL")
	if lokiURL == "" {
		log.Println("LOKI_URL not set, logging to stdout only")
		return
	}

	w := &lokiWriter{
		url: lokiURL + "/loki/api/v1/push",
		labels: map[string]string{
			"app": serviceName,
		},
		client: &http.Client{Timeout: 5 * time.Second},
		quit:   make(chan struct{}),
	}
	defaultWriter = w

	go w.flushLoop()

	log.SetOutput(io.MultiWriter(os.Stderr, w))
	log.Printf("Loki logging enabled for %s", serviceName)
}

func (w *lokiWriter) Write(p []byte) (n int, err error) {
	w.mu.Lock()
	entry := []string{
		fmt.Sprintf("%d", time.Now().UnixNano()),
		string(bytes.TrimSpace(p)),
	}
	w.batch = append(w.batch, entry)
	w.mu.Unlock()
	return len(p), nil
}

func (w *lokiWriter) flushLoop() {
	ticker := time.NewTicker(1 * time.Second)
	defer ticker.Stop()
	for {
		select {
		case <-ticker.C:
			w.flush()
		case <-w.quit:
			w.flush()
			return
		}
	}
}

func (w *lokiWriter) flush() {
	w.mu.Lock()
	if len(w.batch) == 0 {
		w.mu.Unlock()
		return
	}
	entries := w.batch
	w.batch = nil
	w.mu.Unlock()

	payload := lokiPush{
		Streams: []lokiStream{
			{
				Stream: w.labels,
				Values: entries,
			},
		},
	}

	body, err := json.Marshal(payload)
	if err != nil {
		fmt.Fprintf(os.Stderr, "loki: marshal error: %v\n", err)
		return
	}

	resp, err := w.client.Post(w.url, "application/json", bytes.NewReader(body))
	if err != nil {
		fmt.Fprintf(os.Stderr, "loki: push error: %v\n", err)
		return
	}
	resp.Body.Close()
}

func Shutdown() {
	if defaultWriter != nil {
		close(defaultWriter.quit)
	}
}
