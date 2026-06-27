package service

import (
	"context"
	"fmt"

	"kasir-cafe/backend/internal/domain"
	"kasir-cafe/backend/internal/repository"
)

type TransactionService interface {
	List(ctx context.Context) ([]domain.Transaction, error)
	Create(ctx context.Context, txData domain.Transaction, details []domain.TransactionDetail) (domain.Transaction, error)
	Report(ctx context.Context) (map[string]map[string]int64, error)
}

type transactionService struct {
	txRepo      repository.TransactionRepository
	productRepo repository.ProductRepository
}

func NewTransactionService(
	txRepo repository.TransactionRepository,
	productRepo repository.ProductRepository,
) TransactionService {
	return &transactionService{
		txRepo:      txRepo,
		productRepo: productRepo,
	}
}

func (s *transactionService) List(ctx context.Context) ([]domain.Transaction, error) {
	return s.txRepo.List(ctx)
}

func (s *transactionService) Create(ctx context.Context, txData domain.Transaction, details []domain.TransactionDetail) (domain.Transaction, error) {
	for _, detail := range details {
		product, err := s.productRepo.GetByID(ctx, detail.ProdukID)
		if err != nil {
			return domain.Transaction{}, err
		}

		if product.Stok < detail.Jumlah {
			return domain.Transaction{}, fmt.Errorf("stok produk %s tidak mencukupi", product.Nama)
		}
	}

	return s.txRepo.Create(ctx, txData, details)
}

func (s *transactionService) Report(ctx context.Context) (map[string]map[string]int64, error) {
	transactions, err := s.txRepo.List(ctx)
	if err != nil {
		return nil, err
	}

	var totalPenjualan int64
	var totalProfit int64
	for _, transaction := range transactions {
		totalPenjualan += transaction.GrandTotal
		totalProfit += transaction.GrandTotal - transaction.Total
	}

	result := map[string]map[string]int64{
		"harian": {
			"total_penjualan": totalPenjualan,
			"transaksi":       int64(len(transactions)),
			"profit":          totalProfit,
		},
		"mingguan": {
			"total_penjualan": totalPenjualan,
			"transaksi":       int64(len(transactions)),
			"profit":          totalProfit,
		},
		"bulanan": {
			"total_penjualan": totalPenjualan,
			"transaksi":       int64(len(transactions)),
			"profit":          totalProfit,
		},
		"tahunan": {
			"total_penjualan": totalPenjualan,
			"transaksi":       int64(len(transactions)),
			"profit":          totalProfit,
		},
	}

	return result, nil
}
