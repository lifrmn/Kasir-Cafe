package com.kasircafe.pos.data.api

import com.kasircafe.pos.data.api.model.LoginRequest
import com.kasircafe.pos.data.api.model.LoginResponse
import com.kasircafe.pos.data.api.model.ProductDto
import com.kasircafe.pos.data.api.model.ReportResponse
import com.kasircafe.pos.data.api.model.TransactionCreateRequest
import com.kasircafe.pos.data.api.model.TransactionDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PosApiService {
    @POST("/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("/produk")
    suspend fun getProducts(): List<ProductDto>

    @POST("/produk")
    suspend fun createProduct(@Body product: ProductDto): ProductDto

    @PUT("/produk/{id}")
    suspend fun updateProduct(@Path("id") id: Long, @Body product: ProductDto): ProductDto

    @DELETE("/produk/{id}")
    suspend fun deleteProduct(@Path("id") id: Long)

    @GET("/transaksi")
    suspend fun getTransactions(): List<TransactionDto>

    @POST("/transaksi")
    suspend fun createTransaction(@Body request: TransactionCreateRequest): TransactionDto

    @GET("/laporan")
    suspend fun getReport(): ReportResponse
}
