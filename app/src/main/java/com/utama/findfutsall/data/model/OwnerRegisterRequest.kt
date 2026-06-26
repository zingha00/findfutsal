package com.utama.findfutsall.data.model

data class OwnerRegisterRequest(
    val user_id: Int,
    val nama_lapangan: String,
    val alamat: String,
    val maps_link: String = "",
    val kota: String,
    val no_telepon: String,
    val deskripsi: String,
    val jenis_permukaan: List<String>,
    val jumlah_lapangan: Int,
    val harga_per_jam: Long,
    val jam_buka: String,
    val jam_tutup: String,
    val fasilitas: List<String>
    // foto dikirim terpisah via multipart
)