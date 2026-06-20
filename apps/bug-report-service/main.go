package main

import (
	"log"
	"net/http"

	"bug-report-service/internal/config"
	"bug-report-service/internal/db"
	"bug-report-service/internal/handlers"
	"bug-report-service/internal/metrics"
)

func main() {
	cfg := config.Load()

	database := db.Bootstrap(cfg)
	db.RunMigrations(database)

	mux := http.NewServeMux()
	mux.Handle("GET /metrics", metrics.Handler())
	h := handlers.New(database, cfg)
	h.Register(mux)

	log.Printf("Bug report service starting on port %s", cfg.Port)
	if err := http.ListenAndServe(":"+cfg.Port, mux); err != nil {
		log.Fatalf("Server failed: %v", err)
	}
}
