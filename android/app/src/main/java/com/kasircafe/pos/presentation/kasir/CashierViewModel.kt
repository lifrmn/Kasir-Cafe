package com.kasircafe.pos.presentation.kasir

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kasircafe.pos.data.repository.ProductRepository
import com.kasircafe.pos.data.repository.TransactionRepository
import com.kasircafe.pos.domain.model.Product
import com.kasircafe.pos.domain.model.TransactionDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartItem(
    val product: Product,
    val qty: Int
)

data class CashierUiState(
    val cart: List<CartItem> = emptyList(),
    val discount: Long = 0,
    val tax: Long = 0,
    val paymentMethod: String = "Tunai",
    val isLoading: Boolean = false,
    val message: String = "",
    val error: String = ""
)

@HiltViewModel
class CashierViewModel @Inject constructor(
    productRepository: ProductRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    val products = productRepository.observeProducts().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _uiState = MutableStateFlow(CashierUiState())
    val uiState: StateFlow<CashierUiState> = _uiState.asStateFlow()

    init {
        refreshTransactions()
    }

    fun addProduct(product: Product) {
        _uiState.update { state ->
            val existing = state.cart.find { it.product.id == product.id }
            val nextCart = if (existing == null) {
                state.cart + CartItem(product = product, qty = 1)
            } else {
                state.cart.map {
                    if (it.product.id == product.id) it.copy(qty = it.qty + 1) else it
                }
            }
            state.copy(cart = nextCart, message = "", error = "")
        }
    }

    fun updatePaymentMethod(value: String) {
        _uiState.update { it.copy(paymentMethod = value) }
    }

    fun setDiscount(value: Long) {
        _uiState.update { it.copy(discount = value) }
    }

    fun setTax(value: Long) {
        _uiState.update { it.copy(tax = value) }
    }

    private fun refreshTransactions() {
        viewModelScope.launch {
            transactionRepository.refreshTransactions()
        }
    }

    fun checkout() {
        val state = _uiState.value
        if (state.cart.isEmpty()) {
            _uiState.update { it.copy(error = "Keranjang masih kosong") }
            return
        }

        val total = state.cart.sumOf { it.product.hargaJual * it.qty }
        val grandTotal = total - state.discount + state.tax

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = "", message = "") }
            runCatching {
                transactionRepository.createTransaction(
                    total = total,
                    diskon = state.discount,
                    pajak = state.tax,
                    grandTotal = grandTotal,
                    metodePembayaran = state.paymentMethod,
                    kasir = "kasir-mobile",
                    detail = state.cart.map { item ->
                        TransactionDetail(
                            produkId = item.product.id,
                            jumlah = item.qty,
                            harga = item.product.hargaJual,
                            subtotal = item.product.hargaJual * item.qty
                        )
                    }
                )
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false, cart = emptyList(), message = "Transaksi berhasil", error = "") }
                refreshTransactions()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = throwable.message ?: "Transaksi gagal"
                    )
                }
            }
        }
    }
}
