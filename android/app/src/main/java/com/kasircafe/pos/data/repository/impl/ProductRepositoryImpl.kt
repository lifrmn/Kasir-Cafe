package com.kasircafe.pos.data.repository.impl

import com.kasircafe.pos.data.api.PosApiService
import com.kasircafe.pos.data.database.dao.ProductDao
import com.kasircafe.pos.data.mapper.toDomain
import com.kasircafe.pos.data.mapper.toDto
import com.kasircafe.pos.data.mapper.toEntity
import com.kasircafe.pos.data.repository.ProductRepository
import com.kasircafe.pos.domain.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val api: PosApiService,
    private val dao: ProductDao
) : ProductRepository {
    override fun observeProducts(): Flow<List<Product>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun refreshProducts() {
        val remote = api.getProducts().map { it.toDomain().toEntity() }
        dao.insertAll(remote)
    }

    override suspend fun createProduct(product: Product) {
        val created = api.createProduct(product.toDto()).toDomain()
        dao.upsert(created.toEntity())
    }

    override suspend fun updateProduct(product: Product) {
        val updated = api.updateProduct(product.id, product.toDto()).toDomain()
        dao.upsert(updated.toEntity())
    }

    override suspend fun deleteProduct(id: Long) {
        api.deleteProduct(id)
        dao.deleteById(id)
    }
}
