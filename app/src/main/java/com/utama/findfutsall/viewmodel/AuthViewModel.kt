package com.utama.findfutsall.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utama.findfutsall.data.model.LoginResponse
import com.utama.findfutsall.data.model.RegisterResponse
import com.utama.findfutsall.data.repository.AuthRepository
import kotlinx.coroutines.launch
import org.json.JSONObject

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _loginResult = MutableLiveData<Result<LoginResponse>?>()
    val loginResult: LiveData<Result<LoginResponse>?> = _loginResult

    private val _registerResult = MutableLiveData<Result<RegisterResponse>?>()
    val registerResult: LiveData<Result<RegisterResponse>?> = _registerResult

    private val _forgotPasswordResult = MutableLiveData<Result<String>?>()
    val forgotPasswordResult: LiveData<Result<String>?> = _forgotPasswordResult

    private val _verifyOtpResult = MutableLiveData<Result<String>?>()
    val verifyOtpResult: LiveData<Result<String>?> = _verifyOtpResult

    private val _resetPasswordResult = MutableLiveData<Result<String>?>()
    val resetPasswordResult: LiveData<Result<String>?> = _resetPasswordResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _googleAuthResult = MutableLiveData<Result<LoginResponse>>()
    val googleAuthResult: LiveData<Result<LoginResponse>> = _googleAuthResult

    private val _registerMessage = MutableLiveData<String?>()
    val registerMessage: LiveData<String?> = _registerMessage

    fun googleAuth(name: String, email: String, photo: String, token: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val request = mapOf(
                    "name" to name,
                    "email" to email,
                    "photo" to photo,
                    "token" to token
                )
                val response = repository.googleAuth(request)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _googleAuthResult.value = Result.success(it)
                    }
                } else {
                    _googleAuthResult.value = Result.failure(Exception("Google Auth gagal"))
                }
            } catch (e: Exception) {
                _googleAuthResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun login(identifier: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.login(identifier, password)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _loginResult.value = Result.success(it)
                    } ?: run {
                        _loginResult.value = Result.failure(Exception("Response body is null"))
                    }
                } else {
                    val errorMsg = parseError(response.errorBody()?.string())
                    _loginResult.value = Result.failure(Exception(errorMsg ?: "Login gagal (${response.code()})"))
                }
            } catch (e: Exception) {
                _loginResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(
        namaLengkap: String,
        username: String,
        email: String,
        nomorHp: String,
        password: String,
        passwordConfirmation: String
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.register(namaLengkap, username, email, nomorHp, password, passwordConfirmation)
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.success) {
                            _registerResult.value = Result.success(it)
                            _registerMessage.value = it.message
                        } else {
                            _registerResult.value = Result.failure(Exception(it.message))
                        }
                    } ?: run {
                        _registerResult.value = Result.failure(Exception("Response body is null"))
                    }
                } else {
                    val errorMsg = parseError(response.errorBody()?.string())
                    _registerResult.value = Result.failure(Exception(errorMsg ?: "Registrasi gagal (${response.code()})"))
                }
            } catch (e: Exception) {
                _registerResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun forgotPassword(email: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.forgotPassword(email)
                if (response.isSuccessful) {
                    val body = response.body()
                    val isSuccess = body?.get("success") as? Boolean ?: false
                    if (isSuccess) {
                        val msg = body?.get("message") as? String ?: "Kode OTP telah dikirim"
                        _forgotPasswordResult.value = Result.success(msg)
                    } else {
                        val msg = body?.get("message") as? String ?: "Gagal mengirim kode OTP"
                        _forgotPasswordResult.value = Result.failure(Exception(msg))
                    }
                } else {
                    val errorMsg = parseError(response.errorBody()?.string())
                    _forgotPasswordResult.value = Result.failure(Exception(errorMsg ?: "Email tidak ditemukan"))
                }
            } catch (e: Exception) {
                _forgotPasswordResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun verifyOtp(email: String, otp: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.verifyOtp(email, otp)
                if (response.isSuccessful) {
                    val body = response.body()
                    val isSuccess = body?.get("success") as? Boolean ?: false
                    if (isSuccess) {
                        val msg = body?.get("message") as? String ?: "Kode OTP valid"
                        _verifyOtpResult.value = Result.success(msg)
                    } else {
                        val msg = body?.get("message") as? String ?: "Kode OTP tidak valid"
                        _verifyOtpResult.value = Result.failure(Exception(msg))
                    }
                } else {
                    val errorMsg = parseError(response.errorBody()?.string())
                    _verifyOtpResult.value = Result.failure(Exception(errorMsg ?: "Verifikasi OTP gagal"))
                }
            } catch (e: Exception) {
                _verifyOtpResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetPassword(email: String, otp: String, newPassword: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.resetPassword(email, otp, newPassword)
                if (response.isSuccessful) {
                    val body = response.body()
                    val isSuccess = body?.get("success") as? Boolean ?: false
                    if (isSuccess) {
                        val msg = body?.get("message") as? String ?: "Password berhasil diubah"
                        _resetPasswordResult.value = Result.success(msg)
                    } else {
                        val msg = body?.get("message") as? String ?: "Gagal mengubah password"
                        _resetPasswordResult.value = Result.failure(Exception(msg))
                    }
                } else {
                    val errorMsg = parseError(response.errorBody()?.string())
                    _resetPasswordResult.value = Result.failure(Exception(errorMsg ?: "Reset password gagal"))
                }
            } catch (e: Exception) {
                _resetPasswordResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetLoginResult() { _loginResult.value = null }
    fun resetRegisterResult() { _registerResult.value = null }
    fun resetForgotPasswordResult() { _forgotPasswordResult.value = null }
    fun resetVerifyOtpResult() { _verifyOtpResult.value = null }
    fun resetResetPasswordResult() { _resetPasswordResult.value = null }
    fun clearRegisterMessage() { _registerMessage.value = null }

    private fun parseError(errorBody: String?): String? {
        if (errorBody == null) return null
        return try {
            val jsonObject = JSONObject(errorBody)
            jsonObject.optString("message").ifEmpty { jsonObject.optString("error") }
        } catch (e: Exception) { null }
    }
}