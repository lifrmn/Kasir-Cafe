package router

import (
	"kasir-cafe/backend/internal/handler"
	"kasir-cafe/backend/internal/middleware"
	"kasir-cafe/backend/internal/service"

	"github.com/gin-gonic/gin"
)

func New(h *handler.Handler, authService service.AuthService) *gin.Engine {
	r := gin.Default()

	r.Use(func(c *gin.Context) {
		c.Writer.Header().Set("Access-Control-Allow-Origin", "*")
		c.Writer.Header().Set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
		c.Writer.Header().Set("Access-Control-Allow-Headers", "Content-Type, Authorization")

		if c.Request.Method == "OPTIONS" {
			c.AbortWithStatus(204)
			return
		}

		c.Next()
	})

	r.POST("/login", h.Login)
	r.POST("/seed-admin", h.SeedAdmin)

	protected := r.Group("/")
	protected.Use(middleware.RequireAuth(authService))
	protected.POST("/refresh-token", h.RefreshToken)
	protected.POST("/logout", h.Logout)

	adminOnly := protected.Group("/")
	adminOnly.Use(middleware.RequireRoles("admin"))
	adminOnly.POST("/produk", h.CreateProduct)
	adminOnly.PUT("/produk/:id", h.UpdateProduct)
	adminOnly.DELETE("/produk/:id", h.DeleteProduct)
	adminOnly.POST("/supplier", h.CreateSupplier)
	adminOnly.GET("/admin/auth-audit-logs", h.GetAuthAuditLogs)
	adminOnly.GET("/admin/auth-audit-logs/export", h.ExportAuthAuditLogsCSV)
	adminOnly.GET("/admin/auth-audit-summary", h.GetAuthAuditSummary)

	protected.GET("/produk", h.GetProducts)

	protected.GET("/transaksi", h.GetTransactions)
	protected.POST("/transaksi", h.CreateTransaction)

	protected.GET("/laporan", h.GetReport)
	protected.GET("/stok", h.GetLowStock)

	return r
}
