package router

import (
	"kasir-cafe/backend/internal/handler"

	"github.com/gin-gonic/gin"
)

func New(h *handler.Handler) *gin.Engine {
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

	r.GET("/produk", h.GetProducts)
	r.POST("/produk", h.CreateProduct)
	r.PUT("/produk/:id", h.UpdateProduct)
	r.DELETE("/produk/:id", h.DeleteProduct)

	r.GET("/transaksi", h.GetTransactions)
	r.POST("/transaksi", h.CreateTransaction)

	r.GET("/laporan", h.GetReport)
	r.GET("/stok", h.GetLowStock)
	r.POST("/supplier", h.CreateSupplier)

	return r
}
