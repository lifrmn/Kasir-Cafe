package config

import (
	"os"
	"strconv"
)

type Config struct {
	Port               string
	DatabaseURL        string
	JWTSecret          string
	AuditRetentionDays int
}

func Load() Config {
	port := os.Getenv("PORT")
	if port == "" {
		port = "8080"
	}

	databaseURL := os.Getenv("DATABASE_URL")
	if databaseURL == "" {
		databaseURL = "postgres://postgres:postgres@localhost:5432/kasir_cafe?sslmode=disable"
	}

	jwtSecret := os.Getenv("JWT_SECRET")
	if jwtSecret == "" {
		jwtSecret = "dev-secret"
	}

	auditRetentionDays := 180
	if raw := os.Getenv("AUDIT_RETENTION_DAYS"); raw != "" {
		if parsed, err := strconv.Atoi(raw); err == nil && parsed > 0 {
			auditRetentionDays = parsed
		}
	}

	return Config{
		Port:               port,
		DatabaseURL:        databaseURL,
		JWTSecret:          jwtSecret,
		AuditRetentionDays: auditRetentionDays,
	}
}
