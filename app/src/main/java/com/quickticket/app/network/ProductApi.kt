package com.quickticket.app.network

import com.quickticket.app.network.model.Producto
import com.quickticket.app.network.model.ProductoRequest
import retrofit2.http.*

interface ProductApi {

    @GET("api/products")
    suspend fun getAllProducts(): List<Producto>

    @GET("api/products/{id}")
    suspend fun getProductById(@Path("id") id: Long): Producto

    @POST("api/products")
    suspend fun createProduct(@Body request: ProductoRequest): Producto

    @PUT("api/products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Long,
        @Body request: ProductoRequest
    ): Producto

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(@Path("id") id: Long)
}
