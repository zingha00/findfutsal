package com.utama.findfutsall.data.api

import com.utama.findfutsall.data.model.LoginRequest
import com.utama.findfutsall.data.model.LoginResponse
import com.utama.findfutsall.data.model.OwnerRegisterRequest
import com.utama.findfutsall.data.model.OwnerRegisterResponse
import com.utama.findfutsall.data.model.RegisterRequest
import com.utama.findfutsall.data.model.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("login.php")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("register.php")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("forgot_password.php")
    suspend fun forgotPassword(
        @Body request: Map<String, String>
    ): Response<Map<String, Any>>

    @POST("google_auth.php")
    suspend fun googleAuth(
        @Body request: Map<String, String>
    ): Response<LoginResponse>

    @POST("register_owner.php")
    suspend fun registerOwner(
        @Body request: OwnerRegisterRequest
    ): Response<OwnerRegisterResponse>

    @POST("get_owner_data.php")
    suspend fun getOwnerData(
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): Response<Map<String, @JvmSuppressWildcards Any>>

    @POST("get_owner_fields.php")
    suspend fun getOwnerFields(
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): Response<Map<String, @JvmSuppressWildcards Any>>

    @POST("get_owner_stats.php")
    suspend fun getOwnerStats(
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): Response<Map<String, @JvmSuppressWildcards Any>>

    @POST("get_owner_keuangan.php")
    suspend fun getKeuangan(
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): Response<com.utama.findfutsall.data.model.KeuanganResponse>

    @POST("booking.php")
    suspend fun createBooking(
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): Response<Map<String, @JvmSuppressWildcards Any>>

    @POST("get_user_bookings.php")
    suspend fun getUserBookings(
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): Response<Map<String, @JvmSuppressWildcards Any>>

    @POST("get_owner_bookings.php")
    suspend fun getOwnerBookings(
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): Response<Map<String, @JvmSuppressWildcards Any>>

    @POST("toggle_favorite.php")
    suspend fun toggleFavorite(
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): Response<Map<String, @JvmSuppressWildcards Any>>

    @POST("get_favorites.php")
    suspend fun getFavorites(
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): Response<Map<String, @JvmSuppressWildcards Any>>
}