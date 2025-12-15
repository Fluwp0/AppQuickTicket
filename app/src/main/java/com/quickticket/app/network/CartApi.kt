package com.quickticket.app.network

import com.quickticket.app.network.model.CartItem
import com.quickticket.app.network.model.CartItemRequest
import com.quickticket.app.network.model.CartItemUpdateRequest
import retrofit2.http.*

interface CartApi {

    @GET("api/cart/{email}")
    suspend fun getCart(
        @Path("email") email: String
    ): List<CartItem>

    @POST("api/cart")
    suspend fun addItem(
        @Body request: CartItemRequest
    ): CartItem

    @PUT("api/cart/{id}")
    suspend fun updateItem(
        @Path("id") id: Long,
        @Body request: CartItemUpdateRequest
    ): CartItem

    @DELETE("api/cart/{id}")
    suspend fun removeItem(
        @Path("id") id: Long
    )

}
