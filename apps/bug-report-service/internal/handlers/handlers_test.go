package handlers

import (
	"net/http"
	"net/http/httptest"
	"testing"

	"bug-report-service/internal/config"
)

func newHandlerNoDB(t *testing.T, origins string) *Handler {
	t.Helper()
	return &Handler{cfg: &config.Config{JWTSecret: "secret", AllowedOrigins: origins}}
}

// ---------- withCORS ----------

func TestWithCORS_AllowedOrigin(t *testing.T) {
	cfg := &config.Config{AllowedOrigins: "http://localhost:3000"}
	handler := withCORS(cfg, func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
	})

	req := httptest.NewRequest(http.MethodGet, "/", nil)
	req.Header.Set("Origin", "http://localhost:3000")
	w := httptest.NewRecorder()

	handler(w, req)

	if got := w.Header().Get("Access-Control-Allow-Origin"); got != "http://localhost:3000" {
		t.Errorf("expected ACAO header to be set, got %q", got)
	}
}

func TestWithCORS_DisallowedOrigin(t *testing.T) {
	cfg := &config.Config{AllowedOrigins: "http://localhost:3000"}
	handler := withCORS(cfg, func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
	})

	req := httptest.NewRequest(http.MethodGet, "/", nil)
	req.Header.Set("Origin", "http://evil.com")
	w := httptest.NewRecorder()

	handler(w, req)

	if got := w.Header().Get("Access-Control-Allow-Origin"); got != "" {
		t.Errorf("expected no ACAO header for disallowed origin, got %q", got)
	}
}

func TestWithCORS_OptionsPreflight(t *testing.T) {
	cfg := &config.Config{AllowedOrigins: "http://localhost:3000"}
	called := false
	handler := withCORS(cfg, func(w http.ResponseWriter, r *http.Request) {
		called = true
	})

	req := httptest.NewRequest(http.MethodOptions, "/", nil)
	w := httptest.NewRecorder()

	handler(w, req)

	if w.Code != http.StatusNoContent {
		t.Errorf("expected 204 for OPTIONS, got %d", w.Code)
	}
	if called {
		t.Error("inner handler should not be called for OPTIONS preflight")
	}
}

// ---------- requireAuth ----------

func TestRequireAuth_ValidToken(t *testing.T) {
	h := newHandlerNoDB(t, "")
	req := httptest.NewRequest(http.MethodGet, "/", nil)
	req.Header.Set("Authorization", "Bearer "+makeTestToken("secret"))
	w := httptest.NewRecorder()

	if !h.requireAuth(w, req) {
		t.Error("expected requireAuth to return true for valid token")
	}
}

func TestRequireAuth_MissingToken(t *testing.T) {
	h := newHandlerNoDB(t, "")
	req := httptest.NewRequest(http.MethodGet, "/", nil)
	w := httptest.NewRecorder()

	if h.requireAuth(w, req) {
		t.Error("expected requireAuth to return false when no token")
	}
	if w.Code != http.StatusUnauthorized {
		t.Errorf("expected 401, got %d", w.Code)
	}
}

func TestRequireAuth_InvalidToken(t *testing.T) {
	h := newHandlerNoDB(t, "")
	req := httptest.NewRequest(http.MethodGet, "/", nil)
	req.Header.Set("Authorization", "Bearer invalidtoken")
	w := httptest.NewRecorder()

	if h.requireAuth(w, req) {
		t.Error("expected requireAuth to return false for invalid token")
	}
	if w.Code != http.StatusUnauthorized {
		t.Errorf("expected 401, got %d", w.Code)
	}
}
