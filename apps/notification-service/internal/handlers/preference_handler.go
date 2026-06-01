package handlers

import (
	"encoding/json"
	"net/http"

	"notification-service/internal/models"
)

func (h *Handler) getPreferences(w http.ResponseWriter, r *http.Request) {
	claims, err := h.extractClaims(r)
	if err != nil {
		http.Error(w, "unauthorized", http.StatusUnauthorized)
		return
	}

	rows, err := h.db.Query(
		`SELECT notification_type, enabled
		 FROM notification_preferences
		 WHERE user_id = $1`,
		claims.UserID,
	)
	if err != nil {
		http.Error(w, "failed to fetch preferences", http.StatusInternalServerError)
		return
	}
	defer rows.Close()

	prefs := map[string]bool{}
	for rows.Next() {
		var notifType string
		var enabled bool
		if rows.Scan(&notifType, &enabled) == nil {
			prefs[notifType] = enabled
		}
	}

	// Fill in defaults for types not yet stored so the UI always has all keys
	for _, t := range []models.NotificationTypeValue{
		models.NotificationTypeFollow,
		models.NotificationTypeUserPost,
		models.NotificationTypeRecommendedPost,
		models.NotificationTypeLike,
		models.NotificationTypeComment,
	} {
		if _, ok := prefs[string(t)]; !ok {
			prefs[string(t)] = true
		}
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(prefs)
}

func (h *Handler) updatePreference(w http.ResponseWriter, r *http.Request) {
	claims, err := h.extractClaims(r)
	if err != nil {
		http.Error(w, "unauthorized", http.StatusUnauthorized)
		return
	}

	notifType := models.NotificationTypeValue(r.PathValue("type"))
	switch notifType {
	case models.NotificationTypeFollow,
		models.NotificationTypeUserPost,
		models.NotificationTypeRecommendedPost,
		models.NotificationTypeLike,
		models.NotificationTypeComment:
		// valid type
	default:
		http.Error(w, "invalid notification type", http.StatusBadRequest)
		return
	}

	var body struct {
		Enabled bool `json:"enabled"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		http.Error(w, "invalid request body", http.StatusBadRequest)
		return
	}

	_, err = h.db.Exec(
		`INSERT INTO notification_preferences (user_id, notification_type, enabled)
		 VALUES ($1, $2, $3)
		 ON CONFLICT (user_id, notification_type) DO UPDATE SET enabled = EXCLUDED.enabled`,
		claims.UserID, string(notifType), body.Enabled,
	)
	if err != nil {
		http.Error(w, "failed to update preference", http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]bool{"enabled": body.Enabled})
}
