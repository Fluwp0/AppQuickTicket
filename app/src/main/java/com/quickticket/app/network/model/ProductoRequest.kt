package com.quickticket.app.network.model

import java.math.BigDecimal

data class ProductoRequest(
    val name: String,
    val category: String,
    val price: BigDecimal,
    val stock: Int
)
