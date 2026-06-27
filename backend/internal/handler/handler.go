package handler

import (
	"net/http"
	"strconv"

	"kasir-cafe/backend/internal/domain"
	"kasir-cafe/backend/internal/service"

	"github.com/gin-gonic/gin"
)

type Handler struct {
	productService     service.ProductService
	transactionService service.TransactionService
	authService        service.AuthService
}

func NewHandler(
	productService service.ProductService,
	transactionService service.TransactionService,
	authService service.AuthService,
) *Handler {
	return &Handler{
		productService:     productService,
		transactionService: transactionService,
		authService:        authService,
	}
}

type loginRequest struct {
	Username string `json:"username" binding:"required"`
	Password string `json:"password" binding:"required"`
}

func (h *Handler) Login(c *gin.Context) {
	var req loginRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	token, role, err := h.authService.Login(c.Request.Context(), req.Username, req.Password)
	if err != nil {
		c.JSON(http.StatusUnauthorized, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"token": token, "role": role})
}

func (h *Handler) SeedAdmin(c *gin.Context) {
	if err := h.authService.SeedDefaultAdmin(c.Request.Context()); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "seed admin berhasil"})
}

func (h *Handler) GetProducts(c *gin.Context) {
	products, err := h.productService.List(c.Request.Context())
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, products)
}

func (h *Handler) CreateProduct(c *gin.Context) {
	var req domain.Product
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	product, err := h.productService.Create(c.Request.Context(), req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusCreated, product)
}

func (h *Handler) UpdateProduct(c *gin.Context) {
	id, err := strconv.ParseInt(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "id tidak valid"})
		return
	}

	var req domain.Product
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	product, err := h.productService.Update(c.Request.Context(), id, req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, product)
}

func (h *Handler) DeleteProduct(c *gin.Context) {
	id, err := strconv.ParseInt(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "id tidak valid"})
		return
	}

	if err := h.productService.Delete(c.Request.Context(), id); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "hapus produk berhasil"})
}

type createTransactionRequest struct {
	Total            int64                      `json:"total"`
	Diskon           int64                      `json:"diskon"`
	Pajak            int64                      `json:"pajak"`
	GrandTotal       int64                      `json:"grand_total"`
	MetodePembayaran string                     `json:"metode_pembayaran"`
	Kasir            string                     `json:"kasir"`
	Detail           []domain.TransactionDetail `json:"detail"`
}

func (h *Handler) GetTransactions(c *gin.Context) {
	transactions, err := h.transactionService.List(c.Request.Context())
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, transactions)
}

func (h *Handler) CreateTransaction(c *gin.Context) {
	var req createTransactionRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	transaction, err := h.transactionService.Create(
		c.Request.Context(),
		domain.Transaction{
			Total:            req.Total,
			Diskon:           req.Diskon,
			Pajak:            req.Pajak,
			GrandTotal:       req.GrandTotal,
			MetodePembayaran: req.MetodePembayaran,
			Kasir:            req.Kasir,
		},
		req.Detail,
	)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusCreated, transaction)
}

func (h *Handler) GetReport(c *gin.Context) {
	report, err := h.transactionService.Report(c.Request.Context())
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, report)
}

func (h *Handler) GetLowStock(c *gin.Context) {
	minimum, err := h.productService.LowStock(c.Request.Context(), 5)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"minimum": minimum})
}

func (h *Handler) CreateSupplier(c *gin.Context) {
	// Placeholder endpoint supplier, siap dipindahkan ke service/repository saat modul supplier diimplementasikan.
	c.JSON(http.StatusCreated, gin.H{"message": "supplier berhasil ditambahkan"})
}
