package com.quickticket.app.network

import com.quickticket.app.network.model.LoginRequest
import com.quickticket.app.network.model.RegisterRequest
import com.quickticket.app.network.model.UserResponse
import com.quickticket.app.network.model.TicketStatusResponse
import com.quickticket.app.network.model.TicketClaimRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {

    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): UserResponse

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): UserResponse

    @GET("/api/tickets/status")
    suspend fun getTicketStatus(@Query("email") email: String): TicketStatusResponse

    @POST("/api/tickets/claim")
    suspend fun claimTicket(@Body request: TicketClaimRequest): TicketStatusResponse
}
