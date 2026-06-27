package service

import (
	"context"

	"kasir-cafe/backend/internal/domain"
	"kasir-cafe/backend/internal/repository"
)

type ProductService interface {
	List(ctx context.Context) ([]domain.Product, error)
	Create(ctx context.Context, product domain.Product) (domain.Product, error)
	Update(ctx context.Context, id int64, product domain.Product) (domain.Product, error)
	Delete(ctx context.Context, id int64) error
	LowStock(ctx context.Context, threshold int) ([]domain.Product, error)
}

type productService struct {
	repo repository.ProductRepository
}

func NewProductService(repo repository.ProductRepository) ProductService {
	return &productService{repo: repo}
}

func (s *productService) List(ctx context.Context) ([]domain.Product, error) {
	return s.repo.List(ctx)
}

func (s *productService) Create(ctx context.Context, product domain.Product) (domain.Product, error) {
	return s.repo.Create(ctx, product)
}

func (s *productService) Update(ctx context.Context, id int64, product domain.Product) (domain.Product, error) {
	return s.repo.Update(ctx, id, product)
}

func (s *productService) Delete(ctx context.Context, id int64) error {
	return s.repo.Delete(ctx, id)
}

func (s *productService) LowStock(ctx context.Context, threshold int) ([]domain.Product, error) {
	products, err := s.repo.List(ctx)
	if err != nil {
		return nil, err
	}

	lowStock := make([]domain.Product, 0)
	for _, product := range products {
		if product.Stok <= threshold {
			lowStock = append(lowStock, product)
		}
	}

	return lowStock, nil
}
