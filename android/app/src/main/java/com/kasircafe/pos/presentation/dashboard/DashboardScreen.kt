package com.kasircafe.pos.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DashboardScreen(
    onOpenProducts: () -> Unit,
    onOpenCashier: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Dashboard", style = MaterialTheme.typography.headlineMedium)
        DashboardCard(title = "Total Penjualan", value = "Rp ${state.summary.totalPenjualan}")
        DashboardCard(title = "Transaksi", value = "${state.summary.transaksi}")
        DashboardCard(title = "Produk Terjual", value = "-" )
        DashboardCard(title = "Profit", value = "Rp ${state.summary.profit}")

        Button(onClick = onOpenProducts, modifier = Modifier.fillMaxWidth()) {
            Text("Kelola Produk")
        }
        Button(onClick = onOpenCashier, modifier = Modifier.fillMaxWidth()) {
            Text("Buka Kasir")
        }

        if (state.error.isNotBlank()) {
            Text(state.error, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun DashboardCard(title: String, value: String) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}
