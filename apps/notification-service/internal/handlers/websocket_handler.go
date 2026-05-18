package handlers

import (
	"log"
	"net/http"
	"time"

	"github.com/gorilla/websocket"
	"notification-service/internal/auth"
	"notification-service/internal/hub"
)

var upgrader = websocket.Upgrader{
	ReadBufferSize:  1024,
	WriteBufferSize: 1024,
	// Origin validation is handled by JWT — any origin is allowed at the protocol level
	CheckOrigin: func(_ *http.Request) bool { return true },
}

func (h *Handler) webSocketUpgrade(w http.ResponseWriter, r *http.Request) {
	// Browsers cannot set custom headers during WebSocket handshakes,
	// so the JWT is passed as a query parameter instead.
	token := r.URL.Query().Get("token")
	if token == "" {
		// Accept Authorization header for non-browser clients
		bearer := r.Header.Get("Authorization")
		if len(bearer) > 7 && bearer[:7] == "Bearer " {
			token = bearer[7:]
		}
	}

	if token == "" {
		http.Error(w, "missing token", http.StatusUnauthorized)
		return
	}

	claims, err := auth.ValidateToken(token, h.cfg.JWTSecret)
	if err != nil {
		http.Error(w, "invalid token", http.StatusUnauthorized)
		return
	}

	conn, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		log.Printf("ws: upgrade failed for user %s: %v", claims.UserID, err)
		return
	}

	client := &hub.Client{
		UserID: claims.UserID,
		Conn:   conn,
		Send:   make(chan []byte, 256),
	}

	h.hub.Register(client)

	// Write pump — forwards hub messages to the browser
	go func() {
		defer func() {
			h.hub.Unregister(client)
			conn.Close()
		}()

		const writeWait = 10 * time.Second

		for {
			msg, ok := <-client.Send
			conn.SetWriteDeadline(time.Now().Add(writeWait))
			if !ok {
				conn.WriteMessage(websocket.CloseMessage, []byte{})
				return
			}
			if err := conn.WriteMessage(websocket.TextMessage, msg); err != nil {
				log.Printf("ws: write error for user %s: %v", claims.UserID, err)
				return
			}
		}
	}()

	// Read pump — keeps the connection alive and handles client-sent pings
	const (
		pongWait   = 60 * time.Second
		pingPeriod = 54 * time.Second
	)

	conn.SetReadLimit(512)
	conn.SetReadDeadline(time.Now().Add(pongWait))
	conn.SetPongHandler(func(string) error {
		conn.SetReadDeadline(time.Now().Add(pongWait))
		return nil
	})

	ticker := time.NewTicker(pingPeriod)
	defer ticker.Stop()

	go func() {
		for range ticker.C {
			conn.SetWriteDeadline(time.Now().Add(10 * time.Second))
			if err := conn.WriteMessage(websocket.PingMessage, nil); err != nil {
				return
			}
		}
	}()

	for {
		if _, _, err := conn.ReadMessage(); err != nil {
			break
		}
	}

	h.hub.Unregister(client)
}
