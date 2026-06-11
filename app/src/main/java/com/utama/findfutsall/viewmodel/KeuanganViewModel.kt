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

    fun loadKeuangan(
        userId: Int,
        filter: String = "bulan",
        status: String = "",
        dateFrom: String = "",
        dateTo: String = "",
        page: Int = 1
    ) {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val body = mutableMapOf<String, Any>(
                    "user_id" to userId,
                    "filter"  to filter,
                    "page"    to page
                )
                if (status.isNotEmpty()) body["status"] = status
                if (dateFrom.isNotEmpty()) body["date_from"] = dateFrom
                if (dateTo.isNotEmpty())   body["date_to"]   = dateTo

                val response = ApiClient.instance.getKeuangan(body)
                if (response.isSuccessful && response.body()?.success == true) {
                    _keuangan.value = response.body()
                } else {
                    _error.value = response.body()?.message ?: "Gagal memuat data keuangan"
                }
            } catch (e: Exception) {
                _error.value = "Koneksi gagal: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}