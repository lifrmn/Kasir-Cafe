package service

import (
	"context"
	"database/sql"
	"errors"
	"fmt"
	"time"

	"golang.org/x/crypto/bcrypt"
	"kasir-cafe/backend/internal/repository"
)

type AuthService interface {
	Login(ctx context.Context, username string, password string) (string, string, error)
	SeedDefaultAdmin(ctx context.Context) error
}

type authService struct {
	userRepo  repository.UserRepository
	jwtSecret string
}

func NewAuthService(userRepo repository.UserRepository, jwtSecret string) AuthService {
	return &authService{userRepo: userRepo, jwtSecret: jwtSecret}
}

func (s *authService) Login(ctx context.Context, username string, password string) (string, string, error) {
	user, err := s.userRepo.FindByUsername(ctx, username)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return "", "", errors.New("username atau password salah")
		}
		return "", "", err
	}

	if err := bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(password)); err != nil {
		return "", "", errors.New("username atau password salah")
	}

	// Token format sengaja sederhana untuk starter project.
	token := fmt.Sprintf("%s:%s:%d", user.Username, s.jwtSecret, time.Now().Unix())
	return token, user.Role, nil
}

func (s *authService) SeedDefaultAdmin(ctx context.Context) error {
	return s.userRepo.SeedDefaultAdmin(ctx)
}
