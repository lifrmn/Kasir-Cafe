#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR/backend"

export DATABASE_URL="${DATABASE_URL:-postgres://postgres:postgres@localhost:5432/kasir_cafe?sslmode=disable}"
export JWT_SECRET="${JWT_SECRET:-super-secret-change-this}"

# One-shot cleanup job, cocok dipanggil scheduler external (cron/systemd timer).
go run ./cmd/cleanup
