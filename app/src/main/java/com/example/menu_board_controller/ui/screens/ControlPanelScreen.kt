package com.example.menu_board_controller.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.menu_board_controller.data.model.Category
import com.example.menu_board_controller.data.model.Product
import com.example.menu_board_controller.ui.viewmodel.MenuViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlPanelScreen(viewModel: MenuViewModel = viewModel()) {
    val menuData by viewModel.menuData.collectAsState()
    var showCategoryDialog by remember { mutableStateOf<Category?>(null) }
    var isAddingCategory by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Menu Control Panel", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { isAddingCategory = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Category") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(menuData, key = { it.id }) { category ->
                CategoryCard(
                    category = category,
                    onEditCategory = { showCategoryDialog = category },
                    onAddProduct = { name, price -> viewModel.addProduct(name, price, category.id) },
                    onDeleteCategory = { viewModel.deleteCategory(category.id) },
                    onDeleteProduct = { viewModel.deleteProduct(it) },
                    onToggleCategoryVisibility = { viewModel.updateCategory(category.id, null, null, it) },
                    onToggleProductVisibility = { id, visible -> viewModel.updateProduct(id, null, null, visible) },
                    viewModel = viewModel
                )
            }
        }

        if (isAddingCategory) {
            CategoryDialog(
                onDismiss = { isAddingCategory = false },
                onConfirm = { name, index ->
                    viewModel.addCategory(name, index)
                    isAddingCategory = false
                }
            )
        }

        showCategoryDialog?.let { category ->
            CategoryDialog(
                category = category,
                onDismiss = { showCategoryDialog = null },
                onConfirm = { name, index ->
                    viewModel.updateCategory(category.id, name, index, category.isVisible)
                    showCategoryDialog = null
                }
            )
        }
    }
}

@Composable
fun CategoryCard(
    category: Category,
    onEditCategory: () -> Unit,
    onAddProduct: (String, Double) -> Unit,
    onDeleteCategory: () -> Unit,
    onDeleteProduct: (String) -> Unit,
    onToggleCategoryVisibility: (Boolean) -> Unit,
    onToggleProductVisibility: (String, Boolean) -> Unit,
    viewModel: MenuViewModel
) {
    var expanded by remember { mutableStateOf(true) }
    var showProductDialog by remember { mutableStateOf<Product?>(null) }
    var isAddingProduct by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (category.isVisible) MaterialTheme.colorScheme.surface 
                             else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.name, 
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (category.isVisible) Color.Unspecified else Color.Gray
                    )
                    Text(
                        text = "Order Index: ${category.orderIndex}", 
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = category.isVisible,
                        onCheckedChange = onToggleCategoryVisibility,
                        modifier = Modifier.scaleWidget(0.8f)
                    )
                    IconButton(onClick = onEditCategory) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDeleteCategory) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    category.products.forEach { product ->
                        ProductItemView(
                            product = product,
                            onEdit = { showProductDialog = product },
                            onDelete = { onDeleteProduct(product.id) },
                            onToggleVisibility = { onToggleProductVisibility(product.id, it) }
                        )
                    }

                    TextButton(
                        onClick = { isAddingProduct = true },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Product")
                    }
                }
            }
        }

        if (isAddingProduct) {
            ProductDialog(
                onDismiss = { isAddingProduct = false },
                onConfirm = { name, price ->
                    onAddProduct(name, price)
                    isAddingProduct = false
                }
            )
        }

        showProductDialog?.let { product ->
            ProductDialog(
                product = product,
                onDismiss = { showProductDialog = null },
                onConfirm = { name, price ->
                    viewModel.updateProduct(product.id, name, price, product.isVisible)
                    showProductDialog = null
                }
            )
        }
    }
}

@Composable
fun ProductItemView(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleVisibility: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.name, 
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (product.isVisible) Color.Unspecified else Color.Gray
            )
            Text(
                text = "$${String.format(Locale.US, "%.2f", product.price)}", 
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = product.isVisible,
                onCheckedChange = onToggleVisibility,
                modifier = Modifier.scaleWidget(0.7f)
            )
            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun CategoryDialog(
    category: Category? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var orderIndex by remember { mutableStateOf(category?.orderIndex?.toString() ?: "0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category == null) "New Category" else "Edit Category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = orderIndex, 
                    onValueChange = { orderIndex = it }, 
                    label = { Text("Display Order") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, orderIndex.toIntOrNull() ?: 0) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ProductDialog(
    product: Product? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "New Product" else "Edit Product") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = price, 
                    onValueChange = { price = it }, 
                    label = { Text("Price") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, price.toDoubleOrNull() ?: 0.0) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Helper for switch scaling
@Composable
fun Modifier.scaleWidget(scale: Float): Modifier = this.then(
    Modifier.scale(scale)
)
