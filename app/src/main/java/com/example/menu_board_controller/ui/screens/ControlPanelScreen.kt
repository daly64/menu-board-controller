package com.example.menu_board_controller.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.menu_board_controller.data.model.Category
import com.example.menu_board_controller.data.model.Product
import com.example.menu_board_controller.ui.viewmodel.MenuViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlPanelScreen(viewModel: MenuViewModel = viewModel()) {
    val menuData by viewModel.menuData.collectAsState()
    var isAddingCategory by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Dashboard, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("BOARD CONTROLLER", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.reconnect() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { isAddingCategory = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("NEW CATEGORY") }
            )
        }
    ) { padding ->
        if (menuData.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    Text("CONNECTING TO BOARD...", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(menuData, key = { index, category -> category.id + index }) { _, category ->
                CategoryAccordion(
                    category = category,
                    onEdit = { categoryToEdit = category },
                    onDelete = { viewModel.deleteCategory(category.id) },
                    onToggle = { viewModel.updateCategory(id = category.id, isVisible = it) },
                    viewModel = viewModel
                )
            }
            item { Spacer(Modifier.height(80.dp)) }
        }

        if (isAddingCategory) {
            CategoryDialog(onDismiss = { isAddingCategory = false }, onConfirm = { n, i -> viewModel.addCategory(n, i); isAddingCategory = false })
        }
        categoryToEdit?.let { cat ->
            CategoryDialog(category = cat, onDismiss = { categoryToEdit = null }, onConfirm = { n, i -> viewModel.updateCategory(cat.id, n, i, cat.isVisible); categoryToEdit = null })
        }
    }
}

@Composable
fun CategoryAccordion(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: (Boolean) -> Unit,
    viewModel: MenuViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = category.isVisible,
                    onCheckedChange = onToggle,
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                )
                
                Spacer(Modifier.width(8.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        category.name.uppercase(), 
                        fontWeight = FontWeight.ExtraBold, 
                        fontSize = 16.sp,
                        color = if (category.isVisible) MaterialTheme.colorScheme.onSurface else Color.Gray
                    )
                    Text(
                        "${category.products.size} items · pos: ${category.orderIndex}", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) }
                
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Products
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    if (category.products.isEmpty()) {
                        Text(
                            "Empty Category", 
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    category.products.forEach { product ->
                        ProductListItem(
                            product = product,
                            onToggle = { viewModel.updateProduct(id = product.id, isVisible = it) },
                            onEdit = { productToEdit = product }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }

                    // Add Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAddProductDialog = true }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AddCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("ADD PRODUCT", fontWeight = FontWeight.Black, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    if (showAddProductDialog) {
        ProductDialog(onDismiss = { showAddProductDialog = false }, onConfirm = { n, p -> viewModel.addProduct(n, p, category.id); showAddProductDialog = false })
    }
    productToEdit?.let { prod ->
        ProductDialog(product = prod, onDismiss = { productToEdit = null }, onConfirm = { n, p -> viewModel.updateProduct(prod.id, n, p, prod.isVisible, category.id); productToEdit = null }, onDelete = { viewModel.deleteProduct(prod.id); productToEdit = null })
    }
}

@Composable
fun ProductListItem(
    product: Product,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (product.isVisible) Color.Transparent else Color.LightGray.copy(alpha = 0.1f))
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = product.isVisible,
            onCheckedChange = onToggle,
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
        )
        
        Spacer(Modifier.width(12.dp))

        Text(
            text = product.name, 
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.SemiBold, 
            color = if (product.isVisible) MaterialTheme.colorScheme.onSurface else Color.Gray,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        Text(
            text = String.format(Locale.US, "%.3f DT", product.price), 
            color = MaterialTheme.colorScheme.primary, 
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) { 
            Icon(Icons.Default.Settings, null, modifier = Modifier.size(18.dp), tint = Color.Gray) 
        }
    }
}

@Composable
fun CategoryDialog(category: Category? = null, onDismiss: () -> Unit, onConfirm: (String, Int) -> Unit) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var index by remember { mutableStateOf(category?.orderIndex?.toString() ?: "1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(if (category == null) "NEW CATEGORY" else "EDIT CATEGORY", fontWeight = FontWeight.Black, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("NAME") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = index, onValueChange = { index = it }, label = { Text("ORDER") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { Button(onClick = { if(name.isNotBlank()) onConfirm(name, index.toIntOrNull() ?: 1) }) { Text("SAVE") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL") } }
    )
}

@Composable
fun ProductDialog(product: Product? = null, onDismiss: () -> Unit, onConfirm: (String, Double) -> Unit, onDelete: (() -> Unit)? = null) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(if (product == null) "NEW PRODUCT" else "EDIT PRODUCT", fontWeight = FontWeight.Black, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("PRODUCT NAME") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("PRICE (DT)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                if (onDelete != null) {
                    Button(onClick = onDelete, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("DELETE PERMANENTLY")
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { if(name.isNotBlank()) onConfirm(name, price.toDoubleOrNull() ?: 0.0) }) { Text("SAVE") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL") } }
    )
}
