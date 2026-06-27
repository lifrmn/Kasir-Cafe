package com.kasircafe.pos.data.api.model

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val role: String
)

data class ProductDto(
    val id: Long = 0,
    val nama: String,
    val barcode: String,
    val kategori: String,
    val harga_beli: Long,
    val harga_jual: Long,
    val stok: Int,
    val foto: String = ""
)

data class TransactionDto(
    val id: Long,
    val tanggal: String,
    val total: Long,
    val diskon: Long,
    val pajak: Long,
    val grand_total: Long,
    val metode_pembayaran: String,
    val kasir: String
)

data class TransactionDetailDto(
    val produk_id: Long,
    val jumlah: Int,
    val harga: Long,
    val subtotal: Long
)

data class TransactionCreateRequest(
    val total: Long,
    val diskon: Long,
    val pajak: Long,
    val grand_total: Long,
    val metode_pembayaran: String,
    val kasir: String,
    val detail: List<TransactionDetailDto>
)

data class ReportPeriodDto(
    val total_penjualan: Long,
    val transaksi: Long,
    val profit: Long
)

data class ReportResponse(
    val harian: ReportPeriodDto,
    val mingguan: ReportPeriodDto,
    val bulanan: ReportPeriodDto,
    val tahunan: ReportPeriodDto
)

data class FailedLoginPerDayDto(
    val day: String = "",
    val total: Int = 0
)

data class TopIpAddressDto(
    val ip_address: String = "",
    val total: Int = 0
)

data class AuthAuditSummaryDto(
    val total_events: Int = 0,
    val total_login: Int = 0,
    val total_failed_login: Int = 0,
    val total_logout: Int = 0,
    val total_refresh: Int = 0,
    val failed_login_per_day: List<FailedLoginPerDayDto> = emptyList(),
    val top_ip_addresses: List<TopIpAddressDto> = emptyList()
)
