package com.kasircafe.pos.presentation.kasir

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kasircafe.pos.domain.model.Product
import com.kasircafe.pos.domain.model.TransactionDetail
import com.kasircafe.pos.domain.usecase.CreateTransactionUseCase
import com.kasircafe.pos.domain.usecase.GetProductsUseCase
import com.kasircafe.pos.domain.usecase.ObservePendingTransactionsUseCase
import com.kasircafe.pos.domain.usecase.RefreshTransactionsUseCase
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
    getProducts: GetProductsUseCase,
    observePendingTransactions: ObservePendingTransactionsUseCase,
    private val createTransaction: CreateTransactionUseCase,
    private val refreshTransactionsUseCase: RefreshTransactionsUseCase
) : ViewModel() {
    val products = getProducts().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val pendingCount = observePendingTransactions().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
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
            refreshTransactionsUseCase()
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
                createTransaction(
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
            }.onSuccess { online ->
                val info = if (online) {
                    "Transaksi berhasil"
                } else {
                    "Koneksi gagal. Transaksi disimpan offline dan akan disinkronkan otomatis."
                }
                _uiState.update { it.copy(isLoading = false, cart = emptyList(), message = info, error = "") }
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
