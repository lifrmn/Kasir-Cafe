# Project Spec - Kasir Cafe POS

## Tujuan
Aplikasi POS Android untuk toko kelontong, minimarket, cafe, warung, barbershop, laundry, dan UMKM.

## Platform dan Mode
- Android 10+
- Offline first (Room)
- Online sync (REST API + PostgreSQL)

## Modul Android
1. Login (Admin/Kasir)
2. Dashboard (penjualan, transaksi, produk terjual, profit)
3. Produk (CRUD, kategori, barcode, foto, stok)
4. Kasir (scan, cart, diskon, voucher, pajak, payment)
5. Pelanggan (member, poin, riwayat)
6. Supplier
7. Pembelian barang
8. Manajemen stok dan opname
9. Laporan (harian-mingguan-bulanan-tahunan)
10. Pengaturan toko

## Skema Data Utama
- products
- transactions
- transaction_details
- customers
- suppliers
- users

SQL starter ada di `backend/sql/schema.sql`.

## Kontrak API Starter
- POST /login
- GET /produk
- POST /produk
- PUT /produk/:id
- DELETE /produk/:id
- GET /transaksi
- POST /transaksi
- GET /laporan
- GET /stok
- POST /supplier

## Arsitektur Android
UI -> ViewModel -> Repository -> API/Room -> Database

## Fitur Premium (Target)
- Sinkronisasi cloud
- Multi device
- Backup otomatis
- Multi cabang
- QRIS
- Bluetooth printer
- Barcode scanner
- Dark mode
- Export PDF/Excel
- Notifikasi stok habis
