package repository

import (
	"context"
	"database/sql"

	"golang.org/x/crypto/bcrypt"
	"kasir-cafe/backend/internal/domain"
)

type UserRepository interface {
	FindByUsername(ctx context.Context, username string) (domain.User, error)
	SeedDefaultAdmin(ctx context.Context) error
}

type userRepository struct {
	db *sql.DB
}

func NewUserRepository(db *sql.DB) UserRepository {
	return &userRepository{db: db}
}

func (r *userRepository) FindByUsername(ctx context.Context, username string) (domain.User, error) {
	var user domain.User
	err := r.db.QueryRowContext(ctx, `
		SELECT id, username, password, role
		FROM users
		WHERE username=$1`, username,
	).Scan(&user.ID, &user.Username, &user.Password, &user.Role)

	return user, err
}

func (r *userRepository) SeedDefaultAdmin(ctx context.Context) error {
	var count int
	if err := r.db.QueryRowContext(ctx, `SELECT COUNT(1) FROM users`).Scan(&count); err != nil {
		return err
	}

	if count > 0 {
		return nil
	}

	hashed, err := bcrypt.GenerateFromPassword([]byte("admin123"), bcrypt.DefaultCost)
	if err != nil {
		return err
	}

	_, err = r.db.ExecContext(ctx, `
		INSERT INTO users (username, password, role)
		VALUES ($1, $2, $3)`, "admin", string(hashed), "admin")

	return err
}
