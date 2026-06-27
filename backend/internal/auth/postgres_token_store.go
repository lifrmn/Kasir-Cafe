package auth

import (
	"crypto/sha256"
	"database/sql"
	"encoding/hex"
	"time"
)

type postgresTokenStore struct {
	db *sql.DB
}

func NewPostgresTokenStore(db *sql.DB) TokenStore {
	return &postgresTokenStore{db: db}
}

func (s *postgresTokenStore) Revoke(token string, expiresAt time.Time) error {
	_, err := s.db.Exec(`
		INSERT INTO revoked_tokens (token_hash, expires_at)
		VALUES ($1, $2)
		ON CONFLICT (token_hash) DO UPDATE SET expires_at = EXCLUDED.expires_at`,
		hashToken(token), expiresAt,
	)
	return err
}

func (s *postgresTokenStore) IsRevoked(token string) (bool, error) {
	var count int
	err := s.db.QueryRow(`
		SELECT COUNT(1)
		FROM revoked_tokens
		WHERE token_hash = $1 AND expires_at > NOW()`,
		hashToken(token),
	).Scan(&count)
	if err != nil {
		return false, err
	}
	return count > 0, nil
}

func (s *postgresTokenStore) CleanupExpired() error {
	_, err := s.db.Exec(`DELETE FROM revoked_tokens WHERE expires_at <= NOW()`)
	return err
}

func hashToken(token string) string {
	sum := sha256.Sum256([]byte(token))
	return hex.EncodeToString(sum[:])
}
