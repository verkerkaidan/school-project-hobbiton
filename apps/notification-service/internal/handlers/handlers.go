package handlers

import (
	"database/sql"
	"net/http"

	"notification-service/internal/auth"
	"notification-service/internal/config"
	"notification-service/internal/hub"
)

// Handler bundles all HTTP handler dependencies.
type Handler struct {
	db  *sql.DB
	hub *hub.Hub
	cfg *config.Config
}

// New constructs a Handler.
func New(db *sql.DB, h *hub.Hub, cfg *config.Config) *Handler {
	return &Handler{db: db, hub: h, cfg: cfg}
}

// Register wires all routes onto mux.
// Note: Go 1.22 method+path patterns are used (e.g. "GET /foo").
func (h *Handler) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /health", withCORS(h.cfg, h.health))

	// WebSocket upgrade — JWT via ?token= query param
	mux.HandleFunc("GET /ws", withCORS(h.cfg, h.webSocketUpgrade))

	// Notification CRUD — JWT via Authorization header
	mux.HandleFunc("POST /notifications", withCORS(h.cfg, h.createNotification))
	mux.HandleFunc("GET /notifications", withCORS(h.cfg, h.listNotifications))
	mux.HandleFunc("PATCH /notifications/{id}/read", withCORS(h.cfg, h.markAsRead))
	mux.HandleFunc("POST /notifications/read-all", withCORS(h.cfg, h.markAllRead))
	mux.HandleFunc("GET /notifications/unread-count", withCORS(h.cfg, h.unreadCount))

	// Notification preferences
	mux.HandleFunc("GET /notification-preferences", withCORS(h.cfg, h.getPreferences))
	mux.HandleFunc("PUT /notification-preferences/{type}", withCORS(h.cfg, h.updatePreference))

	// Handle OPTIONS pre-flight for all paths
	mux.HandleFunc("OPTIONS /", withCORS(h.cfg, func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusNoContent)
	}))
}

// withCORS wraps a handler to add CORS headers on every response.
func withCORS(cfg *config.Config, next http.HandlerFunc) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Access-Control-Allow-Origin", cfg.AllowedOrigins)
		w.Header().Set("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS")
		w.Header().Set("Access-Control-Allow-Headers", "Authorization, Content-Type")
		if r.Method == http.MethodOptions {
			w.WriteHeader(http.StatusNoContent)
			return
		}
		next(w, r)
	}
}

func (h *Handler) health(w http.ResponseWriter, _ *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	w.Write([]byte(`{"status":"ok"}`))
}

// extractClaims reads and validates the Bearer token from the Authorization header.
func (h *Handler) extractClaims(r *http.Request) (*auth.Claims, error) {
	token := r.Header.Get("Authorization")
	if len(token) > 7 && token[:7] == "Bearer " {
		token = token[7:]
	}
	return auth.ValidateToken(token, h.cfg.JWTSecret)
}
