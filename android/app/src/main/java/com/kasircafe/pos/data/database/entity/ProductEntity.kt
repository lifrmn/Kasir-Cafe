package com.kasircafe.pos.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: Long,
    val nama: String,
    val barcode: String,
    val kategori: String,
    val hargaBeli: Long,
    val hargaJual: Long,
    val stok: Int,
    val foto: String
)
