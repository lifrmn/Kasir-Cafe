package com.kasircafe.pos.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kasircafe.pos.data.database.dao.ProductDao
import com.kasircafe.pos.data.database.dao.TransactionDao
import com.kasircafe.pos.data.database.entity.ProductEntity
import com.kasircafe.pos.data.database.entity.TransactionDetailEntity
import com.kasircafe.pos.data.database.entity.TransactionEntity

@Database(
	entities = [ProductEntity::class, TransactionEntity::class, TransactionDetailEntity::class],
	version = 1,
	exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
	abstract fun productDao(): ProductDao
	abstract fun transactionDao(): TransactionDao
}
