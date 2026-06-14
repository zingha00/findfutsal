package com.utama.findfutsall.ui.detail

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.utama.findfutsall.data.api.ApiClient
import com.utama.findfutsall.databinding.ActivityPaymentBinding
import com.utama.findfutsall.utils.SessionManager
import kotlinx.coroutines.launch

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private lateinit var session: SessionManager
    private var selectedPayment = "transfer"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        val fieldId      = intent.getIntExtra("field_id", 0)
        val fieldName    = intent.getStringExtra("field_name") ?: ""
        val fieldAddress = intent.getStringExtra("field_address") ?: ""
        val fieldDate    = intent.getStringExtra("field_date") ?: ""
        val fieldStart   = intent.getStringExtra("field_start") ?: ""
        val fieldEnd     = intent.getStringExtra("field_end") ?: ""
        val fieldPrice   = intent.getIntExtra("field_price", 0)
        val serviceFee   = 5000
        val total        = fieldPrice + serviceFee

        setupUI(fieldName, fieldAddress, fieldDate, fieldStart, fieldEnd, fieldPrice, total)
        setupPaymentSelection()

        binding.btnBack.setOnClickListener { finish() }

        binding.btnCopy.setOnClickListener {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("VA", binding.tvVirtualAccount.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Nomor VA disalin!", Toast.LENGTH_SHORT).show()
        }

        binding.btnConfirm.setOnClickListener {
            doBooking(fieldId, fieldName, fieldDate, fieldStart, fieldEnd, total)
        }
    }

    private fun setupUI(
        name: String, address: String, date: String,
        start: String, end: String, price: Int, total: Int
    ) {
        binding.tvFieldName.text    = name
        binding.tvFieldAddress.text = address
        binding.tvFieldDate.text    = date
        binding.tvFieldTime.text    = "$start - $end (1 Jam)"
        binding.tvRincianSewa.text  = "Rp ${formatPrice(price)}"
        binding.tvTotalBayar.text   = "Rp ${formatPrice(total)}"
        binding.tvVirtualAccount.text = "8077 0812 3456 7890"
    }

    private fun setupPaymentSelection() {
        binding.cardBca.setOnClickListener {
            selectedPayment = "transfer"
            binding.rbBca.isChecked  = true
            binding.rbOvo.isChecked  = false
            binding.rbDana.isChecked = false
        }
        binding.cardOvo.setOnClickListener {
            selectedPayment = "ovo"
            binding.rbBca.isChecked  = false
            binding.rbOvo.isChecked  = true
            binding.rbDana.isChecked = false
        }
        binding.cardDana.setOnClickListener {
            selectedPayment = "dana"
            binding.rbBca.isChecked  = false
            binding.rbOvo.isChecked  = false
            binding.rbDana.isChecked = true
        }
    }

    private fun doBooking(
        fieldId: Int, fieldName: String, fieldDate: String,
        fieldStart: String, fieldEnd: String, total: Int
    ) {
        val userId       = session.getUserId()
        val customerName = session.getUserName() ?: "Customer"

        if (fieldId == 0 || fieldDate.isEmpty() || fieldStart.isEmpty()) {
            Toast.makeText(this, "Data booking tidak lengkap", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnConfirm.isEnabled = false
        binding.btnConfirm.text      = "Memproses..."

        lifecycleScope.launch {
            try {
                val response = ApiClient.instance.createBooking(
                    mapOf(
                        "user_id"       to userId,
                        "field_id"      to fieldId,
                        "customer_name" to customerName,
                        "play_date"     to fieldDate,
                        "start_time"    to "$fieldStart:00",
                        "end_time"      to "$fieldEnd:00",
                        "total_price"   to total,
                        "payment_method" to selectedPayment
                    )
                )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body["success"] == true) {
                        val bookingId = (body["booking_id"] as? Double)?.toInt() ?: 0
                        val intent = Intent(this@PaymentActivity, BookingSuccessActivity::class.java).apply {
                            putExtra("field_name",  fieldName)
                            putExtra("field_date",  fieldDate)
                            putExtra("field_start", fieldStart)
                            putExtra("field_end",   fieldEnd)
                            putExtra("total",       total)
                            putExtra("booking_id",  bookingId)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        val msg = body["message"]?.toString() ?: "Booking gagal"
                        Toast.makeText(this@PaymentActivity, msg, Toast.LENGTH_LONG).show()
                        binding.btnConfirm.isEnabled = true
                        binding.btnConfirm.text = "Konfirmasi Pembayaran"
                    }
                } else {
                    Toast.makeText(this@PaymentActivity, "Gagal terhubung ke server", Toast.LENGTH_SHORT).show()
                    binding.btnConfirm.isEnabled = true
                    binding.btnConfirm.text = "Konfirmasi Pembayaran"
                }
            } catch (e: Exception) {
                Toast.makeText(this@PaymentActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.btnConfirm.isEnabled = true
                binding.btnConfirm.text = "Konfirmasi Pembayaran"
            }
        }
    }

    private fun formatPrice(price: Int): String {
        return String.format("%,d", price).replace(",", ".")
    }
}