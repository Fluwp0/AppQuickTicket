package com.quickticket.app.network.model

data class RegisterRequest(
    val name: String,
    val rut: String,
    val email: String,
    val password: String
)
