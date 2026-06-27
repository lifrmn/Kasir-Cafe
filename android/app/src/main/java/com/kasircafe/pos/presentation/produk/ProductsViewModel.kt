package com.kasircafe.pos.presentation.produk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kasircafe.pos.data.repository.ProductRepository
import com.kasircafe.pos.domain.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductForm(
    val id: Long = 0,
    val nama: String = "",
    val barcode: String = "",
    val kategori: String = "",
    val hargaBeli: String = "0",
    val hargaJual: String = "0",
    val stok: String = "0"
)

data class ProductsUiState(
    val isLoading: Boolean = false,
    val error: String = "",
    val successMessage: String = "",
    val form: ProductForm = ProductForm()
)

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {
    val products = repository.observeProducts().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun updateForm(update: ProductForm) {
        _uiState.update { it.copy(form = update, error = "", successMessage = "") }
    }

    fun selectProduct(product: Product) {
        _uiState.update {
            it.copy(
                form = ProductForm(
                    id = product.id,
                    nama = product.nama,
                    barcode = product.barcode,
                    kategori = product.kategori,
                    hargaBeli = product.hargaBeli.toString(),
                    hargaJual = product.hargaJual.toString(),
                    stok = product.stok.toString()
                ),
                error = "",
                successMessage = ""
            )
        }
    }

    fun clearForm() {
        _uiState.update { it.copy(form = ProductForm()) }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = "") }
            runCatching { repository.refreshProducts() }
                .onSuccess { _uiState.update { it.copy(isLoading = false) } }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Gagal sinkron produk"
                        )
                    }
                }
        }
    }

    fun submit() {
        val form = _uiState.value.form
        val product = Product(
            id = form.id,
            nama = form.nama,
            barcode = form.barcode,
            kategori = form.kategori,
            hargaBeli = form.hargaBeli.toLongOrNull() ?: 0,
            hargaJual = form.hargaJual.toLongOrNull() ?: 0,
            stok = form.stok.toIntOrNull() ?: 0
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = "", successMessage = "") }
            runCatching {
                if (product.id == 0L) repository.createProduct(product) else repository.updateProduct(product)
            }.onSuccess {
                _uiState.update {
                    it.copy(isLoading = false, successMessage = "Produk berhasil disimpan", form = ProductForm())
                }
                refresh()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(isLoading = false, error = throwable.message ?: "Simpan produk gagal")
                }
            }
        }
    }

    fun deleteProduct(id: Long) {
        viewModelScope.launch {
            runCatching { repository.deleteProduct(id) }
                .onSuccess {
                    _uiState.update { it.copy(successMessage = "Produk berhasil dihapus", error = "") }
                    refresh()
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(error = throwable.message ?: "Hapus produk gagal") }
                }
        }
    }
}
