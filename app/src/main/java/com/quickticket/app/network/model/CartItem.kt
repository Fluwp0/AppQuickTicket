package com.quickticket.app.network.model

import java.math.BigDecimal


data class CartItem(
    val id: Long,
    val userEmail: String,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val totalPrice: BigDecimal
)
