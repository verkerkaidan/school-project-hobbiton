package handlers

import (
	"database/sql"
	"errors"
	"testing"

	"notification-service/internal/models"

	"github.com/google/uuid"
)

func TestIsNotificationEnabled(t *testing.T) {
	userID := uuid.New()

	t.Run("default enabled when no preference exists", func(t *testing.T) {
		h, mock, cleanup := newTestHandler(t)
		defer cleanup()

		expectPreference(mock, userID, models.NotificationTypeFollow, false, sql.ErrNoRows)

		if !h.isNotificationEnabled(userID, models.NotificationTypeFollow) {
			t.Fatal("expected missing preference to default enabled")
		}
	})

	t.Run("returns stored disabled preference", func(t *testing.T) {
		h, mock, cleanup := newTestHandler(t)
		defer cleanup()

		expectPreference(mock, userID, models.NotificationTypeUserPost, false, nil)

		if h.isNotificationEnabled(userID, models.NotificationTypeUserPost) {
			t.Fatal("expected stored disabled preference to be respected")
		}
	})

	t.Run("fails open on database errors", func(t *testing.T) {
		h, mock, cleanup := newTestHandler(t)
		defer cleanup()

		expectPreference(mock, userID, models.NotificationTypeComment, false, errors.New("database unavailable"))

		if !h.isNotificationEnabled(userID, models.NotificationTypeComment) {
			t.Fatal("expected database errors to fail open")
		}
	})
}
