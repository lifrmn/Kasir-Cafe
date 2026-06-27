package com.kasircafe.pos.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kasircafe.pos.presentation.audit.AuditScreen
import com.kasircafe.pos.presentation.dashboard.DashboardScreen
import com.kasircafe.pos.presentation.kasir.CashierScreen
import com.kasircafe.pos.presentation.login.LoginScreen
import com.kasircafe.pos.presentation.produk.ProductsScreen

private object Route {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val PRODUCTS = "products"
    const val CASHIER = "cashier"
    const val AUDIT = "audit"
}

@Composable
fun PosNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Route.LOGIN) {
        composable(Route.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Route.DASHBOARD) {
                        popUpTo(Route.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Route.DASHBOARD) {
            DashboardScreen(
                onOpenProducts = { navController.navigate(Route.PRODUCTS) },
                onOpenCashier = { navController.navigate(Route.CASHIER) },
                onOpenAudit = { navController.navigate(Route.AUDIT) }
            )
        }
        composable(Route.PRODUCTS) {
            ProductsScreen()
        }
        composable(Route.CASHIER) {
            CashierScreen()
        }
        composable(Route.AUDIT) {
            AuditScreen()
        }
    }
}
