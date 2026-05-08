package db

import (
	"database/sql"
	"fmt"
	"log"

	_ "github.com/lib/pq"
	"notification-service/internal/config"
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
		 VALUES ('FOLLOW'), ('USER_POST'), ('RECOMMENDED_POST')
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
	}

	for _, q := range migrations {
		if _, err := database.Exec(q); err != nil {
			log.Fatalf("Migration failed:\n%s\nError: %v", q, err)
		}
	}

	log.Println("Database migrations completed successfully")
}
