package com.utama.findfutsall.ui.detail

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.utama.findfutsall.R
import com.utama.findfutsall.ui.main.MainActivity

class BookingSuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_success)

        val fieldName = intent.getStringExtra("field_name") ?: "-"
        val date      = intent.getStringExtra("field_date") ?: "-"
        val start     = intent.getStringExtra("field_start") ?: "-"
        val end       = intent.getStringExtra("field_end") ?: "-"
        val total     = intent.getIntExtra("total", 0)
        val bookingId = intent.getIntExtra("booking_id", 0)

        findViewById<TextView>(R.id.tvSuccessFieldName).text = fieldName
        findViewById<TextView>(R.id.tvSuccessDate).text      = date
        findViewById<TextView>(R.id.tvSuccessTime).text      = "$start - $end"
        findViewById<TextView>(R.id.tvSuccessTotal).text     = "Rp ${formatPrice(total)}"
        findViewById<TextView>(R.id.tvBookingId).text        = "ID Booking: #$bookingId"

        findViewById<Button>(R.id.btnLihatPesanan).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                putExtra("navigate_to", "pesanan")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            })
            finish()
        }

        findViewById<Button>(R.id.btnKembaliHome).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            })
            finish()
        }
    }

    private fun formatPrice(price: Int): String {
        return String.format("%,d", price).replace(",", ".")
    }
}