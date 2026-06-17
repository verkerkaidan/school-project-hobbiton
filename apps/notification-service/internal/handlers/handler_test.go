package handlers

import (
	"bytes"
	"database/sql"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"regexp"
	"testing"
	"time"

	"notification-service/internal/config"
	"notification-service/internal/hub"
	"notification-service/internal/models"

	"github.com/DATA-DOG/go-sqlmock"
	"github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
)

const testJWTSecret = "unit-test-secret"

func newTestHandler(t *testing.T) (*Handler, sqlmock.Sqlmock, func()) {
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

func performRequest(h *Handler, method, target string, body []byte, userID uuid.UUID) *httptest.ResponseRecorder {
	mux := http.NewServeMux()
	h.Register(mux)

	req := httptest.NewRequest(method, target, bytes.NewReader(body))
	if userID != uuid.Nil {
		req.Header.Set("Authorization", "Bearer "+signedToken(userID))
	}
	if body != nil {
		req.Header.Set("Content-Type", "application/json")
	}

	rec := httptest.NewRecorder()
	mux.ServeHTTP(rec, req)
	return rec
}

func signedToken(userID uuid.UUID) string {
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, jwt.MapClaims{
		"userId": userID.String(),
	})
	signed, err := token.SignedString([]byte(testJWTSecret))
	if err != nil {
		panic(err)
	}
	return signed
}

func expectPreference(mock sqlmock.Sqlmock, userID uuid.UUID, notifType models.NotificationTypeValue, enabled bool, err error) {
	row := mock.ExpectQuery(regexp.QuoteMeta(`SELECT enabled FROM notification_preferences
		 WHERE user_id = $1 AND notification_type = $2`)).
		WithArgs(userID, string(notifType))
	if err != nil {
		row.WillReturnError(err)
		return
	}
	row.WillReturnRows(sqlmock.NewRows([]string{"enabled"}).AddRow(enabled))
}

func TestCreateNotification(t *testing.T) {
	userID := uuid.New()
	actorID := uuid.New()
	referenceID := uuid.New()
	content := "nice work"

	t.Run("persists and returns notification when enabled", func(t *testing.T) {
		h, mock, cleanup := newTestHandler(t)
		defer cleanup()

		expectPreference(mock, userID, models.NotificationTypeComment, true, nil)
		mock.ExpectExec(regexp.QuoteMeta(`INSERT INTO notifications (id, user_id, actor_id, type, message, content, reference_id, created_at)
		 VALUES ($1, $2, $3, $4, $5, $6, $7, $8)`)).
			WithArgs(sqlmock.AnyArg(), userID, &actorID, string(models.NotificationTypeComment), "New comment", &content, &referenceID, sqlmock.AnyArg()).
			WillReturnResult(sqlmock.NewResult(0, 1))

		body, _ := json.Marshal(models.CreateNotificationRequest{
			UserID:      userID,
			ActorID:     &actorID,
			Type:        models.NotificationTypeComment,
			Message:     "New comment",
			Content:     &content,
			ReferenceID: &referenceID,
		})
		rec := performRequest(h, http.MethodPost, "/notifications", body, uuid.Nil)

		if rec.Code != http.StatusCreated {
			t.Fatalf("expected status %d, got %d: %s", http.StatusCreated, rec.Code, rec.Body.String())
		}

		var got models.Notification
		if err := json.NewDecoder(rec.Body).Decode(&got); err != nil {
			t.Fatalf("failed to decode response: %v", err)
		}
		if got.UserID != userID || got.ActorID == nil || *got.ActorID != actorID || got.Type != models.NotificationTypeComment {
			t.Fatalf("unexpected notification payload: %+v", got)
		}
		if got.Content == nil || *got.Content != content || got.ReferenceID == nil || *got.ReferenceID != referenceID {
			t.Fatalf("expected optional fields to round-trip, got content=%v reference=%v", got.Content, got.ReferenceID)
		}
	})

	t.Run("skips without insert when disabled by preference", func(t *testing.T) {
		h, mock, cleanup := newTestHandler(t)
		defer cleanup()

		expectPreference(mock, userID, models.NotificationTypeLike, false, nil)

		body, _ := json.Marshal(models.CreateNotificationRequest{
			UserID:  userID,
			Type:    models.NotificationTypeLike,
			Message: "New like",
		})
		rec := performRequest(h, http.MethodPost, "/notifications", body, uuid.Nil)

		if rec.Code != http.StatusOK {
			t.Fatalf("expected status %d, got %d: %s", http.StatusOK, rec.Code, rec.Body.String())
		}

		var got map[string]string
		if err := json.NewDecoder(rec.Body).Decode(&got); err != nil {
			t.Fatalf("failed to decode response: %v", err)
		}
		if got["status"] != "skipped" {
			t.Fatalf("expected skipped response, got %+v", got)
		}
	})

	t.Run("validates required fields before querying preferences", func(t *testing.T) {
		h, _, cleanup := newTestHandler(t)
		defer cleanup()

		rec := performRequest(h, http.MethodPost, "/notifications", []byte(`{"type":"LIKE"}`), uuid.Nil)

		if rec.Code != http.StatusBadRequest {
			t.Fatalf("expected status %d, got %d", http.StatusBadRequest, rec.Code)
		}
	})
}

func TestBatchCreateNotifications(t *testing.T) {
	actorID := uuid.New()
	recipients := []uuid.UUID{uuid.New(), uuid.New(), uuid.New()}
	referenceID := uuid.New()

	h, mock, cleanup := newTestHandler(t)
	defer cleanup()

	expectPreference(mock, recipients[0], models.NotificationTypeUserPost, true, nil)
	mock.ExpectQuery(regexp.QuoteMeta(`SELECT 1 FROM user_post_subscriptions
			 WHERE subscriber_id = $1 AND author_id = $2`)).
		WithArgs(recipients[0], actorID).
		WillReturnRows(sqlmock.NewRows([]string{"1"}).AddRow(1))
	mock.ExpectExec(regexp.QuoteMeta(`INSERT INTO notifications (id, user_id, actor_id, type, message, reference_id, created_at)
			 VALUES ($1, $2, $3, $4, $5, $6, $7)`)).
		WithArgs(sqlmock.AnyArg(), recipients[0], &actorID, string(models.NotificationTypeUserPost), "New post", &referenceID, sqlmock.AnyArg()).
		WillReturnResult(sqlmock.NewResult(0, 1))

	expectPreference(mock, recipients[1], models.NotificationTypeUserPost, false, nil)

	expectPreference(mock, recipients[2], models.NotificationTypeUserPost, true, nil)
	mock.ExpectQuery(regexp.QuoteMeta(`SELECT 1 FROM user_post_subscriptions
			 WHERE subscriber_id = $1 AND author_id = $2`)).
		WithArgs(recipients[2], actorID).
		WillReturnError(sql.ErrNoRows)

	body, _ := json.Marshal(map[string]any{
		"actorId":      actorID,
		"recipientIds": recipients,
		"type":         string(models.NotificationTypeUserPost),
		"message":      "New post",
		"referenceId":  referenceID,
	})
	rec := performRequest(h, http.MethodPost, "/notifications/batch", body, uuid.Nil)

	if rec.Code != http.StatusOK {
		t.Fatalf("expected status %d, got %d: %s", http.StatusOK, rec.Code, rec.Body.String())
	}

	var got map[string]int
	if err := json.NewDecoder(rec.Body).Decode(&got); err != nil {
		t.Fatalf("failed to decode response: %v", err)
	}
	if got["created"] != 1 || got["skipped"] != 2 {
		t.Fatalf("expected one created and two skipped, got %+v", got)
	}
}

func TestListNotifications(t *testing.T) {
	userID := uuid.New()
	actorID := uuid.New()
	referenceID := uuid.New()
	content := "welcome"
	createdAt := time.Date(2026, 6, 15, 12, 0, 0, 0, time.UTC)
	notificationID := uuid.New()

	h, mock, cleanup := newTestHandler(t)
	defer cleanup()

	mock.ExpectQuery(regexp.QuoteMeta(`SELECT id, user_id, actor_id, type, message, content, reference_id, read_at, created_at
		 FROM notifications
		 WHERE user_id = $1
		 ORDER BY created_at DESC
		 LIMIT $2 OFFSET $3`)).
		WithArgs(userID, 10, 0).
		WillReturnRows(sqlmock.NewRows([]string{"id", "user_id", "actor_id", "type", "message", "content", "reference_id", "read_at", "created_at"}).
			AddRow(notificationID, userID, &actorID, string(models.NotificationTypeFollow), "Someone followed you", &content, &referenceID, nil, createdAt))
	mock.ExpectQuery(regexp.QuoteMeta(`SELECT COUNT(*) FROM notifications WHERE user_id = $1`)).
		WithArgs(userID).
		WillReturnRows(sqlmock.NewRows([]string{"count"}).AddRow(1))

	rec := performRequest(h, http.MethodGet, "/notifications?limit=500&offset=-4", nil, userID)

	if rec.Code != http.StatusOK {
		t.Fatalf("expected status %d, got %d: %s", http.StatusOK, rec.Code, rec.Body.String())
	}

	var got models.NotificationsResponse
	if err := json.NewDecoder(rec.Body).Decode(&got); err != nil {
		t.Fatalf("failed to decode response: %v", err)
	}
	if got.Total != 1 || got.Limit != 10 || got.Offset != 0 || len(got.Notifications) != 1 {
		t.Fatalf("unexpected paginated response: %+v", got)
	}
	if got.Notifications[0].ID != notificationID || got.Notifications[0].Type != models.NotificationTypeFollow {
		t.Fatalf("unexpected notification row: %+v", got.Notifications[0])
	}
}

func TestReadStateEndpoints(t *testing.T) {
	userID := uuid.New()
	notificationID := uuid.New()

	t.Run("mark one notification read", func(t *testing.T) {
		h, mock, cleanup := newTestHandler(t)
		defer cleanup()

		mock.ExpectExec(regexp.QuoteMeta(`UPDATE notifications
		 SET read_at = $1
		 WHERE id = $2 AND user_id = $3 AND read_at IS NULL`)).
			WithArgs(sqlmock.AnyArg(), notificationID, userID).
			WillReturnResult(sqlmock.NewResult(0, 1))

		rec := performRequest(h, http.MethodPatch, "/notifications/"+notificationID.String()+"/read", nil, userID)

		if rec.Code != http.StatusOK {
			t.Fatalf("expected status %d, got %d: %s", http.StatusOK, rec.Code, rec.Body.String())
		}

		var got map[string]bool
		if err := json.NewDecoder(rec.Body).Decode(&got); err != nil {
			t.Fatalf("failed to decode response: %v", err)
		}
		if !got["updated"] {
			t.Fatalf("expected updated=true, got %+v", got)
		}
	})

	t.Run("mark all unread notifications read", func(t *testing.T) {
		h, mock, cleanup := newTestHandler(t)
		defer cleanup()

		mock.ExpectExec(regexp.QuoteMeta(`UPDATE notifications SET read_at = $1 WHERE user_id = $2 AND read_at IS NULL`)).
			WithArgs(sqlmock.AnyArg(), userID).
			WillReturnResult(sqlmock.NewResult(0, 3))

		rec := performRequest(h, http.MethodPost, "/notifications/read-all", nil, userID)

		if rec.Code != http.StatusOK {
			t.Fatalf("expected status %d, got %d: %s", http.StatusOK, rec.Code, rec.Body.String())
		}

		var got map[string]int64
		if err := json.NewDecoder(rec.Body).Decode(&got); err != nil {
			t.Fatalf("failed to decode response: %v", err)
		}
		if got["updated"] != 3 {
			t.Fatalf("expected three updates, got %+v", got)
		}
	})

	t.Run("returns unread count", func(t *testing.T) {
		h, mock, cleanup := newTestHandler(t)
		defer cleanup()

		mock.ExpectQuery(regexp.QuoteMeta(`SELECT COUNT(*) FROM notifications WHERE user_id = $1 AND read_at IS NULL`)).
			WithArgs(userID).
			WillReturnRows(sqlmock.NewRows([]string{"count"}).AddRow(4))

		rec := performRequest(h, http.MethodGet, "/notifications/unread-count", nil, userID)

		if rec.Code != http.StatusOK {
			t.Fatalf("expected status %d, got %d: %s", http.StatusOK, rec.Code, rec.Body.String())
		}

		var got map[string]int
		if err := json.NewDecoder(rec.Body).Decode(&got); err != nil {
			t.Fatalf("failed to decode response: %v", err)
		}
		if got["count"] != 4 {
			t.Fatalf("expected count=4, got %+v", got)
		}
	})
}

func TestPreferences(t *testing.T) {
	userID := uuid.New()

	t.Run("returns stored preferences with default enabled values", func(t *testing.T) {
		h, mock, cleanup := newTestHandler(t)
		defer cleanup()

		mock.ExpectQuery(regexp.QuoteMeta(`SELECT notification_type, enabled
		 FROM notification_preferences
		 WHERE user_id = $1`)).
			WithArgs(userID).
			WillReturnRows(sqlmock.NewRows([]string{"notification_type", "enabled"}).
				AddRow(string(models.NotificationTypeLike), false))

		rec := performRequest(h, http.MethodGet, "/notification-preferences", nil, userID)

		if rec.Code != http.StatusOK {
			t.Fatalf("expected status %d, got %d: %s", http.StatusOK, rec.Code, rec.Body.String())
		}

		var got map[string]bool
		if err := json.NewDecoder(rec.Body).Decode(&got); err != nil {
			t.Fatalf("failed to decode response: %v", err)
		}
		if got[string(models.NotificationTypeLike)] {
			t.Fatalf("expected LIKE preference to remain disabled: %+v", got)
		}
		for _, notifType := range []models.NotificationTypeValue{
			models.NotificationTypeFollow,
			models.NotificationTypeUserPost,
			models.NotificationTypeRecommendedPost,
			models.NotificationTypeComment,
		} {
			if !got[string(notifType)] {
				t.Fatalf("expected %s to default enabled: %+v", notifType, got)
			}
		}
	})

	t.Run("upserts a valid preference", func(t *testing.T) {
		h, mock, cleanup := newTestHandler(t)
		defer cleanup()

		mock.ExpectExec(regexp.QuoteMeta(`INSERT INTO notification_preferences (user_id, notification_type, enabled)
		 VALUES ($1, $2, $3)
		 ON CONFLICT (user_id, notification_type) DO UPDATE SET enabled = EXCLUDED.enabled`)).
			WithArgs(userID, string(models.NotificationTypeComment), false).
			WillReturnResult(sqlmock.NewResult(0, 1))

		rec := performRequest(h, http.MethodPut, "/notification-preferences/COMMENT", []byte(`{"enabled":false}`), userID)

		if rec.Code != http.StatusOK {
			t.Fatalf("expected status %d, got %d: %s", http.StatusOK, rec.Code, rec.Body.String())
		}

		var got map[string]bool
		if err := json.NewDecoder(rec.Body).Decode(&got); err != nil {
			t.Fatalf("failed to decode response: %v", err)
		}
		if got["enabled"] {
			t.Fatalf("expected enabled=false, got %+v", got)
		}
	})

	t.Run("rejects invalid preference type before writing", func(t *testing.T) {
		h, _, cleanup := newTestHandler(t)
		defer cleanup()

		rec := performRequest(h, http.MethodPut, "/notification-preferences/UNKNOWN", []byte(`{"enabled":true}`), userID)

		if rec.Code != http.StatusBadRequest {
			t.Fatalf("expected status %d, got %d", http.StatusBadRequest, rec.Code)
		}
	})
}

func TestPostSubscriptions(t *testing.T) {
	userID := uuid.New()
	authorID := uuid.New()

	t.Run("reports existing subscription", func(t *testing.T) {
		h, mock, cleanup := newTestHandler(t)
		defer cleanup()

		mock.ExpectQuery(regexp.QuoteMeta(`SELECT 1 FROM user_post_subscriptions
		 WHERE subscriber_id = $1 AND author_id = $2`)).
			WithArgs(userID, authorID).
			WillReturnRows(sqlmock.NewRows([]string{"1"}).AddRow(1))

		rec := performRequest(h, http.MethodGet, "/user-post-subscriptions/"+authorID.String(), nil, userID)

		if rec.Code != http.StatusOK {
			t.Fatalf("expected status %d, got %d: %s", http.StatusOK, rec.Code, rec.Body.String())
		}

		var got map[string]bool
		if err := json.NewDecoder(rec.Body).Decode(&got); err != nil {
			t.Fatalf("failed to decode response: %v", err)
		}
		if !got["subscribed"] {
			t.Fatalf("expected subscribed=true, got %+v", got)
		}
	})

	t.Run("toggles off existing subscription", func(t *testing.T) {
		h, mock, cleanup := newTestHandler(t)
		defer cleanup()

		mock.ExpectQuery(regexp.QuoteMeta(`SELECT 1 FROM user_post_subscriptions
		 WHERE subscriber_id = $1 AND author_id = $2`)).
			WithArgs(userID, authorID).
			WillReturnRows(sqlmock.NewRows([]string{"1"}).AddRow(1))
		mock.ExpectExec(regexp.QuoteMeta(`DELETE FROM user_post_subscriptions
			 WHERE subscriber_id = $1 AND author_id = $2`)).
			WithArgs(userID, authorID).
			WillReturnResult(sqlmock.NewResult(0, 1))

		rec := performRequest(h, http.MethodPut, "/user-post-subscriptions/"+authorID.String(), nil, userID)

		if rec.Code != http.StatusOK {
			t.Fatalf("expected status %d, got %d: %s", http.StatusOK, rec.Code, rec.Body.String())
		}

		var got map[string]bool
		if err := json.NewDecoder(rec.Body).Decode(&got); err != nil {
			t.Fatalf("failed to decode response: %v", err)
		}
		if got["subscribed"] {
			t.Fatalf("expected subscribed=false, got %+v", got)
		}
	})

	t.Run("toggles on missing subscription", func(t *testing.T) {
		h, mock, cleanup := newTestHandler(t)
		defer cleanup()

		mock.ExpectQuery(regexp.QuoteMeta(`SELECT 1 FROM user_post_subscriptions
		 WHERE subscriber_id = $1 AND author_id = $2`)).
			WithArgs(userID, authorID).
			WillReturnError(sql.ErrNoRows)
		mock.ExpectExec(regexp.QuoteMeta(`INSERT INTO user_post_subscriptions (subscriber_id, author_id)
			 VALUES ($1, $2)
			 ON CONFLICT DO NOTHING`)).
			WithArgs(userID, authorID).
			WillReturnResult(sqlmock.NewResult(0, 1))

		rec := performRequest(h, http.MethodPut, "/user-post-subscriptions/"+authorID.String(), nil, userID)

		if rec.Code != http.StatusOK {
			t.Fatalf("expected status %d, got %d: %s", http.StatusOK, rec.Code, rec.Body.String())
		}

		var got map[string]bool
		if err := json.NewDecoder(rec.Body).Decode(&got); err != nil {
			t.Fatalf("failed to decode response: %v", err)
		}
		if !got["subscribed"] {
			t.Fatalf("expected subscribed=true, got %+v", got)
		}
	})
}

func TestAuthAndCORS(t *testing.T) {
	h, _, cleanup := newTestHandler(t)
	defer cleanup()

	rec := performRequest(h, http.MethodGet, "/notifications", nil, uuid.Nil)
	if rec.Code != http.StatusUnauthorized {
		t.Fatalf("expected status %d, got %d", http.StatusUnauthorized, rec.Code)
	}

	req := httptest.NewRequest(http.MethodOptions, "/notifications", nil)
	req.Header.Set("Origin", "http://localhost:19293/")
	corsRec := httptest.NewRecorder()
	withCORS(h.cfg, h.health)(corsRec, req)

	if corsRec.Code != http.StatusNoContent {
		t.Fatalf("expected preflight status %d, got %d", http.StatusNoContent, corsRec.Code)
	}
	if got := corsRec.Header().Get("Access-Control-Allow-Origin"); got != "http://localhost:19293" {
		t.Fatalf("unexpected CORS origin header %q", got)
	}
}
