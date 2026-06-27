package repository

import (
	"context"
	"database/sql"

	"kasir-cafe/backend/internal/domain"
)

type ProductRepository interface {
	List(ctx context.Context) ([]domain.Product, error)
	Create(ctx context.Context, product domain.Product) (domain.Product, error)
	Update(ctx context.Context, id int64, product domain.Product) (domain.Product, error)
	Delete(ctx context.Context, id int64) error
	GetByID(ctx context.Context, id int64) (domain.Product, error)
	UpdateStock(ctx context.Context, id int64, stock int) error
}

type productRepository struct {
	db *sql.DB
}

func NewProductRepository(db *sql.DB) ProductRepository {
	return &productRepository{db: db}
}

func (r *productRepository) List(ctx context.Context) ([]domain.Product, error) {
	rows, err := r.db.QueryContext(ctx, `
		SELECT id, nama, barcode, kategori, harga_beli, harga_jual, stok, COALESCE(foto, ''), created_at, updated_at
		FROM products
		ORDER BY id DESC`)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	products := make([]domain.Product, 0)
	for rows.Next() {
		var p domain.Product
		if err := rows.Scan(&p.ID, &p.Nama, &p.Barcode, &p.Kategori, &p.HargaBeli, &p.HargaJual, &p.Stok, &p.Foto, &p.CreatedAt, &p.UpdatedAt); err != nil {
			return nil, err
		}
		products = append(products, p)
	}

	return products, rows.Err()
}

func (r *productRepository) Create(ctx context.Context, product domain.Product) (domain.Product, error) {
	err := r.db.QueryRowContext(ctx, `
		INSERT INTO products (nama, barcode, kategori, harga_beli, harga_jual, stok, foto)
		VALUES ($1, $2, $3, $4, $5, $6, $7)
		RETURNING id, created_at, updated_at`,
		product.Nama,
		product.Barcode,
		product.Kategori,
		product.HargaBeli,
		product.HargaJual,
		product.Stok,
		product.Foto,
	).Scan(&product.ID, &product.CreatedAt, &product.UpdatedAt)

	return product, err
}

func (r *productRepository) Update(ctx context.Context, id int64, product domain.Product) (domain.Product, error) {
	err := r.db.QueryRowContext(ctx, `
		UPDATE products
		SET nama=$1, barcode=$2, kategori=$3, harga_beli=$4, harga_jual=$5, stok=$6, foto=$7, updated_at=NOW()
		WHERE id=$8
		RETURNING id, created_at, updated_at`,
		product.Nama,
		product.Barcode,
		product.Kategori,
		product.HargaBeli,
		product.HargaJual,
		product.Stok,
		product.Foto,
		id,
	).Scan(&product.ID, &product.CreatedAt, &product.UpdatedAt)

	return product, err
}

func (r *productRepository) Delete(ctx context.Context, id int64) error {
	_, err := r.db.ExecContext(ctx, `DELETE FROM products WHERE id = $1`, id)
	return err
}

func (r *productRepository) GetByID(ctx context.Context, id int64) (domain.Product, error) {
	var p domain.Product
	err := r.db.QueryRowContext(ctx, `
		SELECT id, nama, barcode, kategori, harga_beli, harga_jual, stok, COALESCE(foto, ''), created_at, updated_at
		FROM products WHERE id = $1`, id,
	).Scan(&p.ID, &p.Nama, &p.Barcode, &p.Kategori, &p.HargaBeli, &p.HargaJual, &p.Stok, &p.Foto, &p.CreatedAt, &p.UpdatedAt)

	return p, err
}

func (r *productRepository) UpdateStock(ctx context.Context, id int64, stock int) error {
	_, err := r.db.ExecContext(ctx, `UPDATE products SET stok = $1, updated_at = NOW() WHERE id = $2`, stock, id)
	return err
}
