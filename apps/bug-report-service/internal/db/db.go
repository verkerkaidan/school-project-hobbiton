package db

import (
	"database/sql"
	"fmt"
	"log"

	"bug-report-service/internal/config"

	"github.com/lib/pq"
	_ "github.com/lib/pq"
)

// Bootstrap ensures the target database exists before the service starts.
// If admin credentials (POSTGRES_USER / POSTGRES_PASSWORD) are provided, it
// connects to the 'postgres' maintenance database as a superuser and creates
// the service user + database if they do not already exist.
// This makes the service fully self-provisioning — no init-db.sh required.
func Bootstrap(cfg *config.Config) *sql.DB {
	if cfg.DBAdminUser != "" && cfg.DBAdminPassword != "" {
		ensureDatabase(cfg)
	} else {
		log.Println("No admin credentials provided; skipping DB bootstrap — assuming database already exists")
	}
	return connect(cfg)
}

// ensureDatabase connects as superuser and creates the database and service
// user if they do not already exist.
func ensureDatabase(cfg *config.Config) {
	adminDSN := fmt.Sprintf(
		"host=%s port=%s user=%s password=%s dbname=postgres sslmode=disable",
		cfg.DBHost, cfg.DBPort, cfg.DBAdminUser, cfg.DBAdminPassword,
	)

	adminDB, err := sql.Open("postgres", adminDSN)
	if err != nil {
		log.Printf("Warning: could not open admin connection for DB bootstrap: %v — skipping", err)
		return
	}
	defer adminDB.Close()

	if err := adminDB.Ping(); err != nil {
		log.Printf("Warning: could not reach postgres as admin for DB bootstrap: %v — skipping", err)
		return
	}

	// CREATE USER … IF NOT EXISTS is not available in older Postgres versions,
	// so we do a conditional DO block instead.
	createUserSQL := fmt.Sprintf(
		`DO $$ BEGIN
		   IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = %s) THEN
		     CREATE USER %s WITH PASSWORD %s;
		   END IF;
		 END $$`,
		pq.QuoteLiteral(cfg.DBUser),
		pq.QuoteIdentifier(cfg.DBUser),
		pq.QuoteLiteral(cfg.DBPassword),
	)
	if _, err := adminDB.Exec(createUserSQL); err != nil {
		log.Printf("Warning: could not create service user %q: %v", cfg.DBUser, err)
	}

	// Check whether the database already exists.
	var exists int
	_ = adminDB.QueryRow("SELECT 1 FROM pg_database WHERE datname = $1", cfg.DBName).Scan(&exists)

	if exists == 0 {
		_, err = adminDB.Exec(fmt.Sprintf(
			"CREATE DATABASE %s OWNER %s",
			pq.QuoteIdentifier(cfg.DBName),
			pq.QuoteIdentifier(cfg.DBUser),
		))
		if err != nil {
			log.Fatalf("Failed to create database %q: %v", cfg.DBName, err)
		}
		_, _ = adminDB.Exec(fmt.Sprintf(
			"GRANT ALL PRIVILEGES ON DATABASE %s TO %s",
			pq.QuoteIdentifier(cfg.DBName),
			pq.QuoteIdentifier(cfg.DBUser),
		))
		log.Printf("Database %q created and granted to %q", cfg.DBName, cfg.DBUser)
	} else {
		log.Printf("Database %q already exists — skipping creation", cfg.DBName)
	}
}

// connect opens a connection to the service's own database.
func connect(cfg *config.Config) *sql.DB {
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
		`CREATE TABLE IF NOT EXISTS bug_reports (
			id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
			title       VARCHAR(255) NOT NULL,
			description TEXT         NOT NULL,
			status      VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
			created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
			closed_at   TIMESTAMPTZ
		)`,

		`CREATE INDEX IF NOT EXISTS idx_bug_reports_status
		 ON bug_reports (status)`,

		`CREATE INDEX IF NOT EXISTS idx_bug_reports_created_at
		 ON bug_reports (created_at DESC)`,
	}

	for _, q := range migrations {
		if _, err := database.Exec(q); err != nil {
			log.Fatalf("Migration failed:\n%s\nError: %v", q, err)
		}
	}

	log.Println("Database migrations completed successfully")
}
