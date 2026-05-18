package hub

import (
	"log"
	"sync"

	"github.com/google/uuid"
	"github.com/gorilla/websocket"
)

// Client represents a connected WebSocket client.
type Client struct {
	UserID uuid.UUID
	Conn   *websocket.Conn
	Send   chan []byte
}

// BroadcastMessage targets a message at a specific user.
type BroadcastMessage struct {
	UserID  uuid.UUID
	Message []byte
}

// Hub manages all active WebSocket connections, keyed by userId.
// At most one connection per user is maintained.
type Hub struct {
	clients    map[uuid.UUID]*Client
	mu         sync.RWMutex
	register   chan *Client
	unregister chan *Client
	broadcast  chan *BroadcastMessage
}

// NewHub initialises a Hub ready to Run.
func NewHub() *Hub {
	return &Hub{
		clients:    make(map[uuid.UUID]*Client),
		register:   make(chan *Client),
		unregister: make(chan *Client),
		broadcast:  make(chan *BroadcastMessage, 256),
	}
}

// Run processes registration, unregistration and outbound messages.
// It must be called in its own goroutine.
func (h *Hub) Run() {
	for {
		select {
		case client := <-h.register:
			h.mu.Lock()
			// Evict any pre-existing connection for the same user
			if existing, ok := h.clients[client.UserID]; ok {
				log.Printf("hub: replacing existing connection for user %s", client.UserID)
				close(existing.Send)
				existing.Conn.Close()
			}
			h.clients[client.UserID] = client
			h.mu.Unlock()
			log.Printf("hub: registered user %s (total=%d)", client.UserID, len(h.clients))

		case client := <-h.unregister:
			h.mu.Lock()
			if existing, ok := h.clients[client.UserID]; ok && existing == client {
				delete(h.clients, client.UserID)
				close(client.Send)
			}
			h.mu.Unlock()
			log.Printf("hub: unregistered user %s (total=%d)", client.UserID, len(h.clients))

		case msg := <-h.broadcast:
			h.mu.RLock()
			client, ok := h.clients[msg.UserID]
			h.mu.RUnlock()

			if ok {
				select {
				case client.Send <- msg.Message:
				default:
					// Slow consumer — drop the connection
					h.mu.Lock()
					delete(h.clients, msg.UserID)
					close(client.Send)
					h.mu.Unlock()
					log.Printf("hub: dropped slow consumer for user %s", msg.UserID)
				}
			}
		}
	}
}

// Register adds a client to the hub.
func (h *Hub) Register(client *Client) {
	h.register <- client
}

// Unregister removes a client from the hub.
func (h *Hub) Unregister(client *Client) {
	h.unregister <- client
}

// SendToUser queues a message for delivery to a specific user.
// If the user is not connected the message is silently dropped.
func (h *Hub) SendToUser(userID uuid.UUID, message []byte) {
	h.broadcast <- &BroadcastMessage{UserID: userID, Message: message}
}
