package middleware

import (
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
	"kasir-cafe/backend/internal/service"
)

func RequireAuth(authService service.AuthService) gin.HandlerFunc {
	return func(c *gin.Context) {
		token, ok := extractBearerToken(c)
		if !ok {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "authorization bearer token wajib diisi"})
			return
		}

		username, role, err := authService.ValidateToken(token)
		if err != nil {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": err.Error()})
			return
		}

		c.Set("username", username)
		c.Set("role", role)
		c.Set("token", token)
		c.Next()
	}
}

func RequireRoles(allowedRoles ...string) gin.HandlerFunc {
	allowed := make(map[string]struct{}, len(allowedRoles))
	for _, role := range allowedRoles {
		allowed[strings.ToLower(strings.TrimSpace(role))] = struct{}{}
	}

	return func(c *gin.Context) {
		roleValue, exists := c.Get("role")
		if !exists {
			c.AbortWithStatusJSON(http.StatusForbidden, gin.H{"error": "role tidak ditemukan"})
			return
		}

		role, _ := roleValue.(string)
		if _, ok := allowed[strings.ToLower(role)]; !ok {
			c.AbortWithStatusJSON(http.StatusForbidden, gin.H{"error": "akses ditolak untuk role ini"})
			return
		}

		c.Next()
	}
}

func extractBearerToken(c *gin.Context) (string, bool) {
	authorization := c.GetHeader("Authorization")
	if !strings.HasPrefix(authorization, "Bearer ") {
		return "", false
	}
	token := strings.TrimPrefix(authorization, "Bearer ")
	if strings.TrimSpace(token) == "" {
		return "", false
	}
	return token, true
}
