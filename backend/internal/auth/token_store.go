package auth

import "time"

import "sync"

type TokenStore interface {
	Revoke(token string, expiresAt time.Time) error
	IsRevoked(token string) (bool, error)
	CleanupExpired() error
}

type memoryTokenStore struct {
	mu      sync.RWMutex
	revoked map[string]struct{}
}

func NewMemoryTokenStore() TokenStore {
	return &memoryTokenStore{
		revoked: make(map[string]struct{}),
	}
}

func (s *memoryTokenStore) Revoke(token string, _ time.Time) error {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.revoked[token] = struct{}{}
	return nil
}

func (s *memoryTokenStore) IsRevoked(token string) (bool, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()
	_, ok := s.revoked[token]
	return ok, nil
}

func (s *memoryTokenStore) CleanupExpired() error {
	return nil
}
