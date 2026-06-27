package com.kasircafe.pos.presentation.kasir

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CashierScreen(viewModel: CashierViewModel = hiltViewModel()) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pendingCount by viewModel.pendingCount.collectAsStateWithLifecycle()
    val total = state.cart.sumOf { it.product.hargaJual * it.qty }
    val grandTotal = total - state.discount + state.tax

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Kasir", style = MaterialTheme.typography.headlineMedium)

        if (pendingCount > 0) {
            Card {
                Text(
                    "$pendingCount transaksi menunggu sinkronisasi (offline)",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        OutlinedTextField(
            value = state.discount.toString(),
            onValueChange = { viewModel.setDiscount(it.toLongOrNull() ?: 0L) },
            label = { Text("Diskon") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.tax.toString(),
            onValueChange = { viewModel.setTax(it.toLongOrNull() ?: 0L) },
            label = { Text("Pajak") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.updatePaymentMethod("Tunai") }) { Text("Tunai") }
            Button(onClick = { viewModel.updatePaymentMethod("QRIS") }) { Text("QRIS") }
            Button(onClick = { viewModel.updatePaymentMethod("Transfer") }) { Text("Transfer") }
        }

        Text("Metode: ${state.paymentMethod}")
        Text("Total: Rp $total")
        Text("Grand Total: Rp $grandTotal", style = MaterialTheme.typography.titleMedium)

        if (state.error.isNotBlank()) {
            Text(state.error, color = MaterialTheme.colorScheme.error)
        }
        if (state.message.isNotBlank()) {
            Text(state.message, color = MaterialTheme.colorScheme.primary)
        }

        Button(onClick = viewModel::checkout, modifier = Modifier.fillMaxWidth()) {
            Text("Checkout")
        }

        Text("Produk", style = MaterialTheme.typography.titleMedium)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(products, key = { it.id }) { product ->
                Card {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(product.nama)
                            Text("Stok ${product.stok}")
                        }
                        Button(onClick = { viewModel.addProduct(product) }) {
                            Text("Tambah")
                        }
                    }
                }
            }
        }
    }
}
