package com.utama.findfutsall.data.model

import com.google.gson.annotations.SerializedName


data class KeuanganResponse(
    val success: Boolean,
    val summary: KeuanganSummary?,
    val charts: KeuanganCharts?,
    val transaksi: KeuanganTransaksiPage?,
    val period: KeuanganPeriod?
)

data class KeuanganSummary(
    val hari: Double,
    val minggu: Double,
    val bulan: Double,
    val tahun: Double,
    val biaya: Double = 0.0,
    val denda: Double = 0.0,
    @SerializedName("total_transaksi")
    val totalTransaksi: Int,
    @SerializedName("rata_rata")
    val rataRata: Double,
    @SerializedName("pct_change")
    val pctChange: Double,
    @SerializedName("target_bulanan")
    val targetBulanan: Double = 0.0
)

data class KeuanganCharts(
    val daily: List<ChartPoint>,
    val monthly: List<ChartPoint>
)

data class KeuanganPeriod(
    val from: String,
    val to: String
)

data class KeuanganTransaksiPage(
    val data: List<TransaksiItem>
)

data class ChartPoint(
    val label: String,
    val value: Double
)

data class ChartMetode(
    val metode: String,
    val total: Double
)

data class ChartCompare(
    val label: String,
    val current: Double,
    val previous: Double
)

data class TransaksiItem(
    val id: Int,
    @SerializedName("customer_name")
    val customerName: String? = null,
    @SerializedName("field_name")
    val fieldName: String? = null,
    @SerializedName("play_date")
    val playDate: String? = null,
    @SerializedName("start_time")
    val startTime: String? = null,
    @SerializedName("end_time")
    val endTime: String? = null,
    @SerializedName("booking_status")
    val bookingStatus: String? = null,
    @SerializedName("total_price")
    val totalPrice: Double? = null,
    @SerializedName("payment_method")
    val paymentMethod: String? = null,
    @SerializedName("pendapatan_bersih")
    val pendapatanBersih: Double? = null
)