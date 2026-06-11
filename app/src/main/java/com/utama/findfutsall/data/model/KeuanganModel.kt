package com.utama.findfutsall.data.model

import com.google.gson.annotations.SerializedName

// ─── Response utama ───────────────────────────────────────────────────────────
data class KeuanganResponse(
    @SerializedName("success")    val success: Boolean,
    @SerializedName("message")    val message: String? = null,
    @SerializedName("filter")     val filter: String? = null,
    @SerializedName("period")     val period: KeuanganPeriod? = null,
    @SerializedName("summary")    val summary: KeuanganSummary? = null,
    @SerializedName("charts")     val charts: KeuanganCharts? = null,
    @SerializedName("transaksi")  val transaksi: KeuanganTransaksiPage? = null
)

data class KeuanganPeriod(
    @SerializedName("from") val from: String,
    @SerializedName("to")   val to: String
)

// ─── Summary cards ────────────────────────────────────────────────────────────
data class KeuanganSummary(
    @SerializedName("hari")             val hari: Double,
    @SerializedName("minggu")           val minggu: Double,
    @SerializedName("bulan")            val bulan: Double,
    @SerializedName("tahun")            val tahun: Double,
    @SerializedName("total_transaksi")  val totalTransaksi: Int,
    @SerializedName("rata_rata")        val rataRata: Double,
    @SerializedName("pct_change")       val pctChange: Double,
    @SerializedName("total_periode")    val totalPeriode: Double
)

// ─── Charts ───────────────────────────────────────────────────────────────────
data class KeuanganCharts(
    @SerializedName("monthly")  val monthly: List<ChartPoint>,
    @SerializedName("weekly")   val weekly: List<ChartPoint>,
    @SerializedName("daily")    val daily: List<ChartPoint>,
    @SerializedName("metode")   val metode: List<ChartMetode>,
    @SerializedName("compare")  val compare: List<ChartCompare>
)

data class ChartPoint(
    @SerializedName("label") val label: String,
    @SerializedName("value") val value: Double
)

data class ChartMetode(
    @SerializedName("label")  val label: String,
    @SerializedName("jumlah") val jumlah: Int,
    @SerializedName("value")  val value: Double
)

data class ChartCompare(
    @SerializedName("label")     val label: String,
    @SerializedName("this_year") val thisYear: Double,
    @SerializedName("last_year") val lastYear: Double
)

// ─── Tabel transaksi ─────────────────────────────────────────────────────────
data class KeuanganTransaksiPage(
    @SerializedName("data")        val data: List<TransaksiItem>,
    @SerializedName("total")       val total: Int,
    @SerializedName("page")        val page: Int,
    @SerializedName("per_page")    val perPage: Int,
    @SerializedName("total_pages") val totalPages: Int
)

data class TransaksiItem(
    @SerializedName("id")                val id: Int,
    @SerializedName("customer_name")     val customerName: String,
    @SerializedName("field_name")        val fieldName: String,
    @SerializedName("play_date")         val playDate: String,
    @SerializedName("start_time")        val startTime: String,
    @SerializedName("end_time")          val endTime: String,
    @SerializedName("total_price")       val totalPrice: Double,
    @SerializedName("service_fee")       val serviceFee: Double,
    @SerializedName("pendapatan_bersih") val pendapatanBersih: Double,
    @SerializedName("payment_method")    val paymentMethod: String,
    @SerializedName("booking_status")    val bookingStatus: String,
    @SerializedName("payment_status")    val paymentStatus: String,
    @SerializedName("created_at")        val createdAt: String
)