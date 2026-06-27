package com.kasircafe.pos.domain.usecase

import com.kasircafe.pos.data.repository.ProductRepository
import javax.inject.Inject

class GetProductsUseCase @Inject constructor(
    private val repository: ProductRepository
)
