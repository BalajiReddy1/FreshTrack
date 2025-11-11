package com.example.freshtrack.presentation.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.freshtrack.presentation.screen.dashboard.DashboardScreen
import com.example.freshtrack.presentation.screen.productlist.ProductListScreen
import com.example.freshtrack.presentation.screen.addproduct.AddEditProductScreen
import com.example.freshtrack.presentation.screen.productdetails.ProductDetailsScreen
import com.example.freshtrack.presentation.screen.settings.SettingsScreen

/**
 * Navigation routes for the app
 */
sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object ProductList : Screen("product_list")
    object AddProduct : Screen("add_product")
    object EditProduct : Screen("edit_product/{productId}") {
        fun createRoute(productId: String) = "edit_product/$productId"
    }
    object ProductDetails : Screen("product_details/{productId}") {
        fun createRoute(productId: String) = "product_details/$productId"
    }
    object Settings : Screen("settings")
    object BarcodeScanner : Screen("barcode_scanner")
}

/**
 * Main navigation graph for FreshTrack
 */
@Composable
fun FreshTrackNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Dashboard.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Dashboard Screen
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToProductList = {
                    navController.navigate(Screen.ProductList.route)
                },
                onNavigateToAddProduct = {
                    navController.navigate(Screen.AddProduct.route)
                },
                onNavigateToProductDetails = { productId ->
                    navController.navigate(Screen.ProductDetails.createRoute(productId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // Product List Screen
        composable(Screen.ProductList.route) {
            ProductListScreen(
                onNavigateBack = {
                    navController.navigateUp()
                },
                onNavigateToAddProduct = {
                    navController.navigate(Screen.AddProduct.route)
                },
                onNavigateToProductDetails = { productId ->
                    navController.navigate(Screen.ProductDetails.createRoute(productId))
                }
            )
        }

        // Add Product Screen
        composable(Screen.AddProduct.route) {
            AddEditProductScreen(
                productId = null,
                onNavigateBack = {
                    navController.navigateUp()
                },
                onNavigateToScanner = {
                    navController.navigate(Screen.BarcodeScanner.route)
                }
            )
        }

        // Edit Product Screen
        composable(
            route = Screen.EditProduct.route,
            arguments = listOf(
                navArgument("productId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            AddEditProductScreen(
                productId = productId,
                onNavigateBack = {
                    navController.navigateUp()
                },
                onNavigateToScanner = {
                    navController.navigate(Screen.BarcodeScanner.route)
                }
            )
        }

        // Product Details Screen
        composable(
            route = Screen.ProductDetails.route,
            arguments = listOf(
                navArgument("productId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailsScreen(
                productId = productId,
                onNavigateBack = {
                    navController.navigateUp()
                },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.EditProduct.createRoute(id))
                }
            )
        }

        // Settings Screen
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        // Barcode Scanner Screen (placeholder for now)
        composable(Screen.BarcodeScanner.route) {
            // BarcodeScannerScreen - Will implement in Phase 2
            // For now, just navigate back
            navController.navigateUp()
        }
    }
}