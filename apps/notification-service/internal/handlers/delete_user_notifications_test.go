package handlers

import (
	"encoding/json"
	"errors"
	"net/http"
	"net/http/httptest"
	"regexp"
	"testing"

	"notification-service/internal/config"
	"notification-service/internal/hub"

	"github.com/DATA-DOG/go-sqlmock"
	"github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
)

// signedTokenWithRoles creates a JWT carrying the given role strings in the
// "role" array claim, matching the structure produced by authentication-service.
func signedTokenWithRoles(userID uuid.UUID, roles []string) string {
	rolesIface := make([]interface{}, len(roles))
	for i, r := range roles {
		rolesIface[i] = r
	}
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, jwt.MapClaims{
		"userId": userID.String(),
		"role":   rolesIface,
	})
	signed, err := token.SignedString([]byte(testJWTSecret))
	if err != nil {
		panic(err)
	}
	return signed
}

// performRequestWithToken sends a request using an explicit raw Authorization
// header value (e.g. "Bearer <token>"), allowing role-bearing tokens to be used
// without going through the uuid-only signedToken helper.
func performRequestWithToken(h *Handler, method, target, authHeader string) *httptest.ResponseRecorder {
	mux := http.NewServeMux()
	h.Register(mux)

	req := httptest.NewRequest(method, target, nil)
	if authHeader != "" {
		req.Header.Set("Authorization", authHeader)
	}

	rec := httptest.NewRecorder()
	mux.ServeHTTP(rec, req)
	return rec
}

func newTestHandlerForDelete(t *testing.T) (*Handler, sqlmock.Sqlmock, func()) {
	t.Helper()
	db, mock, err := sqlmock.New(sqlmock.QueryMatcherOption(sqlmock.QueryMatcherRegexp))
	if err != nil {
		t.Fatalf("failed to create sqlmock: %v", err)
	}
	h := New(db, hub.NewHub(), &config.Config{
		JWTSecret:      testJWTSecret,
		AllowedOrigins: "http://localhost:19293",
	})
	return h, mock, func() {
		if err := mock.ExpectationsWereMet(); err != nil {
			t.Fatalf("unmet sql expectations: %v", err)
		}
		db.Close()
	}
}

// ---------------------------------------------------------------------------
// DELETE /notifications/users/me — self-service
// ---------------------------------------------------------------------------

func TestDeleteUserNotifications(t *testing.T) {
	userID := uuid.New()

	t.Run("deletes all notifications for authenticated user and returns count", func(t *testing.T) {
		h, mock, cleanup := newTestHandlerForDelete(t)
		defer cleanup()

		mock.ExpectExec(regexp.QuoteMeta(`DELETE FROM notifications WHERE user_id = $1 OR actor_id = $1`)).
			WithArgs(userID).
			WillReturnResult(sqlmock.NewResult(0, 5))

		rec := performRequest(h, http.MethodDelete, "/notifications/users/me", nil, userID)

		if rec.Code != http.StatusOK {
			t.Fatalf("expected %d, got %d: %s", http.StatusOK, rec.Code, rec.Body.String())
		}
		var got map[string]int64
		if err := json.NewDecoder(rec.Body).Decode(&got); err != nil {
			t.Fatalf("decode error: %v", err)
		}
		if got["deleted"] != 5 {
			t.Fatalf("expected deleted=5, got %+v", got)
		}
	})

	t.Run("returns zero when user has no notifications", func(t *testing.T) {
		h, mock, cleanup := newTestHandlerForDelete(t)
		defer cleanup()

		mock.ExpectExec(regexp.QuoteMeta(`DELETE FROM notifications WHERE user_id = $1 OR actor_id = $1`)).
			WithArgs(userID).
			WillReturnResult(sqlmock.NewResult(0, 0))

		rec := performRequest(h, http.MethodDelete, "/notifications/users/me", nil, userID)

		if rec.Code != http.StatusOK {
			t.Fatalf("expected %d, got %d: %s", http.StatusOK, rec.Code, rec.Body.String())
		}
		var got map[string]int64
		if err := json.NewDecoder(rec.Body).Decode(&got); err != nil {
			t.Fatalf("decode error: %v", err)
		}
		if got["deleted"] != 0 {
			t.Fatalf("expected deleted=0, got %+v", got)
		}
	})

	t.Run("rejects request without JWT with 401", func(t *testing.T) {
		h, _, cleanup := newTestHandlerForDelete(t)
		defer cleanup()

		rec := performRequest(h, http.MethodDelete, "/notifications/users/me", nil, uuid.Nil)

		if rec.Code != http.StatusUnauthorized {
			t.Fatalf("expected %d, got %d", http.StatusUnauthorized, rec.Code)
		}
	})

	t.Run("returns 500 on database error", func(t *testing.T) {
		h, mock, cleanup := newTestHandlerForDelete(t)
		defer cleanup()

		mock.ExpectExec(regexp.QuoteMeta(`DELETE FROM notifications WHERE user_id = $1 OR actor_id = $1`)).
			WithArgs(userID).
			WillReturnError(errors.New("connection refused"))

		rec := performRequest(h, http.MethodDelete, "/notifications/users/me", nil, userID)

		if rec.Code != http.StatusInternalServerError {
			t.Fatalf("expected %d, got %d", http.StatusInternalServerError, rec.Code)
		}
	})
}

// ---------------------------------------------------------------------------
// DELETE /notifications/users/{userId} — admin
// ---------------------------------------------------------------------------

func TestAdminDeleteUserNotifications(t *testing.T) {
	adminID := uuid.New()
	targetID := uuid.New()

	adminToken := "Bearer " + signedTokenWithRoles(adminID, []string{"ROLE_ADMIN"})
	userToken := "Bearer " + signedTokenWithRoles(adminID, []string{"ROLE_USER"})
	noRoleToken := "Bearer " + signedToken(adminID) // signedToken produces no role claim

	t.Run("admin deletes target user notifications and returns count", func(t *testing.T) {
		h, mock, cleanup := newTestHandlerForDelete(t)
		defer cleanup()

		mock.ExpectExec(regexp.QuoteMeta(`DELETE FROM notifications WHERE user_id = $1 OR actor_id = $1`)).
			WithArgs(targetID).
			WillReturnResult(sqlmock.NewResult(0, 7))

		rec := performRequestWithToken(h, http.MethodDelete, "/notifications/users/"+targetID.String(), adminToken)

		if rec.Code != http.StatusOK {
			t.Fatalf("expected %d, got %d: %s", http.StatusOK, rec.Code, rec.Body.String())
		}
		var got map[string]int64
		if err := json.NewDecoder(rec.Body).Decode(&got); err != nil {
			t.Fatalf("decode error: %v", err)
		}
		if got["deleted"] != 7 {
			t.Fatalf("expected deleted=7, got %+v", got)
		}
	})

	t.Run("returns zero when target user has no notifications", func(t *testing.T) {
		h, mock, cleanup := newTestHandlerForDelete(t)
		defer cleanup()

		mock.ExpectExec(regexp.QuoteMeta(`DELETE FROM notifications WHERE user_id = $1 OR actor_id = $1`)).
			WithArgs(targetID).
			WillReturnResult(sqlmock.NewResult(0, 0))

		rec := performRequestWithToken(h, http.MethodDelete, "/notifications/users/"+targetID.String(), adminToken)

		if rec.Code != http.StatusOK {
			t.Fatalf("expected %d, got %d: %s", http.StatusOK, rec.Code, rec.Body.String())
		}
		var got map[string]int64
		if err := json.NewDecoder(rec.Body).Decode(&got); err != nil {
			t.Fatalf("decode error: %v", err)
		}
		if got["deleted"] != 0 {
			t.Fatalf("expected deleted=0, got %+v", got)
		}
	})

	t.Run("rejects request without JWT with 401", func(t *testing.T) {
		h, _, cleanup := newTestHandlerForDelete(t)
		defer cleanup()

		rec := performRequestWithToken(h, http.MethodDelete, "/notifications/users/"+targetID.String(), "")

		if rec.Code != http.StatusUnauthorized {
			t.Fatalf("expected %d, got %d", http.StatusUnauthorized, rec.Code)
		}
	})

	t.Run("rejects ROLE_USER token with 403", func(t *testing.T) {
		h, _, cleanup := newTestHandlerForDelete(t)
		defer cleanup()

		rec := performRequestWithToken(h, http.MethodDelete, "/notifications/users/"+targetID.String(), userToken)

		if rec.Code != http.StatusForbidden {
			t.Fatalf("expected %d, got %d", http.StatusForbidden, rec.Code)
		}
	})

	t.Run("rejects token with no role claim with 403", func(t *testing.T) {
		h, _, cleanup := newTestHandlerForDelete(t)
		defer cleanup()

		rec := performRequestWithToken(h, http.MethodDelete, "/notifications/users/"+targetID.String(), noRoleToken)

		if rec.Code != http.StatusForbidden {
			t.Fatalf("expected %d, got %d", http.StatusForbidden, rec.Code)
		}
	})

	t.Run("rejects malformed userId path param with 400", func(t *testing.T) {
		h, _, cleanup := newTestHandlerForDelete(t)
		defer cleanup()

		rec := performRequestWithToken(h, http.MethodDelete, "/notifications/users/not-a-uuid", adminToken)

		if rec.Code != http.StatusBadRequest {
			t.Fatalf("expected %d, got %d", http.StatusBadRequest, rec.Code)
		}
	})

	t.Run("returns 500 on database error", func(t *testing.T) {
		h, mock, cleanup := newTestHandlerForDelete(t)
		defer cleanup()

		mock.ExpectExec(regexp.QuoteMeta(`DELETE FROM notifications WHERE user_id = $1 OR actor_id = $1`)).
			WithArgs(targetID).
			WillReturnError(errors.New("connection refused"))

		rec := performRequestWithToken(h, http.MethodDelete, "/notifications/users/"+targetID.String(), adminToken)

		if rec.Code != http.StatusInternalServerError {
			t.Fatalf("expected %d, got %d", http.StatusInternalServerError, rec.Code)
		}
	})
}
