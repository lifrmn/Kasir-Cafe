package domain

import "time"

type Product struct {
	ID        int64     `json:"id"`
	Nama      string    `json:"nama"`
	Barcode   string    `json:"barcode"`
	Kategori  string    `json:"kategori"`
	HargaBeli int64     `json:"harga_beli"`
	HargaJual int64     `json:"harga_jual"`
	Stok      int       `json:"stok"`
	Foto      string    `json:"foto"`
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`
}

type Transaction struct {
	ID               int64     `json:"id"`
	Tanggal          time.Time `json:"tanggal"`
	Total            int64     `json:"total"`
	Diskon           int64     `json:"diskon"`
	Pajak            int64     `json:"pajak"`
	GrandTotal       int64     `json:"grand_total"`
	MetodePembayaran string    `json:"metode_pembayaran"`
	Kasir            string    `json:"kasir"`
}

type TransactionDetail struct {
	ProdukID int64 `json:"produk_id"`
	Jumlah   int   `json:"jumlah"`
	Harga    int64 `json:"harga"`
	Subtotal int64 `json:"subtotal"`
}

type User struct {
	ID       int64  `json:"id"`
	Username string `json:"username"`
	Password string `json:"password"`
	Role     string `json:"role"`
}
