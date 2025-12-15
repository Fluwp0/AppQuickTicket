package com.quickticket.app.data.repository

import com.quickticket.app.data.local.db.UserDao
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class UserRepositoryTest : StringSpec({

    "register falla si el correo ya existe" {
        val dao = mockk<UserDao>()
        val repo = UserRepository(dao)

        coEvery { dao.countByEmail("test@correo.com") } returns 1

        val result = repo.register(
            name = "Henry",
            email = "test@correo.com",
            password = "123456"
        )

        result.isFailure.shouldBeTrue()
        coVerify(exactly = 1) { dao.countByEmail("test@correo.com") }
        coVerify(exactly = 0) { dao.insert(any()) }
    }

    "register inserta usuario cuando el correo NO existe" {
        val dao = mockk<UserDao>()
        val repo = UserRepository(dao)

        coEvery { dao.countByEmail("nuevo@correo.com") } returns 0
        coEvery { dao.insert(any()) } returns 10L

        val result = repo.register(
            name = "Nuevo",
            email = "nuevo@correo.com",
            password = "abcdef"
        )

        result.isSuccess.shouldBeTrue()
        result.getOrNull() shouldBe 10L
        coVerify(exactly = 1) { dao.countByEmail("nuevo@correo.com") }
        coVerify(exactly = 1) { dao.insert(any()) }
    }
})
