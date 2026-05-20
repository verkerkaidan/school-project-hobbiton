package handlers

import (
	"database/sql"
	"encoding/json"
	"errors"
	"net/http"

	"github.com/google/uuid"
)

// getPostSubscription returns whether the authenticated user is subscribed to
// new-post notifications for the given author.
//
// GET /user-post-subscriptions/{authorId}
func (h *Handler) getPostSubscription(w http.ResponseWriter, r *http.Request) {
	claims, err := h.extractClaims(r)
	if err != nil {
		http.Error(w, "unauthorized", http.StatusUnauthorized)
		return
	}

	authorID, err := uuid.Parse(r.PathValue("authorId"))
	if err != nil {
		http.Error(w, "invalid authorId", http.StatusBadRequest)
		return
	}

	var dummy int
	err = h.db.QueryRow(
		`SELECT 1 FROM user_post_subscriptions
		 WHERE subscriber_id = $1 AND author_id = $2`,
		claims.UserID, authorID,
	).Scan(&dummy)

	subscribed := !errors.Is(err, sql.ErrNoRows)
	if err != nil && !errors.Is(err, sql.ErrNoRows) {
		http.Error(w, "failed to check subscription", http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]bool{"subscribed": subscribed})
}

// togglePostSubscription inserts or deletes the subscription row for the
// authenticated user / given author pair, then returns the new state.
//
// PUT /user-post-subscriptions/{authorId}
func (h *Handler) togglePostSubscription(w http.ResponseWriter, r *http.Request) {
	claims, err := h.extractClaims(r)
	if err != nil {
		http.Error(w, "unauthorized", http.StatusUnauthorized)
		return
	}

	authorID, err := uuid.Parse(r.PathValue("authorId"))
	if err != nil {
		http.Error(w, "invalid authorId", http.StatusBadRequest)
		return
	}

	// Check current state
	var dummy int
	err = h.db.QueryRow(
		`SELECT 1 FROM user_post_subscriptions
		 WHERE subscriber_id = $1 AND author_id = $2`,
		claims.UserID, authorID,
	).Scan(&dummy)

	alreadySubscribed := !errors.Is(err, sql.ErrNoRows)
	if err != nil && !errors.Is(err, sql.ErrNoRows) {
		http.Error(w, "failed to check subscription", http.StatusInternalServerError)
		return
	}

	var subscribed bool
	if alreadySubscribed {
		_, err = h.db.Exec(
			`DELETE FROM user_post_subscriptions
			 WHERE subscriber_id = $1 AND author_id = $2`,
			claims.UserID, authorID,
		)
		subscribed = false
	} else {
		_, err = h.db.Exec(
			`INSERT INTO user_post_subscriptions (subscriber_id, author_id)
			 VALUES ($1, $2)
			 ON CONFLICT DO NOTHING`,
			claims.UserID, authorID,
		)
		subscribed = true
	}

	if err != nil {
		http.Error(w, "failed to update subscription", http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]bool{"subscribed": subscribed})
}
