package com.utama.findfutsall.utils

/**
 * Utility terpusat untuk format harga Rupiah.
 * Semua tempat yang menampilkan harga WAJIB pakai fungsi ini,
 * supaya formatnya konsisten di seluruh aplikasi.
 *
 * Format: Rp90.000 (tanpa spasi, titik sebagai pemisah ribuan, tanpa desimal)
 */
object PriceFormatter {

    fun format(price: Int): String = "Rp${formatNumber(price.toLong())}"

    fun format(price: Long): String = "Rp${formatNumber(price)}"

    fun format(price: Double): String = "Rp${formatNumber(price.toLong())}"

    /**
     * Format angka dengan titik sebagai pemisah ribuan, tanpa simbol Rp.
     * Berguna kalau butuh angka mentah saja (misal untuk perhitungan/display khusus).
     */
    fun formatNumber(price: Long): String {
        val s      = price.toString()
        val result = StringBuilder()
        var count  = 0
        for (i in s.length - 1 downTo 0) {
            if (count > 0 && count % 3 == 0) result.insert(0, ".")
            result.insert(0, s[i])
            count++
        }
        return result.toString()
    }

    fun formatNumber(price: Int): String = formatNumber(price.toLong())
}