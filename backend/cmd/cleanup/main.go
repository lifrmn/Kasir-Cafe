package main

import (
	"database/sql"
	"log"

	"kasir-cafe/backend/internal/auth"
	"kasir-cafe/backend/internal/config"
	"kasir-cafe/backend/internal/db"
)

func main() {
	cfg := config.Load()

	database, err := db.Connect(cfg.DatabaseURL)
	if err != nil {
		log.Fatalf("connect database: %v", err)
	}
	defer func(database *sql.DB) {
		_ = database.Close()
	}(database)

	tokenStore := auth.NewPostgresTokenStore(database)
	if err := tokenStore.CleanupExpired(); err != nil {
		log.Fatalf("cleanup revoked token: %v", err)
	}

	log.Printf("cleanup revoked token selesai")
}
