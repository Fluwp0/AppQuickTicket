package com.quickticket.app.network.model

data class CartItemRequest(
    val userEmail: String,
    val productId: Long,
    val quantity: Int
)
