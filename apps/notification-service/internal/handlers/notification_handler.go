package handlers

import (
	"database/sql"
	"encoding/json"
	"net/http"
	"strconv"
	"time"

	"notification-service/internal/models"

	"github.com/google/uuid"
)

// batchCreateNotifications creates USER_POST notifications for a list of
// recipient IDs. Each recipient is only notified if:
//  1. Their global USER_POST preference is enabled (or unset — default on), AND
//  2. They have an active subscription row in user_post_subscriptions for the
//     given actorId (opt-in, off by default).
//
// This endpoint is internal — it is not JWT-protected and must not be exposed
// through the API gateway's public routing.
//
// POST /notifications/batch
func (h *Handler) batchCreateNotifications(w http.ResponseWriter, r *http.Request) {
	var req struct {
		ActorID      uuid.UUID   `json:"actorId"`
		RecipientIDs []uuid.UUID `json:"recipientIds"`
		Type         string      `json:"type"`
		Message      string      `json:"message"`
		ReferenceID  *uuid.UUID  `json:"referenceId"`
	}
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "invalid request body", http.StatusBadRequest)
		return
	}
	if req.ActorID == uuid.Nil || req.Message == "" || req.Type == "" {
		http.Error(w, "actorId, type, and message are required", http.StatusBadRequest)
		return
	}

	created, skipped := 0, 0
	notifType := models.NotificationTypeValue(req.Type)

	for _, recipientID := range req.RecipientIDs {
		// Skip if the user has globally disabled this notification type
		if !h.isNotificationEnabled(recipientID, notifType) {
			skipped++
			continue
		}

		// Skip if the user has not subscribed to this specific author's posts
		var dummy int
		err := h.db.QueryRow(
			`SELECT 1 FROM user_post_subscriptions
			 WHERE subscriber_id = $1 AND author_id = $2`,
			recipientID, req.ActorID,
		).Scan(&dummy)
		if err != nil {
			// sql.ErrNoRows means not subscribed; any other error: skip safely
			skipped++
			continue
		}

		notif := models.Notification{
			ID:          uuid.New(),
			UserID:      recipientID,
			ActorID:     &req.ActorID,
			ReferenceID: req.ReferenceID,
			Type:        notifType,
			Message:     req.Message,
			CreatedAt:   time.Now().UTC(),
		}

		if _, err := h.db.Exec(
			`INSERT INTO notifications (id, user_id, actor_id, type, message, reference_id, created_at)
			 VALUES ($1, $2, $3, $4, $5, $6, $7)`,
			notif.ID, notif.UserID, notif.ActorID, string(notif.Type), notif.Message, notif.ReferenceID, notif.CreatedAt,
		); err != nil {
			skipped++
			continue
		}

		msg, _ := json.Marshal(models.WebSocketMessage{Type: "NOTIFICATION", Payload: notif})
		h.hub.SendToUser(recipientID, msg)
		created++
	}

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	json.NewEncoder(w).Encode(map[string]int{"created": created, "skipped": skipped})
}

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
		ID:          uuid.New(),
		UserID:      req.UserID,
		ActorID:     req.ActorID,
		ReferenceID: req.ReferenceID,
		Content:     req.Content,
		Type:        req.Type,
		Message:     req.Message,
		CreatedAt:   time.Now().UTC(),
	}

	_, err := h.db.Exec(
		`INSERT INTO notifications (id, user_id, actor_id, type, message, content, reference_id, created_at)
		 VALUES ($1, $2, $3, $4, $5, $6, $7, $8)`,
		notif.ID, notif.UserID, notif.ActorID, string(notif.Type), notif.Message, notif.Content, notif.ReferenceID, notif.CreatedAt,
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
		`SELECT id, user_id, actor_id, type, message, content, reference_id, read_at, created_at
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
		if err := rows.Scan(&n.ID, &n.UserID, &n.ActorID, &notifType, &n.Message, &n.Content, &n.ReferenceID, &n.ReadAt, &n.CreatedAt); err != nil {
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
