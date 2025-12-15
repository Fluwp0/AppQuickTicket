package com.quickticket.app.data.local.db

interface UserDao {
    suspend fun countByEmail(email: String): Int
    suspend fun insert(user: UserEntity): Long
}
