package handlers

import (
	"database/sql"
	"encoding/json"
	"net/http"
	"strconv"
	"time"

	"github.com/google/uuid"
	"notification-service/internal/models"
)

func (h *Handler) createNotification(w http.ResponseWriter, r *http.Request) {
	var req models.CreateNotificationRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "invalid request body", http.StatusBadRequest)
		return
	}

	if req.UserID == uuid.Nil || req.Message == "" || req.Type == "" {
		http.Error(w, "userId, type, and message are required", http.StatusBadRequest)
		return
	}

	// Respect the recipient's notification preferences
	if !h.isNotificationEnabled(req.UserID, req.Type) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		json.NewEncoder(w).Encode(map[string]string{"status": "skipped", "reason": "notifications disabled by user"})
		return
	}

	notif := models.Notification{
		ID:        uuid.New(),
		UserID:    req.UserID,
		ActorID:   req.ActorID,
		Type:      req.Type,
		Message:   req.Message,
		CreatedAt: time.Now().UTC(),
	}

	_, err := h.db.Exec(
		`INSERT INTO notifications (id, user_id, actor_id, type, message, created_at)
		 VALUES ($1, $2, $3, $4, $5, $6)`,
		notif.ID, notif.UserID, notif.ActorID, string(notif.Type), notif.Message, notif.CreatedAt,
	)
	if err != nil {
		http.Error(w, "failed to persist notification", http.StatusInternalServerError)
		return
	}

	// Push to user's WebSocket connection if they are currently online
	msg, _ := json.Marshal(models.WebSocketMessage{Type: "NOTIFICATION", Payload: notif})
	h.hub.SendToUser(req.UserID, msg)

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusCreated)
	json.NewEncoder(w).Encode(notif)
}

func (h *Handler) listNotifications(w http.ResponseWriter, r *http.Request) {
	claims, err := h.extractClaims(r)
	if err != nil {
		http.Error(w, "unauthorized", http.StatusUnauthorized)
		return
	}

	limit, _ := strconv.Atoi(r.URL.Query().Get("limit"))
	if limit <= 0 || limit > 100 {
		limit = 10
	}
	offset, _ := strconv.Atoi(r.URL.Query().Get("offset"))
	if offset < 0 {
		offset = 0
	}

	rows, err := h.db.Query(
		`SELECT id, user_id, actor_id, type, message, read_at, created_at
		 FROM notifications
		 WHERE user_id = $1
		 ORDER BY created_at DESC
		 LIMIT $2 OFFSET $3`,
		claims.UserID, limit, offset,
	)
	if err != nil {
		http.Error(w, "failed to fetch notifications", http.StatusInternalServerError)
		return
	}
	defer rows.Close()

	notifications := make([]models.Notification, 0)
	for rows.Next() {
		var n models.Notification
		var notifType string
		if err := rows.Scan(&n.ID, &n.UserID, &n.ActorID, &notifType, &n.Message, &n.ReadAt, &n.CreatedAt); err != nil {
			continue
		}
		n.Type = models.NotificationTypeValue(notifType)
		notifications = append(notifications, n)
	}

	var total int
	h.db.QueryRow(`SELECT COUNT(*) FROM notifications WHERE user_id = $1`, claims.UserID).Scan(&total)

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(models.NotificationsResponse{
		Notifications: notifications,
		Total:         total,
		Limit:         limit,
		Offset:        offset,
	})
}

func (h *Handler) markAsRead(w http.ResponseWriter, r *http.Request) {
	claims, err := h.extractClaims(r)
	if err != nil {
		http.Error(w, "unauthorized", http.StatusUnauthorized)
		return
	}

	notifID, err := uuid.Parse(r.PathValue("id"))
	if err != nil {
		http.Error(w, "invalid notification id", http.StatusBadRequest)
		return
	}

	now := time.Now().UTC()
	result, err := h.db.Exec(
		`UPDATE notifications
		 SET read_at = $1
		 WHERE id = $2 AND user_id = $3 AND read_at IS NULL`,
		now, notifID, claims.UserID,
	)
	if err != nil {
		http.Error(w, "failed to mark as read", http.StatusInternalServerError)
		return
	}

	affected, _ := result.RowsAffected()
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]bool{"updated": affected > 0})
}

func (h *Handler) markAllRead(w http.ResponseWriter, r *http.Request) {
	claims, err := h.extractClaims(r)
	if err != nil {
		http.Error(w, "unauthorized", http.StatusUnauthorized)
		return
	}

	now := time.Now().UTC()
	result, err := h.db.Exec(
		`UPDATE notifications SET read_at = $1 WHERE user_id = $2 AND read_at IS NULL`,
		now, claims.UserID,
	)
	if err != nil {
		http.Error(w, "failed to mark all as read", http.StatusInternalServerError)
		return
	}

	affected, _ := result.RowsAffected()
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]int64{"updated": affected})
}

func (h *Handler) unreadCount(w http.ResponseWriter, r *http.Request) {
	claims, err := h.extractClaims(r)
	if err != nil {
		http.Error(w, "unauthorized", http.StatusUnauthorized)
		return
	}

	var count int
	h.db.QueryRow(
		`SELECT COUNT(*) FROM notifications WHERE user_id = $1 AND read_at IS NULL`,
		claims.UserID,
	).Scan(&count)

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]int{"count": count})
}

// isNotificationEnabled checks the user's preference for a given notification type.
// Returns true (enabled) when no explicit preference record exists (default-on behaviour).
func (h *Handler) isNotificationEnabled(userID uuid.UUID, notifType models.NotificationTypeValue) bool {
	var enabled bool
	err := h.db.QueryRow(
		`SELECT enabled FROM notification_preferences
		 WHERE user_id = $1 AND notification_type = $2`,
		userID, string(notifType),
	).Scan(&enabled)

	if err == sql.ErrNoRows {
		return true // default: enabled
	}
	if err != nil {
		return true // fail open — don't block notification on DB error
	}
	return enabled
}
