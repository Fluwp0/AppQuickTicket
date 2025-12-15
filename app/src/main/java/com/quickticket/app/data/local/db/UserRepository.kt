package com.quickticket.app.data.repository

import com.quickticket.app.data.local.db.UserDao
import com.quickticket.app.data.local.db.UserEntity

class UserRepository(private val dao: UserDao) {

    suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<Long> {
        val exists = dao.countByEmail(email) > 0
        if (exists) {
            return Result.failure(IllegalArgumentException("El correo ya existe"))
        }

        val id = dao.insert(
            UserEntity(
                name = name,
                email = email,
                password = password
            )
        )
        return Result.success(id)
    }
}
