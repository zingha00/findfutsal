package com.utama.findfutsall.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utama.findfutsall.data.api.ApiClient
import com.utama.findfutsall.data.model.KeuanganResponse
import kotlinx.coroutines.launch

class KeuanganViewModel : ViewModel() {

    private val _keuangan = MutableLiveData<KeuanganResponse?>()
    val keuangan: LiveData<KeuanganResponse?> = _keuangan

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadKeuangan(userId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = ApiClient.instance.getKeuangan(
                    mapOf("owner_id" to userId)
                )
                if (response.isSuccessful) {
                    _keuangan.value = response.body()
                } else {
                    _error.value = "Gagal memuat data keuangan"
                }
            } catch (e: Exception) {
                _error.value = "Koneksi gagal: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}