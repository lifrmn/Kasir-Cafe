package com.kasircafe.pos.data.mapper

import com.kasircafe.pos.data.api.model.AuthAuditSummaryDto
import com.kasircafe.pos.data.api.model.ProductDto
import com.kasircafe.pos.data.api.model.TransactionDetailDto
import com.kasircafe.pos.data.api.model.TransactionDto
import com.kasircafe.pos.data.database.entity.ProductEntity
import com.kasircafe.pos.data.database.entity.TransactionDetailEntity
import com.kasircafe.pos.data.database.entity.TransactionEntity
import com.kasircafe.pos.domain.model.AuditSummary
import com.kasircafe.pos.domain.model.FailedLoginPerDay
import com.kasircafe.pos.domain.model.Product
import com.kasircafe.pos.domain.model.TopIpAddress
import com.kasircafe.pos.domain.model.Transaction
import com.kasircafe.pos.domain.model.TransactionDetail

fun ProductEntity.toDomain(): Product = Product(
    id = id,
    nama = nama,
    barcode = barcode,
    kategori = kategori,
    hargaBeli = hargaBeli,
    hargaJual = hargaJual,
    stok = stok,
    foto = foto
)

fun Product.toEntity(): ProductEntity = ProductEntity(
    id = id,
    nama = nama,
    barcode = barcode,
    kategori = kategori,
    hargaBeli = hargaBeli,
    hargaJual = hargaJual,
    stok = stok,
    foto = foto
)

fun ProductDto.toDomain(): Product = Product(
    id = id,
    nama = nama,
    barcode = barcode,
    kategori = kategori,
    hargaBeli = harga_beli,
    hargaJual = harga_jual,
    stok = stok,
    foto = foto
)

fun Product.toDto(): ProductDto = ProductDto(
    id = id,
    nama = nama,
    barcode = barcode,
    kategori = kategori,
    harga_beli = hargaBeli,
    harga_jual = hargaJual,
    stok = stok,
    foto = foto
)

fun TransactionDto.toDomain(): Transaction = Transaction(
    id = id,
    tanggal = tanggal,
    total = total,
    diskon = diskon,
    pajak = pajak,
    grandTotal = grand_total,
    metodePembayaran = metode_pembayaran,
    kasir = kasir
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    tanggal = tanggal,
    total = total,
    diskon = diskon,
    pajak = pajak,
    grandTotal = grandTotal,
    metodePembayaran = metodePembayaran,
    kasir = kasir
)

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    tanggal = tanggal,
    total = total,
    diskon = diskon,
    pajak = pajak,
    grandTotal = grandTotal,
    metodePembayaran = metodePembayaran,
    kasir = kasir
)

fun TransactionDetail.toDto(): TransactionDetailDto = TransactionDetailDto(
    produk_id = produkId,
    jumlah = jumlah,
    harga = harga,
    subtotal = subtotal
)

fun TransactionDetailDto.toDomain(): TransactionDetail = TransactionDetail(
    produkId = produk_id,
    jumlah = jumlah,
    harga = harga,
    subtotal = subtotal
)

fun TransactionDetailEntity.toDomain(): TransactionDetail = TransactionDetail(
    produkId = produkId,
    jumlah = jumlah,
    harga = harga,
    subtotal = subtotal
)

fun TransactionDetail.toEntity(transactionId: Long): TransactionDetailEntity = TransactionDetailEntity(
    transactionId = transactionId,
    produkId = produkId,
    jumlah = jumlah,
    harga = harga,
    subtotal = subtotal
)

fun AuthAuditSummaryDto.toDomain(): AuditSummary = AuditSummary(
    totalEvents = total_events,
    totalLogin = total_login,
    totalFailedLogin = total_failed_login,
    totalLogout = total_logout,
    totalRefresh = total_refresh,
    failedLoginPerDay = failed_login_per_day.map { FailedLoginPerDay(day = it.day, total = it.total) },
    topIpAddresses = top_ip_addresses.map { TopIpAddress(ipAddress = it.ip_address, total = it.total) }
)
