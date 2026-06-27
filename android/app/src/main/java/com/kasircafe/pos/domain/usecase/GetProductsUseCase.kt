package com.kasircafe.pos.domain.usecase

import com.kasircafe.pos.data.repository.ProductRepository
import com.kasircafe.pos.domain.model.Product
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProductsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(): Flow<List<Product>> = repository.observeProducts()
}
