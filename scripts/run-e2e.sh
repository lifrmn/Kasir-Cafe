#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "[1/5] Menyalakan PostgreSQL via docker compose..."
docker compose up -d postgres

echo "[2/5] Menunggu PostgreSQL siap..."
until docker compose exec -T postgres pg_isready -U postgres -d kasir_cafe >/dev/null 2>&1; do
  printf "."
  sleep 1
done
echo

echo "[3/5] Menjalankan migration schema..."
docker compose exec -T postgres psql -U postgres -d kasir_cafe -f /workspaces/Kasir-Cafe/backend/sql/schema.sql

echo "[4/5] Menjalankan seed data..."
docker compose exec -T postgres psql -U postgres -d kasir_cafe -f /workspaces/Kasir-Cafe/backend/sql/seed.sql

echo "[5/5] Menjalankan backend API..."
cd "$ROOT_DIR/backend"
export DATABASE_URL="postgres://postgres:postgres@localhost:5432/kasir_cafe?sslmode=disable"
export PORT="8080"
export JWT_SECRET="super-secret-change-this"
go run ./cmd/api
