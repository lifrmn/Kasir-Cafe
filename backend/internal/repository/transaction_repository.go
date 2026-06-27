package repository

import (
	"context"
	"database/sql"

	"kasir-cafe/backend/internal/domain"
)

type TransactionRepository interface {
	List(ctx context.Context) ([]domain.Transaction, error)
	Create(ctx context.Context, txData domain.Transaction, details []domain.TransactionDetail) (domain.Transaction, error)
}

type transactionRepository struct {
	db *sql.DB
}

func NewTransactionRepository(db *sql.DB) TransactionRepository {
	return &transactionRepository{db: db}
}

func (r *transactionRepository) List(ctx context.Context) ([]domain.Transaction, error) {
	rows, err := r.db.QueryContext(ctx, `
		SELECT id, tanggal, total, diskon, pajak, grand_total, metode_pembayaran, kasir
		FROM transactions
		ORDER BY id DESC`)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	transactions := make([]domain.Transaction, 0)
	for rows.Next() {
		var t domain.Transaction
		if err := rows.Scan(&t.ID, &t.Tanggal, &t.Total, &t.Diskon, &t.Pajak, &t.GrandTotal, &t.MetodePembayaran, &t.Kasir); err != nil {
			return nil, err
		}
		transactions = append(transactions, t)
	}

	return transactions, rows.Err()
}

func (r *transactionRepository) Create(ctx context.Context, txData domain.Transaction, details []domain.TransactionDetail) (domain.Transaction, error) {
	dbTx, err := r.db.BeginTx(ctx, nil)
	if err != nil {
		return domain.Transaction{}, err
	}

	defer func() {
		if err != nil {
			_ = dbTx.Rollback()
		}
	}()

	err = dbTx.QueryRowContext(ctx, `
		INSERT INTO transactions (tanggal, total, diskon, pajak, grand_total, metode_pembayaran, kasir)
		VALUES (NOW(), $1, $2, $3, $4, $5, $6)
		RETURNING id, tanggal`,
		txData.Total,
		txData.Diskon,
		txData.Pajak,
		txData.GrandTotal,
		txData.MetodePembayaran,
		txData.Kasir,
	).Scan(&txData.ID, &txData.Tanggal)
	if err != nil {
		return domain.Transaction{}, err
	}

	for _, detail := range details {
		if _, err = dbTx.ExecContext(ctx, `
			INSERT INTO transaction_details (transaksi_id, produk_id, jumlah, harga, subtotal)
			VALUES ($1, $2, $3, $4, $5)`,
			txData.ID,
			detail.ProdukID,
			detail.Jumlah,
			detail.Harga,
			detail.Subtotal,
		); err != nil {
			return domain.Transaction{}, err
		}

		if _, err = dbTx.ExecContext(ctx, `
			UPDATE products
			SET stok = stok - $1, updated_at = NOW()
			WHERE id = $2`, detail.Jumlah, detail.ProdukID); err != nil {
			return domain.Transaction{}, err
		}
	}

	if err = dbTx.Commit(); err != nil {
		return domain.Transaction{}, err
	}

	return txData, nil
}
