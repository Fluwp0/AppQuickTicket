package com.quickticket.app.network.model

data class UserResponse(
    val id: Long,
    val name: String,
    val rut: String,
    val email: String,
    val createdAt: String
)

