package com.kasircafe.pos.domain.model

data class Transaction(
    val id: Long,
    val tanggal: String,
    val total: Long,
    val diskon: Long,
    val pajak: Long,
    val grandTotal: Long,
    val metodePembayaran: String,
    val kasir: String
)

data class TransactionDetail(
    val produkId: Long,
    val jumlah: Int,
    val harga: Long,
    val subtotal: Long
)
