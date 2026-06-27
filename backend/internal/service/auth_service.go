package service

import (
	"context"
	"database/sql"
	"errors"
	"time"

	"github.com/golang-jwt/jwt/v5"
	"golang.org/x/crypto/bcrypt"
	"kasir-cafe/backend/internal/repository"
)

type AuthService interface {
	Login(ctx context.Context, username string, password string) (string, string, error)
	SeedDefaultAdmin(ctx context.Context) error
	ValidateToken(token string) (string, string, error)
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

	claims := jwt.MapClaims{
		"sub":  user.Username,
		"role": user.Role,
		"iat":  time.Now().Unix(),
		"exp":  time.Now().Add(24 * time.Hour).Unix(),
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	signed, err := token.SignedString([]byte(s.jwtSecret))
	if err != nil {
		return "", "", err
	}

	return signed, user.Role, nil
}

func (s *authService) SeedDefaultAdmin(ctx context.Context) error {
	return s.userRepo.SeedDefaultAdmin(ctx)
}

func (s *authService) ValidateToken(token string) (string, string, error) {
	parsed, err := jwt.Parse(token, func(t *jwt.Token) (interface{}, error) {
		if _, ok := t.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, errors.New("metode signing token tidak valid")
		}
		return []byte(s.jwtSecret), nil
	})
	if err != nil || !parsed.Valid {
		return "", "", errors.New("token tidak valid")
	}

	claims, ok := parsed.Claims.(jwt.MapClaims)
	if !ok {
		return "", "", errors.New("claims token tidak valid")
	}

	username, _ := claims["sub"].(string)
	role, _ := claims["role"].(string)
	if username == "" || role == "" {
		return "", "", errors.New("token tidak valid")
	}

	return username, role, nil
}
