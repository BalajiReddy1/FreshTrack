package com.example.freshtrack.presentation.screen.dashboard


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.freshtrack.presentation.component.*
import com.example.freshtrack.presentation.theme.*
import com.example.freshtrack.presentation.viewmodel.DashboardViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToProductList: () -> Unit,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToProductDetails: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FreshTrack") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddProduct,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Product"
                )
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LoadingState(message = "Loading your products...")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                item {
                    Text(
                        text = "Dashboard",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }

                // Statistics Cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Total Products",
                            value = uiState.totalActiveProducts.toString(),
                            icon = {
                                Icon(
                                    Icons.Default.Inventory,
                                    contentDescription = null,
                                    tint = PrimaryGreen
                                )
                            },
                            backgroundColor = PrimaryGreen,
                            onClick = onNavigateToProductList,
                            modifier = Modifier.weight(1f)
                        )

                        StatCard(
                            title = "Expiring Soon",
                            value = (uiState.expiringToday.size + uiState.expiringThisWeek.size).toString(),
                            icon = {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = UrgencyWarning
                                )
                            },
                            backgroundColor = UrgencyWarning,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Expiring Today Section
                if (uiState.expiringToday.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Expiring Today",
                            icon = Icons.Default.Today,
                            color = UrgencyCritical
                        )
                    }

                    items(uiState.expiringToday) { product ->
                        ProductCard(
                            product = product,
                            onClick = { onNavigateToProductDetails(product.id) }
                        )
                    }
                }

                // Critical Items Section
                if (uiState.criticalItems.isNotEmpty() && uiState.expiringToday.isEmpty()) {
                    item {
                        SectionHeader(
                            title = "Critical Items",
                            icon = Icons.Default.Error,
                            color = UrgencyCritical
                        )
                    }

                    items(uiState.criticalItems.take(3)) { product ->
                        ProductCard(
                            product = product,
                            onClick = { onNavigateToProductDetails(product.id) }
                        )
                    }
                }

                // Expiring This Week Section
                if (uiState.expiringThisWeek.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Expiring This Week",
                            icon = Icons.Default.CalendarToday,
                            color = UrgencyWarning
                        )
                    }

                    items(uiState.expiringThisWeek.take(5)) { product ->
                        ProductCard(
                            product = product,
                            onClick = { onNavigateToProductDetails(product.id) }
                        )
                    }
                }

                // Expired Products Section
                if (uiState.expiredProducts.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Expired Products",
                            icon = Icons.Default.Block,
                            color = UrgencyExpired
                        )
                    }

                    items(uiState.expiredProducts.take(3)) { product ->
                        ProductCard(
                            product = product,
                            onClick = { onNavigateToProductDetails(product.id) }
                        )
                    }
                }

                // Empty State
                if (uiState.totalActiveProducts == 0) {
                    item {
                        EmptyState(
                            title = "No Products Yet",
                            message = "Add your first product to start tracking expiry dates",
                            icon = {
                                Icon(
                                    Icons.Default.Inventory2,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            },
                            actionButton = {
                                Button(onClick = onNavigateToAddProduct) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Add Product")
                                }
                            }
                        )
                    }
                }

                // View All Button
                if (uiState.totalActiveProducts > 0) {
                    item {
                        OutlinedButton(
                            onClick = onNavigateToProductList,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View All Products")
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = color
        )
    }
}