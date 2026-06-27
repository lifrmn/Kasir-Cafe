CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    nama TEXT NOT NULL,
    barcode TEXT NOT NULL,
    kategori TEXT NOT NULL,
    harga_beli BIGINT NOT NULL,
    harga_jual BIGINT NOT NULL,
    stok INTEGER NOT NULL DEFAULT 0,
    foto TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    tanggal TIMESTAMP NOT NULL,
    total BIGINT NOT NULL,
    diskon BIGINT NOT NULL DEFAULT 0,
    pajak BIGINT NOT NULL DEFAULT 0,
    grand_total BIGINT NOT NULL,
    metode_pembayaran TEXT NOT NULL,
    kasir TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS transaction_details (
    id BIGSERIAL PRIMARY KEY,
    transaksi_id BIGINT NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    produk_id BIGINT NOT NULL REFERENCES products(id),
    jumlah INTEGER NOT NULL,
    harga BIGINT NOT NULL,
    subtotal BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS customers (
    id BIGSERIAL PRIMARY KEY,
    nama TEXT NOT NULL,
    telepon TEXT,
    alamat TEXT,
    poin INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS suppliers (
    id BIGSERIAL PRIMARY KEY,
    nama TEXT NOT NULL,
    telepon TEXT,
    alamat TEXT
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    role TEXT NOT NULL CHECK (role IN ('admin', 'kasir'))
);
