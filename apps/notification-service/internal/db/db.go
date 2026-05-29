package db

import (
	"database/sql"
	"fmt"
	"log"

	"notification-service/internal/config"

	_ "github.com/lib/pq"
)

// Connect opens a PostgreSQL connection and verifies connectivity.
func Connect(cfg *config.Config) *sql.DB {
	dsn := fmt.Sprintf(
		"host=%s port=%s user=%s password=%s dbname=%s sslmode=disable",
		cfg.DBHost, cfg.DBPort, cfg.DBUser, cfg.DBPassword, cfg.DBName,
	)

	database, err := sql.Open("postgres", dsn)
	if err != nil {
		log.Fatalf("Failed to open database connection: %v", err)
	}

	if err := database.Ping(); err != nil {
		log.Fatalf("Failed to ping database: %v", err)
	}

	log.Println("Database connected successfully")
	return database
}

// RunMigrations creates all required tables if they do not already exist.
func RunMigrations(database *sql.DB) {
	migrations := []string{
		// Seed table for valid notification type values (acts as a constraint reference)
		`CREATE TABLE IF NOT EXISTS notification_types (
			name VARCHAR(50) PRIMARY KEY
		)`,

		`INSERT INTO notification_types (name)
		 VALUES ('FOLLOW'), ('USER_POST'), ('RECOMMENDED_POST'), ('LIKE'), ('COMMENT')
		 ON CONFLICT DO NOTHING`,

		// Core notifications table — extensible via the type column
		`CREATE TABLE IF NOT EXISTS notifications (
			id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
			user_id    UUID        NOT NULL,
			actor_id   UUID,
			type       VARCHAR(50) NOT NULL REFERENCES notification_types(name),
			message    TEXT        NOT NULL,
			read_at    TIMESTAMPTZ,
			created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
		)`,

		`CREATE INDEX IF NOT EXISTS idx_notifications_user_id
		 ON notifications (user_id)`,

		`CREATE INDEX IF NOT EXISTS idx_notifications_created_at
		 ON notifications (created_at DESC)`,

		// Per-user, per-type notification preferences — defaults to enabled
		`CREATE TABLE IF NOT EXISTS notification_preferences (
			user_id           UUID        NOT NULL,
			notification_type VARCHAR(50) NOT NULL REFERENCES notification_types(name),
			enabled           BOOLEAN     NOT NULL DEFAULT TRUE,
			PRIMARY KEY (user_id, notification_type)
		)`,

		// Per-user opt-in subscriptions for new-post notifications from a specific author.
		// A row here means the subscriber wants notifications when the author posts.
		// Absence means no notification (opt-in, off by default).
		`CREATE TABLE IF NOT EXISTS user_post_subscriptions (
			subscriber_id UUID NOT NULL,
			author_id     UUID NOT NULL,
			PRIMARY KEY (subscriber_id, author_id)
		)`,

		`CREATE INDEX IF NOT EXISTS idx_user_post_subscriptions_author
		 ON user_post_subscriptions (author_id)`,

		// Add reference_id to support linking a notification back to a specific
		// resource (e.g. a post UUID for USER_POST notifications).
		// Uses ADD COLUMN IF NOT EXISTS so it is safe to re-run on an existing DB.
		`ALTER TABLE notifications ADD COLUMN IF NOT EXISTS reference_id UUID`,

		// Add content to carry optional extra text (e.g. the comment body for
		// COMMENT notifications). Nullable so existing rows are unaffected.
		`ALTER TABLE notifications ADD COLUMN IF NOT EXISTS content TEXT`,
	}

	for _, q := range migrations {
		if _, err := database.Exec(q); err != nil {
			log.Fatalf("Migration failed:\n%s\nError: %v", q, err)
		}
	}

	log.Println("Database migrations completed successfully")
}
