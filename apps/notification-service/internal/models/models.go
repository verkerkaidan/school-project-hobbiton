package models

import (
	"time"

	"github.com/google/uuid"
)

// NotificationTypeValue enumerates all supported notification types.
// New types can be added here without breaking existing records.
type NotificationTypeValue string

const (
	NotificationTypeFollow          NotificationTypeValue = "FOLLOW"
	NotificationTypeUserPost        NotificationTypeValue = "USER_POST"
	NotificationTypeRecommendedPost NotificationTypeValue = "RECOMMENDED_POST"
	NotificationTypeLike            NotificationTypeValue = "LIKE"
	NotificationTypeComment         NotificationTypeValue = "COMMENT"
)

// Notification represents a persisted notification record.
type Notification struct {
	ID          uuid.UUID             `json:"id"`
	UserID      uuid.UUID             `json:"userId"`  // recipient
	ActorID     *uuid.UUID            `json:"actorId"` // who triggered it (null for system notifications)
	Type        NotificationTypeValue `json:"type"`
	Message     string                `json:"message"`
	Content     *string               `json:"content"`     // optional extra body (e.g. comment text)
	ReferenceID *uuid.UUID            `json:"referenceId"` // optional link target (e.g. post UUID)
	ReadAt      *time.Time            `json:"readAt"`
	CreatedAt   time.Time             `json:"createdAt"`
}

// NotificationPreference stores whether a user has enabled a specific notification type.
type NotificationPreference struct {
	UserID           uuid.UUID             `json:"userId"`
	NotificationType NotificationTypeValue `json:"notificationType"`
	Enabled          bool                  `json:"enabled"`
}

// CreateNotificationRequest is the inbound payload from other services.
type CreateNotificationRequest struct {
	UserID      uuid.UUID             `json:"userId"` // recipient
	ActorID     *uuid.UUID            `json:"actorId"`
	Type        NotificationTypeValue `json:"type"`
	Message     string                `json:"message"`
	Content     *string               `json:"content"`     // optional extra body
	ReferenceID *uuid.UUID            `json:"referenceId"` // optional link target
}

// WebSocketMessage is the envelope sent over WebSocket connections to clients.
type WebSocketMessage struct {
	Type    string      `json:"type"`
	Payload interface{} `json:"payload"`
}

// NotificationsResponse wraps paginated notification results.
type NotificationsResponse struct {
	Notifications []Notification `json:"notifications"`
	Total         int            `json:"total"`
	Limit         int            `json:"limit"`
	Offset        int            `json:"offset"`
}
