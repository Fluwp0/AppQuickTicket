package com.quickticket.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickticket.app.network.RetrofitClient
import com.quickticket.app.network.model.CartItem
import com.quickticket.app.network.model.CartItemRequest
import com.quickticket.app.network.model.CartItemUpdateRequest
import com.quickticket.app.network.model.Producto
import com.quickticket.app.network.model.ProductoRequest
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    userEmail: String,
    onBackHome: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var showCart by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var cartLoading by remember { mutableStateOf(false) }
    var cartError by remember { mutableStateOf<String?>(null) }
    var cartItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }

    val cartTotal: BigDecimal = remember(cartItems) {
        cartItems.fold(BigDecimal.ZERO) { acc, item -> acc + item.totalPrice }
    }

    var productsLoading by remember { mutableStateOf(false) }
    var productsError by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }
    var products by remember { mutableStateOf<List<Producto>>(emptyList()) }

    LaunchedEffect(userEmail) {
        if (userEmail.isNotBlank()) {
            loadProducts(
                setLoading = { productsLoading = it },
                setError = { productsError = it },
                setProducts = { products = it }
            )
            loadCart(
                email = userEmail,
                setLoading = { cartLoading = it },
                setError = { cartError = it },
                setItems = { cartItems = it }
            )
        } else {
            productsError = "No se encontró el correo del usuario."
        }
    }

    val mercadoLibreYellow = Color(0xFFFFF159)
    val backgroundGray = Color(0xFFF2F2F2)
    val priceColor = Color(0xFF00A650)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (showCart) "Tu carrito" else "Catálogo",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackHome) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showCart = !showCart }) {
                        BadgedBox(
                            badge = {
                                if (cartItems.isNotEmpty()) {
                                    Badge {
                                        Text(text = cartItems.size.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (showCart) Icons.Default.List else Icons.Default.ShoppingCart,
                                contentDescription = "",
                                tint = Color.Black
                            )
                        }
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = mercadoLibreYellow
                )
            )
        },

        floatingActionButton = {
            if (!showCart) {
                FloatingActionButton(
                    onClick = { showAddProductDialog = true },
                    containerColor = mercadoLibreYellow,
                    contentColor = Color.Black
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar producto"
                    )
                }
            }
        }
    ) { innerPadding ->

        if (showAddProductDialog) {
            AddProductDialog(
                onDismiss = { showAddProductDialog = false },
                onCreated = { msg ->
                    infoMessage = msg
                    showAddProductDialog = false
                    scope.launch {
                        loadProducts(
                            setLoading = { productsLoading = it },
                            setError = { productsError = it },
                            setProducts = { products = it }
                        )
                    }
                },
                onError = { err ->
                    productsError = err
                }
            )
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(backgroundGray)
        ) {

            if (infoMessage != null) {
                Text(
                    text = infoMessage!!,
                    color = Color(0xFF388E3C),
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (showCart) {
                CartContent(
                    cartItems = cartItems,
                    cartLoading = cartLoading,
                    cartError = cartError,
                    total = cartTotal,
                    onRetryLoad = {
                        scope.launch {
                            loadCart(
                                email = userEmail,
                                setLoading = { cartLoading = it },
                                setError = { cartError = it },
                                setItems = { cartItems = it }
                            )
                        }
                    },
                    onClearCart = {

                    },
                    onDeleteItem = { item ->
                        scope.launch {
                            try {
                                RetrofitClient.cartApi.removeItem(item.id)
                                loadCart(
                                    email = userEmail,
                                    setLoading = { cartLoading = it },
                                    setError = { cartError = it },
                                    setItems = { cartItems = it }
                                )
                            } catch (e: Exception) {
                                cartError = "Actualizar carrito."
                            }
                        }
                    },
                    onChangeQuantity = { item, newQty ->
                        scope.launch {
                            try {
                                RetrofitClient.cartApi.updateItem(
                                    id = item.id,
                                    request = CartItemUpdateRequest(quantity = newQty)
                                )
                                loadCart(
                                    email = userEmail,
                                    setLoading = { cartLoading = it },
                                    setError = { cartError = it },
                                    setItems = { cartItems = it }
                                )
                            } catch (e: Exception) {
                                cartError = "Error al actualizar la cantidad."
                            }
                        }
                    },
                    mercadoLibreYellow = mercadoLibreYellow,
                    priceColor = priceColor
                )
            } else {
                // ================= VISTA CATÁLOGO =================
                CatalogContent(
                    products = products,
                    productsLoading = productsLoading,
                    productsError = productsError,
                    userEmail = userEmail,
                    onAddToCart = { product ->
                        val cleanEmail = userEmail.trim()

                        if (cleanEmail.isBlank()) {
                            productsError = "No se encontró el correo del usuario."
                            return@CatalogContent
                        }

                        scope.launch {
                            try {
                                infoMessage = null
                                productsError = null

                                RetrofitClient.cartApi.addItem(
                                    CartItemRequest(
                                        userEmail = cleanEmail,
                                        productId = product.id,
                                        quantity = 1
                                    )
                                )

                                infoMessage = "Producto agregado al carrito."

                                loadCart(
                                    email = cleanEmail,
                                    setLoading = { cartLoading = it },
                                    setError = { cartError = it },
                                    setItems = { cartItems = it }
                                )
                            } catch (e: HttpException) {
                                productsError =
                                    "Error al agregar al carrito: ${httpErrorMessage(e)} (productId=${product.id})"
                            } catch (e: Exception) {
                                productsError =
                                    "Error al agregar al carrito: ${e.message ?: "sin detalle"} (productId=${product.id})"
                            }
                        }
                    },
                    onRetryLoad = {
                        scope.launch {
                            loadProducts(
                                setLoading = { productsLoading = it },
                                setError = { productsError = it },
                                setProducts = { products = it }
                            )
                        }
                    },
                    mercadoLibreYellow = mercadoLibreYellow,
                    priceColor = priceColor
                )
            }
        }
    }
}

// ====================== DIALOGO CREAR PRODUCTO =========================

@Composable
private fun AddProductDialog(
    onDismiss: () -> Unit,
    onCreated: (String) -> Unit,
    onError: (String) -> Unit
) {
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var stockText by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text("Agregar producto") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre (ej: Empanada)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría (ej: snack)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Precio (ej: 1200)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = stockText,
                    onValueChange = { stockText = it },
                    label = { Text("Stock (ej: 10)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (localError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = localError!!, color = Color.Red, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !saving,
                onClick = {
                    val n = name.trim()
                    val c = category.trim()

                    val normalizedPrice = priceText.trim().replace(",", ".")
                    val p = normalizedPrice.toBigDecimalOrNull()
                    val s = stockText.trim().toIntOrNull()

                    if (n.isBlank() || c.isBlank()) {
                        localError = "Nombre y categoría son obligatorios."
                        return@TextButton
                    }
                    if (p == null || p < BigDecimal.ZERO) {
                        localError = "Precio inválido."
                        return@TextButton
                    }
                    if (s == null || s < 0) {
                        localError = "Stock inválido."
                        return@TextButton
                    }

                    saving = true
                    localError = null

                    scope.launch {
                        try {
                            RetrofitClient.productApi.createProduct(
                                ProductoRequest(
                                    name = n,
                                    category = c,
                                    price = p,
                                    stock = s
                                )
                            )
                            onCreated("Producto creado.")
                        } catch (e: HttpException) {
                            onError("Error al crear producto: ${httpErrorMessage(e)}")
                        } catch (e: Exception) {
                            onError("Error al crear producto: ${e.message ?: "sin detalle"}")
                        } finally {
                            saving = false
                        }
                    }
                }
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(
                enabled = !saving,
                onClick = onDismiss
            ) { Text("Cancelar") }
        }
    )
}

// ====================== CATÁLOGO =========================

@Composable
private fun CatalogContent(
    products: List<Producto>,
    productsLoading: Boolean,
    productsError: String?,
    userEmail: String,
    onAddToCart: (Producto) -> Unit,
    onRetryLoad: () -> Unit,
    mercadoLibreYellow: Color,
    priceColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        if (productsError != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = productsError,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onRetryLoad,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.LightGray,
                        contentColor = Color.Black
                    )
                ) { Text("Actualizar") }
            }
        }

        Text(
            text = "Productos disponibles",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        if (productsLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (products.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No hay productos disponibles.", fontSize = 15.sp)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(products, key = { it.id }) { product ->
                    ProductRowInCartScreen(
                        product = product,
                        onAddToCart = { onAddToCart(product) },
                        mercadoLibreYellow = mercadoLibreYellow,
                        priceColor = priceColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ProductRowInCartScreen(
    product: Producto,
    onAddToCart: () -> Unit,
    mercadoLibreYellow: Color,
    priceColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = product.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Categoría: ${product.category}",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Text(
                text = "Stock: ${product.stock}",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formatCurrency(product.price),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = priceColor
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onAddToCart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = mercadoLibreYellow,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "Agregar al carrito",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ====================== CARRITO =========================

@Composable
private fun CartContent(
    cartItems: List<CartItem>,
    cartLoading: Boolean,
    cartError: String?,
    total: BigDecimal,
    onRetryLoad: () -> Unit,
    onClearCart: () -> Unit,
    onDeleteItem: (CartItem) -> Unit,
    onChangeQuantity: (CartItem, Int) -> Unit,
    mercadoLibreYellow: Color,
    priceColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        if (cartError != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = cartError,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onRetryLoad,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.LightGray,
                        contentColor = Color.Black
                    )
                ) { Text("Actualizar") }
            }
        }

        if (cartLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (cartItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Tu carrito está vacío",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(cartItems, key = { it.id }) { item ->
                    CartItemRow(
                        item = item,
                        onDelete = { onDeleteItem(item) },
                        onChangeQuantity = { newQty -> onChangeQuantity(item, newQty) },
                        priceColor = priceColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = formatCurrency(total),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = priceColor
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Button(
                            onClick = { /* Implementar el supuesto pago*/ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = mercadoLibreYellow,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(
                                text = "Continuar compra",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartItemRow(
    item: CartItem,
    onDelete: () -> Unit,
    onChangeQuantity: (Int) -> Unit,
    priceColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Cantidad: ${item.quantity}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Precio unitario: ${formatCurrency(item.unitPrice)}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(item.totalPrice),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = priceColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Button(
                        onClick = {
                            val newQty = (item.quantity - 1).coerceAtLeast(1)
                            if (newQty != item.quantity) onChangeQuantity(newQty)
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.LightGray,
                            contentColor = Color.Black
                        )
                    ) { Text("-") }

                    Spacer(modifier = Modifier.width(4.dp))

                    Button(
                        onClick = { onChangeQuantity(item.quantity + 1) },
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.LightGray,
                            contentColor = Color.Black
                        )
                    ) { Text("+") }

                    Spacer(modifier = Modifier.width(4.dp))

                    Button(
                        onClick = onDelete,
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE53935),
                            contentColor = Color.White
                        )
                    ) { Text("X") }
                }
            }
        }
    }
}

// --------- Helpers ---------

private suspend fun loadCart(
    email: String,
    setLoading: (Boolean) -> Unit,
    setError: (String?) -> Unit,
    setItems: (List<CartItem>) -> Unit
) {
    try {
        setLoading(true)
        setError(null)
        val response = RetrofitClient.cartApi.getCart(email)
        setItems(response)
    } catch (e: Exception) {
        setError("Error al cargar el carrito.")
    } finally {
        setLoading(false)
    }
}

private suspend fun loadProducts(
    setLoading: (Boolean) -> Unit,
    setError: (String?) -> Unit,
    setProducts: (List<Producto>) -> Unit
) {
    try {
        setLoading(true)
        setError(null)
        val response = RetrofitClient.productApi.getAllProducts()
        setProducts(response)
    } catch (e: Exception) {
        setError("Error al cargar productos.")
    } finally {
        setLoading(false)
    }
}

private fun formatCurrency(amount: BigDecimal): String {
    val nf = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    return nf.format(amount)
}

private fun httpErrorMessage(e: HttpException): String {
    return try {
        val body = e.response()?.errorBody()?.string()
        if (!body.isNullOrBlank()) "HTTP ${e.code()}: $body" else "HTTP ${e.code()}"
    } catch (_: Exception) {
        "HTTP ${e.code()}"
    }
}
