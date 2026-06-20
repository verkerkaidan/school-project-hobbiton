package handlers

import (
	"database/sql"
	"net/http"
	"net/url"
	"strings"

	"notification-service/internal/auth"
	"notification-service/internal/config"
	"notification-service/internal/hub"
	"notification-service/internal/metrics"
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
	mux.HandleFunc("GET /health", metrics.Wrap(withCORS(h.cfg, h.health), metrics.ExtractPath("GET /health")))

	// WebSocket upgrade — JWT via ?token= query param
	mux.HandleFunc("GET /ws", metrics.Wrap(withCORS(h.cfg, h.webSocketUpgrade), metrics.ExtractPath("GET /ws")))

	// Notification CRUD — JWT via Authorization header
	mux.HandleFunc("POST /notifications", metrics.Wrap(withCORS(h.cfg, h.createNotification), metrics.ExtractPath("POST /notifications")))
	mux.HandleFunc("POST /notifications/batch", metrics.Wrap(withCORS(h.cfg, h.batchCreateNotifications), metrics.ExtractPath("POST /notifications/batch")))
	mux.HandleFunc("DELETE /notifications/users/me", metrics.Wrap(withCORS(h.cfg, h.deleteUserNotifications), metrics.ExtractPath("DELETE /notifications/users/me")))
	mux.HandleFunc("DELETE /notifications/users/{userId}", metrics.Wrap(withCORS(h.cfg, h.adminDeleteUserNotifications), metrics.ExtractPath("DELETE /notifications/users/{userId}")))
	mux.HandleFunc("GET /notifications", metrics.Wrap(withCORS(h.cfg, h.listNotifications), metrics.ExtractPath("GET /notifications")))
	mux.HandleFunc("PATCH /notifications/{id}/read", metrics.Wrap(withCORS(h.cfg, h.markAsRead), metrics.ExtractPath("PATCH /notifications/{id}/read")))
	mux.HandleFunc("POST /notifications/read-all", metrics.Wrap(withCORS(h.cfg, h.markAllRead), metrics.ExtractPath("POST /notifications/read-all")))
	mux.HandleFunc("GET /notifications/unread-count", metrics.Wrap(withCORS(h.cfg, h.unreadCount), metrics.ExtractPath("GET /notifications/unread-count")))

	// Notification preferences
	mux.HandleFunc("GET /notification-preferences", metrics.Wrap(withCORS(h.cfg, h.getPreferences), metrics.ExtractPath("GET /notification-preferences")))
	mux.HandleFunc("PUT /notification-preferences/{type}", metrics.Wrap(withCORS(h.cfg, h.updatePreference), metrics.ExtractPath("PUT /notification-preferences/{type}")))

	// Per-user post subscription toggle — JWT via Authorization header
	mux.HandleFunc("GET /user-post-subscriptions/{authorId}", metrics.Wrap(withCORS(h.cfg, h.getPostSubscription), metrics.ExtractPath("GET /user-post-subscriptions/{authorId}")))
	mux.HandleFunc("PUT /user-post-subscriptions/{authorId}", metrics.Wrap(withCORS(h.cfg, h.togglePostSubscription), metrics.ExtractPath("PUT /user-post-subscriptions/{authorId}")))

	// Handle OPTIONS pre-flight for all paths
	mux.HandleFunc("OPTIONS /", withCORS(h.cfg, func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusNoContent)
	}))
}

// withCORS wraps a handler to add CORS headers on every response.
// AllowedOrigins is a comma-separated list of exact origins.
func withCORS(cfg *config.Config, next http.HandlerFunc) http.HandlerFunc {
	allowedOrigins := map[string]struct{}{}
	for _, raw := range strings.Split(cfg.AllowedOrigins, ",") {
		normalized, ok := normalizeOrigin(raw)
		if ok {
			allowedOrigins[normalized] = struct{}{}
		}
	}

	return func(w http.ResponseWriter, r *http.Request) {
		if origin, ok := normalizeOrigin(r.Header.Get("Origin")); ok {
			if _, exists := allowedOrigins[origin]; exists {
				w.Header().Set("Access-Control-Allow-Origin", origin)
				w.Header().Set("Vary", "Origin")
			}
		}
		w.Header().Set("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS")
		w.Header().Set("Access-Control-Allow-Headers", "Authorization, Content-Type")
		if r.Method == http.MethodOptions {
			w.WriteHeader(http.StatusNoContent)
			return
		}
		next(w, r)
	}
}

func normalizeOrigin(origin string) (string, bool) {
	origin = strings.TrimSpace(strings.TrimSuffix(origin, "/"))
	if origin == "" {
		return "", false
	}

	u, err := url.Parse(origin)
	if err != nil || u.Scheme == "" || u.Host == "" {
		return "", false
	}

	return u.Scheme + "://" + u.Host, true
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
