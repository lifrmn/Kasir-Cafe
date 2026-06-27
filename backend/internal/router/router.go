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

	protected.GET("/produk", h.GetProducts)
	protected.POST("/produk", h.CreateProduct)
	protected.PUT("/produk/:id", h.UpdateProduct)
	protected.DELETE("/produk/:id", h.DeleteProduct)

	protected.GET("/transaksi", h.GetTransactions)
	protected.POST("/transaksi", h.CreateTransaction)

	protected.GET("/laporan", h.GetReport)
	protected.GET("/stok", h.GetLowStock)
	protected.POST("/supplier", h.CreateSupplier)

	return r
}
