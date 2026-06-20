package handlers

import (
	"database/sql"
	"net/http"
	"regexp"
	"strings"

	"bug-report-service/internal/auth"
	"bug-report-service/internal/config"
	"bug-report-service/internal/metrics"
)

// Handler bundles all HTTP handler dependencies.
type Handler struct {
	db  *sql.DB
	cfg *config.Config
}

// New constructs a Handler.
func New(db *sql.DB, cfg *config.Config) *Handler {
	return &Handler{db: db, cfg: cfg}
}

// Register wires all routes onto mux.
// Go 1.22 method+path patterns are used (e.g. "GET /foo").
func (h *Handler) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /health", metrics.Wrap(withCORS(h.cfg, h.health), metrics.ExtractPath("GET /health")))

	// User-facing: submit a new bug report (requires valid JWT)
	mux.HandleFunc("POST /bug-reports", metrics.Wrap(withCORS(h.cfg, h.createBugReport), metrics.ExtractPath("POST /bug-reports")))

	// Admin: list all reports, optionally filtered by ?status=OPEN|CLOSED
	mux.HandleFunc("GET /bug-reports", metrics.Wrap(withCORS(h.cfg, h.listBugReports), metrics.ExtractPath("GET /bug-reports")))

	// Admin: close (archive) a report
	mux.HandleFunc("PUT /bug-reports/{id}/close", metrics.Wrap(withCORS(h.cfg, h.closeBugReport), metrics.ExtractPath("PUT /bug-reports/{id}/close")))

	// Handle OPTIONS pre-flight for all paths
	mux.HandleFunc("OPTIONS /", withCORS(h.cfg, func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusNoContent)
	}))
}

// health returns a simple liveness response.
func (h *Handler) health(w http.ResponseWriter, _ *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	w.Write([]byte(`{"status":"ok"}`))
}

// requireAuth validates the Bearer token from the Authorization header.
// Returns true if valid; writes 401 and returns false otherwise.
func (h *Handler) requireAuth(w http.ResponseWriter, r *http.Request) bool {
	token := r.Header.Get("Authorization")
	if len(token) > 7 && token[:7] == "Bearer " {
		token = token[7:]
	}
	if err := auth.ValidateToken(token, h.cfg.JWTSecret); err != nil {
		http.Error(w, "unauthorized", http.StatusUnauthorized)
		return false
	}
	return true
}

// withCORS wraps a handler to add CORS headers on every response.
// AllowedOrigins is a comma-separated list of exact origins or regex patterns.
func withCORS(cfg *config.Config, next http.HandlerFunc) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		origin := r.Header.Get("Origin")
		if origin != "" {
			for _, pattern := range strings.Split(cfg.AllowedOrigins, ",") {
				pattern = strings.TrimSpace(pattern)
				matched, err := regexp.MatchString("^"+pattern+"$", origin)
				if err == nil && matched {
					w.Header().Set("Access-Control-Allow-Origin", origin)
					break
				}
			}
		}
		w.Header().Set("Access-Control-Allow-Methods", "GET, POST, PATCH, OPTIONS")
		w.Header().Set("Access-Control-Allow-Headers", "Authorization, Content-Type")
		if r.Method == http.MethodOptions {
			w.WriteHeader(http.StatusNoContent)
			return
		}
		next(w, r)
	}
}
