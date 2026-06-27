package service

import (
	"context"
	"database/sql"
	"errors"
	"fmt"
	"time"

	"github.com/golang-jwt/jwt/v5"
	"golang.org/x/crypto/bcrypt"
	"kasir-cafe/backend/internal/auth"
	"kasir-cafe/backend/internal/repository"
)

type AuthService interface {
	Login(ctx context.Context, username string, password string) (string, string, error)
	SeedDefaultAdmin(ctx context.Context) error
	ValidateToken(token string) (string, string, error)
	RefreshToken(token string) (string, error)
	Logout(token string) error
}

type authService struct {
	userRepo   repository.UserRepository
	jwtSecret  string
	tokenStore auth.TokenStore
}

func NewAuthService(userRepo repository.UserRepository, jwtSecret string, tokenStore auth.TokenStore) AuthService {
	return &authService{userRepo: userRepo, jwtSecret: jwtSecret, tokenStore: tokenStore}
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
	parsed, err := s.parseToken(token)
	if err != nil || !parsed.Valid {
		return "", "", errors.New("token tidak valid")
	}

	revoked, err := s.tokenStore.IsRevoked(token)
	if err != nil {
		return "", "", fmt.Errorf("gagal verifikasi token: %w", err)
	}
	if revoked {
		return "", "", errors.New("token sudah tidak berlaku")
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

func (s *authService) RefreshToken(token string) (string, error) {
	username, role, err := s.ValidateToken(token)
	if err != nil {
		return "", err
	}

	expiresAt, err := s.tokenExpiry(token)
	if err != nil {
		return "", err
	}

	claims := jwt.MapClaims{
		"sub":  username,
		"role": role,
		"iat":  time.Now().Unix(),
		"exp":  time.Now().Add(24 * time.Hour).Unix(),
	}

	newToken := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	signed, err := newToken.SignedString([]byte(s.jwtSecret))
	if err != nil {
		return "", err
	}

	if err := s.tokenStore.Revoke(token, expiresAt); err != nil {
		return "", fmt.Errorf("gagal revoke token lama: %w", err)
	}
	return signed, nil
}

func (s *authService) Logout(token string) error {
	if _, _, err := s.ValidateToken(token); err != nil {
		return err
	}
	expiresAt, err := s.tokenExpiry(token)
	if err != nil {
		return err
	}
	if err := s.tokenStore.Revoke(token, expiresAt); err != nil {
		return fmt.Errorf("gagal revoke token: %w", err)
	}
	return nil
}

func (s *authService) parseToken(token string) (*jwt.Token, error) {
	return jwt.Parse(token, func(t *jwt.Token) (interface{}, error) {
		if _, ok := t.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, errors.New("metode signing token tidak valid")
		}
		return []byte(s.jwtSecret), nil
	})
}

func (s *authService) tokenExpiry(token string) (time.Time, error) {
	parsed, err := s.parseToken(token)
	if err != nil || !parsed.Valid {
		return time.Time{}, errors.New("token tidak valid")
	}
	claims, ok := parsed.Claims.(jwt.MapClaims)
	if !ok {
		return time.Time{}, errors.New("claims token tidak valid")
	}
	expRaw, ok := claims["exp"]
	if !ok {
		return time.Time{}, errors.New("exp token tidak ditemukan")
	}

	switch exp := expRaw.(type) {
	case float64:
		return time.Unix(int64(exp), 0), nil
	case int64:
		return time.Unix(exp, 0), nil
	case int:
		return time.Unix(int64(exp), 0), nil
	default:
		return time.Time{}, errors.New("format exp token tidak valid")
	}
}
