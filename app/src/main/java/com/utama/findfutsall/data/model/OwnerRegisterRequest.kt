package com.utama.findfutsall.data.model

data class OwnerRegisterRequest(
    val user_id: Int,
    val nama_lapangan: String,
    val alamat: String,
    val kota: String,
    val no_telepon: String,
    val deskripsi: String,
    val jenis_permukaan: List<String>,   // ["Rumput Sintetis", "Parquet", dll]
    val jumlah_lapangan: Int,
    val harga_per_jam: Long,
    val jam_buka: String,                // "06:00"
    val jam_tutup: String,               // "23:00"
    val fasilitas: List<String>          // ["Parkir Luas", "Toilet", dll]
    // foto dikirim terpisah via multipart
)