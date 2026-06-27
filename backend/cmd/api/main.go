package main

import (
	"context"
	"database/sql"
	"log"

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

	productService := service.NewProductService(productRepo)
	transactionService := service.NewTransactionService(transactionRepo, productRepo)
	authService := service.NewAuthService(userRepo, cfg.JWTSecret)
	if err := authService.SeedDefaultAdmin(context.Background()); err != nil {
		log.Printf("seed default admin warning: %v", err)
	}

	h := handler.NewHandler(productService, transactionService, authService)
	r := router.New(h)

	log.Printf("API running on :%s", cfg.Port)
	if err := r.Run(":" + cfg.Port); err != nil {
		log.Fatalf("run server: %v", err)
	}
}
