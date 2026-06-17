package com.utama.findfutsall.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.utama.findfutsall.databinding.ActivityHelpCenterBinding

class HelpCenterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHelpCenterBinding

    // TANDA: GANTI nomor WhatsApp di bawah ini sesuai kebutuhan (format: 62xxxxxxxxxx, tanpa + atau 0 di depan)
    private val whatsappNumber = "082148856024" // <-- GANTI NOMOR DI SINI
    private val supportEmail   = "acep.ega@widyatama.ac.id"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpCenterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvEmailAddress.text    = supportEmail
        binding.tvWhatsappNumber.text  = formatPhoneDisplay(whatsappNumber)

        binding.btnBack.setOnClickListener { finish() }

        binding.menuEmail.setOnClickListener { openEmail() }
        binding.menuWhatsapp.setOnClickListener { openWhatsapp() }
    }

    private fun openEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$supportEmail")
            putExtra(Intent.EXTRA_SUBJECT, "Bantuan FindFutsall")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Tidak ada aplikasi email yang terpasang", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openWhatsapp() {
        val url = "https://wa.me/$whatsappNumber"
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Toast.makeText(this, "WhatsApp tidak terpasang", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatPhoneDisplay(raw: String): String {
        // 6281234567890 -> +62 812-3456-7890 (format kasar untuk display)
        return if (raw.startsWith("62")) "+${raw.substring(0,2)} ${raw.substring(2)}" else raw
    }
}