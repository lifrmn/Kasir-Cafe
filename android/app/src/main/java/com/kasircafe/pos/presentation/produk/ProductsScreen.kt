package com.kasircafe.pos.presentation.produk

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
fun ProductsScreen(viewModel: ProductsViewModel = hiltViewModel()) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val form = state.form

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Produk", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = form.nama,
            onValueChange = { viewModel.updateForm(form.copy(nama = it)) },
            label = { Text("Nama") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = form.barcode,
            onValueChange = { viewModel.updateForm(form.copy(barcode = it)) },
            label = { Text("Barcode") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = form.kategori,
                onValueChange = { viewModel.updateForm(form.copy(kategori = it)) },
                label = { Text("Kategori") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = form.stok,
                onValueChange = { viewModel.updateForm(form.copy(stok = it)) },
                label = { Text("Stok") },
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = form.hargaBeli,
                onValueChange = { viewModel.updateForm(form.copy(hargaBeli = it)) },
                label = { Text("Harga Beli") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = form.hargaJual,
                onValueChange = { viewModel.updateForm(form.copy(hargaJual = it)) },
                label = { Text("Harga Jual") },
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = viewModel::submit, modifier = Modifier.weight(1f)) {
                Text(if (form.id == 0L) "Tambah" else "Update")
            }
            Button(onClick = viewModel::clearForm, modifier = Modifier.weight(1f)) {
                Text("Reset")
            }
        }

        if (state.error.isNotBlank()) {
            Text(state.error, color = MaterialTheme.colorScheme.error)
        }
        if (state.successMessage.isNotBlank()) {
            Text(state.successMessage, color = MaterialTheme.colorScheme.primary)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(products, key = { it.id }) { product ->
                Card {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(product.nama, style = MaterialTheme.typography.titleMedium)
                        Text("${product.kategori} | Stok: ${product.stok}")
                        Text("Jual: Rp ${product.hargaJual}")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { viewModel.selectProduct(product) }) { Text("Edit") }
                            Button(onClick = { viewModel.deleteProduct(product.id) }) { Text("Hapus") }
                        }
                    }
                }
            }
        }
    }
}
