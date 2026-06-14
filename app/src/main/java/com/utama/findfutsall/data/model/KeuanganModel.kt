package com.utama.findfutsall.data.model

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
    val totalTransaksi: Int,
    val rataRata: Double,
    val pctChange: Double
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
    val customerName: String,
    val fieldName: String,
    val playDate: String,
    val startTime: String,
    val endTime: String,
    val bookingStatus: String,
    val totalPrice: Double,
    val paymentMethod: String = "",
    val pendapatanBersih: Double = 0.0
)