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

    /**
     * filter, dateFrom, dateTo dipakai supaya owner bisa pilih rentang waktu
     * chart (7 hari/30 hari/2 bulan/6 bulan) dan summary card ikut berubah
     * sesuai rentang itu -- bukan cuma chart-nya saja yang berubah secara lokal.
     */
    fun loadKeuangan(
        userId: Int,
        filter: String = "bulan",
        dateFrom: String = "",
        dateTo: String = ""
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val request = mutableMapOf<String, Any>(
                    "owner_id" to userId,
                    "filter"   to filter
                )
                if (dateFrom.isNotEmpty()) request["date_from"] = dateFrom
                if (dateTo.isNotEmpty())   request["date_to"]   = dateTo

                val response = ApiClient.instance.getKeuangan(request)
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