package main

import (
	"context"
	"database/sql"
	"log"

	"kasir-cafe/backend/internal/auth"
	"kasir-cafe/backend/internal/config"
	"kasir-cafe/backend/internal/db"
	"kasir-cafe/backend/internal/handler"
	"kasir-cafe/backend/internal/repository"
	"kasir-cafe/backend/internal/router"
	"kasir-cafe/backend/internal/service"
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

	if err := db.Migrate(database, "sql/schema.sql"); err != nil {
		log.Fatalf("run migration: %v", err)
	}

	productRepo := repository.NewProductRepository(database)
	transactionRepo := repository.NewTransactionRepository(database)
	userRepo := repository.NewUserRepository(database)
	authAuditRepo := repository.NewAuthAuditRepository(database)
	tokenStore := auth.NewPostgresTokenStore(database)
	if err := tokenStore.CleanupExpired(); err != nil {
		log.Printf("cleanup revoked token startup warning: %v", err)
	}
	if deleted, err := authAuditRepo.DeleteOlderThan(context.Background(), cfg.AuditRetentionDays); err != nil {
		log.Printf("retention audit log startup warning: %v", err)
	} else if deleted > 0 {
		log.Printf("retention audit log startup: %d baris dihapus (> %d hari)", deleted, cfg.AuditRetentionDays)
	}

	productService := service.NewProductService(productRepo)
	transactionService := service.NewTransactionService(transactionRepo, productRepo)
	authService := service.NewAuthService(userRepo, cfg.JWTSecret, tokenStore)
	if err := authService.SeedDefaultAdmin(context.Background()); err != nil {
		log.Printf("seed default admin warning: %v", err)
	}

	h := handler.NewHandler(productService, transactionService, authService, authAuditRepo)
	r := router.New(h, authService)

	log.Printf("API running on :%s", cfg.Port)
	if err := r.Run(":" + cfg.Port); err != nil {
		log.Fatalf("run server: %v", err)
	}
}
