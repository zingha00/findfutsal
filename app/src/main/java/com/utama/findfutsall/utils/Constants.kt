package com.utama.findfutsall.utils

object Constants {

    // Gunakan ini jika menggunakan EMULATOR:
    // const val BASE_URL = "http://10.0.2.2/findfutsall/api/"

    // Gunakan ini jika menggunakan HP FISIK (Ganti IP sesuai IP Laptop/PC Anda):
    var BASE_URL = "http://192.168.0.103/findfutsall/api/"

    const val PREF_NAME = "findfutsall_pref"
    const val KEY_TOKEN = "token"
    const val KEY_USER_ID = "user_id"
    const val KEY_USER_NAME = "user_name"
    const val KEY_USER_USERNAME = "user_username"
    const val KEY_USER_EMAIL = "user_email"
    const val KEY_USER_PHONE = "user_phone"
    const val KEY_USER_PHOTO = "user_photo"
    const val KEY_USER_ROLE = "user_role"
    const val KEY_IS_LOGGED_IN = "is_logged_in"

    const val KEY_OWNER_STATUS = "owner_status"
    const val KEY_OWNER_FIELDS = "owner_fields"

    const val KEY_FIELD_ID = "field_id"
    const val KEY_BOOKING_ID = "booking_id"
    const val KEY_CHAT_ID = "chat_id"
}
