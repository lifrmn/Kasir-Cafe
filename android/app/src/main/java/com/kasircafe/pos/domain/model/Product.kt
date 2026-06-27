package com.kasircafe.pos.domain.model

data class Product(
    val id: Long,
    val nama: String,
    val barcode: String,
    val kategori: String,
    val hargaBeli: Long,
    val hargaJual: Long,
    val stok: Int,
    val foto: String = ""
)
