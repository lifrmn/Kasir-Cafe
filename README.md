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
- `GET /produk`
- `POST /produk`
- `PUT /produk/:id`
- `DELETE /produk/:id`
- `GET /transaksi`
- `POST /transaksi`
- `GET /laporan`
- `GET /stok`
- `POST /supplier`

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