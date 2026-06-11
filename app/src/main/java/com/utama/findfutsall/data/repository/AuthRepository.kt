package com.utama.findfutsall.data.repository

import com.utama.findfutsall.data.api.ApiClient
import com.utama.findfutsall.data.model.LoginRequest
import com.utama.findfutsall.data.model.LoginResponse
import com.utama.findfutsall.data.model.OwnerRegisterRequest
import com.utama.findfutsall.data.model.OwnerRegisterResponse
import com.utama.findfutsall.data.model.RegisterRequest
import com.utama.findfutsall.data.model.RegisterResponse
import retrofit2.Response

class AuthRepository {

    private val api = ApiClient.instance

    suspend fun login(identifier: String, password: String): Response<LoginResponse> {
        return api.login(LoginRequest(identifier, password))
    }

    suspend fun register(
        namaLengkap: String,
        username: String,
        email: String,
        nomorHp: String,
        password: String,
        passwordConfirmation: String
    ): Response<RegisterResponse> {
        return api.register(
            RegisterRequest(
                namaLengkap = namaLengkap,
                username = username,
                email = email,
                nomorHp = nomorHp,
                password = password,
                passwordConfirmation = passwordConfirmation
            )
        )
    }

    suspend fun forgotPassword(email: String): Response<Map<String, Any>> {
        return api.forgotPassword(mapOf("email" to email))
    }

    suspend fun googleAuth(request: Map<String, String>): Response<LoginResponse> {
        return api.googleAuth(request)
    }

    suspend fun registerOwner(request: OwnerRegisterRequest): Response<OwnerRegisterResponse> {
        return api.registerOwner(request)
    }
}