<div align="center">

# ☕ Kasir Cafe POS

**Aplikasi Point of Sale modern dengan mode _offline_ + _online_ — Android, Web Dashboard, & REST API dalam satu monorepo.**

![Android](https://img.shields.io/badge/Android-Kotlin%20%2B%20Compose-3DDC84?logo=android&logoColor=white)
![Backend](https://img.shields.io/badge/Backend-Go%20%2B%20Gin-00ADD8?logo=go&logoColor=white)
![Dashboard](https://img.shields.io/badge/Dashboard-React%20%2B%20TS-61DAFB?logo=react&logoColor=black)
![Database](https://img.shields.io/badge/Database-PostgreSQL-4169E1?logo=postgresql&logoColor=white)
![Auth](https://img.shields.io/badge/Auth-JWT%20%2B%20RBAC-FF6F00?logo=jsonwebtokens&logoColor=white)

</div>

---

## 📑 Daftar Isi
- [✨ Sorotan](#-sorotan)
- [🧱 Stack](#-stack)
- [🗂️ Struktur Repo](#️-struktur-repo)
- [🗺️ Roadmap](#️-roadmap)
- [🚀 Menjalankan Project](#-menjalankan-project)
- [⚡ Jalankan End-to-End Sekali Jalan](#-jalankan-end-to-end-sekali-jalan)
- [🔌 Endpoint API](#-endpoint-api)
- [📱 Arsitektur Android](#-arsitektur-android)
- [🔐 RBAC Backend](#-rbac-backend)
- [♻️ Session Lifecycle & Cleanup Job](#️-session-lifecycle--cleanup-job)
- [🧾 Audit Log & Observability](#-audit-log--observability)

---

## ✨ Sorotan
- 🔄 **Offline-first** — transaksi tetap jalan tanpa koneksi, lalu disinkronkan otomatis.
- 🔐 **Keamanan** — JWT + RBAC, token revocation persisten, audit log auth.
- 📊 **Observability** — ringkasan login gagal, top IP, export CSV, retention otomatis.
- 🧩 **Clean Architecture** — pemisahan jelas UI → ViewModel → UseCase → Repository → Data.

---

## 🧱 Stack
| Lapisan | Teknologi |
| --- | --- |
| 📱 **Android** | Kotlin, Jetpack Compose, Material 3, MVVM, Room, Hilt, Navigation Compose, Coroutines/Flow, DataStore |
| ⚙️ **Backend API** | Go + Gin |
| 🗄️ **Database** | PostgreSQL |
| 💻 **Dashboard Admin** | React + TypeScript + Vite |
| ☁️ **Cloud (opsional)** | Firebase atau Supabase |

---

## 🗂️ Struktur Repo
```
Kasir-Cafe
├─ android/                  # Aplikasi Android POS
├─ backend/                  # REST API (Go + Gin)
├─ dashboard/                # Dashboard admin web (React + TS)
├─ docs/                     # Dokumen pendukung
└─ README.md
```

---

## 🗺️ Roadmap
| Versi | Fitur |
| --- | --- |
| **v1.0** | Login, Dashboard, Produk, Kasir, Transaksi, Laporan dasar |
| **v1.5** | Supplier, Pembelian barang, Pelanggan, Barcode, Bluetooth printer |
| **v2.0** | Sinkronisasi cloud, Multi cabang, Web dashboard, Analitik, Backup otomatis, QRIS |

---

## 🚀 Menjalankan Project

### ⚙️ 1) Backend
```bash
cd backend
go run ./cmd/api
```
> API default di `http://localhost:8080`.

### 💻 2) Dashboard
```bash
cd dashboard
npm install
npm run dev
```
> Dashboard default di `http://localhost:5173`.

### 📱 3) Android
1. Buka folder `android/` di Android Studio.
2. Sync Gradle.
3. Jalankan app di emulator/device Android 10+.

---

## ⚡ Jalankan End-to-End Sekali Jalan
Pastikan Docker aktif, lalu:

```bash
./scripts/run-e2e.sh
```

Script akan:
1. 🐘 Menyalakan PostgreSQL dari `docker-compose.yml`.
2. 🧱 Menjalankan migration `backend/sql/schema.sql`.
3. 🌱 Menjalankan seed `backend/sql/seed.sql`.
4. 🚀 Menjalankan backend API di port `8080`.

---

## 🔌 Endpoint API

### 🔑 Auth
| Method | Endpoint | Keterangan |
| --- | --- | --- |
| `POST` | `/login` | Login, mengembalikan token + role |
| `POST` | `/refresh-token` | Perpanjang sesi (Bearer token) |
| `POST` | `/logout` | Akhiri sesi (Bearer token) |

### 📦 Produk & Transaksi
| Method | Endpoint | Akses | Keterangan |
| --- | --- | --- | --- |
| `GET` | `/produk` | semua | Daftar produk |
| `POST` | `/produk` | admin | Tambah produk |
| `PUT` | `/produk/:id` | admin | Ubah produk |
| `DELETE` | `/produk/:id` | admin | Hapus produk |
| `GET` | `/transaksi` | semua | Daftar transaksi |
| `POST` | `/transaksi` | semua | Buat transaksi |
| `GET` | `/laporan` | semua | Laporan penjualan |
| `GET` | `/stok` | semua | Stok menipis |
| `POST` | `/supplier` | admin | Tambah supplier |

### 🧾 Admin · Audit & Observability
| Method | Endpoint | Keterangan |
| --- | --- | --- |
| `GET` | `/admin/auth-audit-logs` | Filter: `event`, `username`, `date_from`, `date_to`, `page`, `limit` |
| `GET` | `/admin/auth-audit-logs/export` | Export CSV sesuai filter |
| `GET` | `/admin/auth-audit-summary` | Ringkasan observability (param `days`, default 30) |

---

## 📱 Arsitektur Android
```
UI → ViewModel → UseCase → Repository → API/Room → Database
```

Folder Android mengikuti pola:
```
app/
├─ data/{repository,database,api}
├─ domain/{model,usecase}
├─ presentation/{login,dashboard,kasir,laporan,produk,pelanggan}
├─ ui/
├─ di/
└─ util/
```

---

## 🔐 RBAC Backend
| Role | Hak Akses |
| --- | --- |
| 👑 **admin** | Akses penuh, termasuk create/update/delete produk dan create supplier |
| 🧑‍💼 **kasir** | Baca data, transaksi, laporan, stok, refresh token, logout |

---

## ♻️ Session Lifecycle & Cleanup Job

**Session Lifecycle**
- Logout dan refresh token menyimpan token lama ke tabel `revoked_tokens` di PostgreSQL.
- Revocation tidak hilang saat backend restart.
- Cleanup token expired memakai job terpisah (production-friendly): scheduler external/pg_cron.

**Opsi 1 — Cron/Scheduler eksternal**
```bash
./scripts/run-cleanup-job.sh
```
Contoh cron setiap 10 menit:
```bash
*/10 * * * * cd /workspaces/Kasir-Cafe && ./scripts/run-cleanup-job.sh >> /tmp/kasir-cleanup.log 2>&1
```

**Opsi 2 — pg_cron (jika tersedia)**
```bash
psql -U postgres -d kasir_cafe -f backend/sql/pg_cron_setup.sql
```
> File: `backend/sql/pg_cron_setup.sql`.

---

## 🧾 Audit Log & Observability
Event auth `login`, `refresh`, `logout` (berhasil/gagal) dicatat ke tabel `auth_audit_logs`.
Kolom penting: `username`, `role`, `success`, `ip_address`, `user_agent`, `detail`, `created_at`.

### 📤 Export CSV
`GET /admin/auth-audit-logs/export` mengembalikan seluruh baris (sesuai filter) sebagai file CSV
(`Content-Type: text/csv`, header `Content-Disposition: attachment`).

### 📊 Summary Observability
`GET /admin/auth-audit-summary?days=30` mengembalikan agregat: total event/login/failed login/logout/refresh,
`failed_login_per_day`, dan `top_ip_addresses` (top 5). Ditampilkan sebagai kartu + bar chart di dashboard admin.

### 🗑️ Retention Policy
Audit log lebih lama dari `AUDIT_RETENTION_DAYS` (default 180 hari) dihapus otomatis:
- saat startup API (`cmd/api`), dan
- pada cleanup job (`cmd/cleanup` / `./scripts/run-cleanup-job.sh`).

Konfigurasi via env `AUDIT_RETENTION_DAYS` (mis. `AUDIT_RETENTION_DAYS=90`).
Untuk pg_cron, lihat `backend/sql/pg_cron_setup.sql`.

---

<div align="center">

Dibuat dengan ❤️ untuk UMKM & kafe modern.

</div>
