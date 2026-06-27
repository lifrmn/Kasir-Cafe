package middleware

import (
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
	"kasir-cafe/backend/internal/service"
)

func RequireAuth(authService service.AuthService) gin.HandlerFunc {
	return func(c *gin.Context) {
		authorization := c.GetHeader("Authorization")
		if !strings.HasPrefix(authorization, "Bearer ") {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "authorization bearer token wajib diisi"})
			return
		}

		token := strings.TrimPrefix(authorization, "Bearer ")
		username, role, err := authService.ValidateToken(token)
		if err != nil {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": err.Error()})
			return
		}

		c.Set("username", username)
		c.Set("role", role)
		c.Next()
	}
}
