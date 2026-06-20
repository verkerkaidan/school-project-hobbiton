package main

import (
	"log"
	"net/http"

	"notification-service/internal/config"
	"notification-service/internal/db"
	"notification-service/internal/handlers"
	"notification-service/internal/hub"
	"notification-service/internal/metrics"
)

func main() {
	cfg := config.Load()

	database := db.Connect(cfg)
	db.RunMigrations(database)

	wsHub := hub.NewHub()
	go wsHub.Run()

	mux := http.NewServeMux()
	mux.Handle("GET /metrics", metrics.Handler())
	h := handlers.New(database, wsHub, cfg)
	h.Register(mux)

	log.Printf("Notification service starting on port %s", cfg.Port)
	if err := http.ListenAndServe(":"+cfg.Port, mux); err != nil {
		log.Fatalf("Server failed: %v", err)
	}
}
