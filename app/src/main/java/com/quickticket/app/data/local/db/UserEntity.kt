package com.quickticket.app.data.local.db

data class UserEntity(
    val id: Long = 0L,
    val name: String,
    val email: String,
    val password: String
)
