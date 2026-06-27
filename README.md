# Kasir Cafe POS (2026)

Monorepo starter untuk aplikasi POS modern dengan mode offline + online.

## Stack
- Android: Kotlin, Jetpack Compose, Material 3, MVVM, Room, Hilt, Navigation Compose, Coroutines/Flow, DataStore
- Backend API: Go + Gin
- Database Server: PostgreSQL
- Dashboard Admin: React + TypeScript + Vite
- Cloud (opsional): Firebase atau Supabase

## Struktur Repo
```
Kasir-Cafe
|- android/                  # Aplikasi Android POS
|- backend/                  # REST API (Go + Gin)
|- dashboard/                # Dashboard admin web (React + TS)
|- docs/                     # Dokumen pendukung
`- README.md
```

## Fitur Utama (Roadmap)
1. v1.0: Login, Dashboard, Produk, Kasir, Transaksi, Laporan dasar
2. v1.5: Supplier, Pembelian barang, Pelanggan, Barcode, Bluetooth printer
3. v2.0: Sinkronisasi cloud, Multi cabang, Web dashboard, Analitik, Backup otomatis, QRIS

## Menjalankan Project

### 1) Backend
```
cd backend
go run ./cmd/api
```
API default di `http://localhost:8080`.

### 2) Dashboard
```
cd dashboard
npm install
npm run dev
```
Dashboard default di `http://localhost:5173`.

### 3) Android
1. Buka folder `android/` di Android Studio.
2. Sync Gradle.
3. Jalankan app di emulator/device Android 10+.

## Contoh Endpoint API
- `POST /login`
- `POST /refresh-token` (Bearer token)
- `POST /logout` (Bearer token)
- `GET /produk`
- `POST /produk`
- `PUT /produk/:id`
- `DELETE /produk/:id`
- `GET /transaksi`
- `POST /transaksi`
- `GET /laporan`
- `GET /stok`
- `POST /supplier`
- `GET /admin/auth-audit-logs` (admin, filter: `event`, `username`, `date_from`, `date_to`, `page`, `limit`)
- `GET /admin/auth-audit-logs/export` (admin, export CSV sesuai filter: `event`, `username`, `date_from`, `date_to`)
- `GET /admin/auth-audit-summary` (admin, ringkasan observability, param `days`, default 30)

## Catatan Arsitektur Android
```
UI -> ViewModel -> Repository -> API/Room -> Database
```

Folder Android mengikuti pola:
```
app/
data/{repository,database,api}
domain/{model,usecase}
presentation/{login,dashboard,kasir,laporan,produk,pelanggan}
ui/
di/
util/
```

## RBAC Backend
- `admin`: akses penuh, termasuk create/update/delete produk dan create supplier.
- `kasir`: baca data, transaksi, laporan, stok, refresh token, logout.

## Session Lifecycle Backend
- Logout dan refresh token menyimpan token lama ke tabel `revoked_tokens` di PostgreSQL.
- Revocation tidak hilang saat backend restart.
- Cleanup token expired memakai job terpisah (production-friendly): scheduler external/pg_cron.

## Cleanup Job (Production)
### Opsi 1: Cron/Scheduler eksternal
Jalankan job one-shot:

```
./scripts/run-cleanup-job.sh
```

Contoh cron setiap 10 menit:

```
*/10 * * * * cd /workspaces/Kasir-Cafe && ./scripts/run-cleanup-job.sh >> /tmp/kasir-cleanup.log 2>&1
```

### Opsi 2: pg_cron (jika tersedia)
Jalankan SQL setup:

```
psql -U postgres -d kasir_cafe -f backend/sql/pg_cron_setup.sql
```

File: `backend/sql/pg_cron_setup.sql`.

## Audit Log Auth
Event auth `login`, `refresh`, `logout` (berhasil/gagal) dicatat ke tabel `auth_audit_logs`.
Kolom penting: `username`, `role`, `success`, `ip_address`, `user_agent`, `detail`, `created_at`.

### Export CSV
`GET /admin/auth-audit-logs/export` mengembalikan seluruh baris (sesuai filter) sebagai file CSV
(`Content-Type: text/csv`, header `Content-Disposition: attachment`).

### Summary Observability
`GET /admin/auth-audit-summary?days=30` mengembalikan agregat: total event/login/failed login/logout/refresh,
`failed_login_per_day`, dan `top_ip_addresses` (top 5). Ditampilkan sebagai kartu + bar chart di dashboard admin.

### Retention Policy
Audit log lebih lama dari `AUDIT_RETENTION_DAYS` (default 180 hari) dihapus otomatis:
- saat startup API (`cmd/api`), dan
- pada cleanup job (`cmd/cleanup` / `./scripts/run-cleanup-job.sh`).

Konfigurasi via env `AUDIT_RETENTION_DAYS` (mis. `AUDIT_RETENTION_DAYS=90`).
Untuk pg_cron, lihat `backend/sql/pg_cron_setup.sql`.

## Jalankan End-to-End Sekali Jalan
Pastikan Docker aktif, lalu:

```
./scripts/run-e2e.sh
```

Script akan:
1. Menyalakan PostgreSQL dari `docker-compose.yml`.
2. Menjalankan migration `backend/sql/schema.sql`.
3. Menjalankan seed `backend/sql/seed.sql`.
4. Menjalankan backend API di port `8080`.