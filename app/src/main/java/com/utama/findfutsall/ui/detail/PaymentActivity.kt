package com.utama.findfutsall.ui.detail

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.utama.findfutsall.databinding.ActivityPaymentBinding

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private var selectedPayment = "transfer"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fieldName = intent.getStringExtra("field_name") ?: ""
        val fieldAddress = intent.getStringExtra("field_address") ?: ""
        val fieldDate = intent.getStringExtra("field_date") ?: ""
        val fieldTime = intent.getStringExtra("field_time") ?: ""
        val fieldPrice = intent.getIntExtra("field_price", 0)
        val serviceFee = 5000
        val total = fieldPrice + serviceFee

        setupUI(fieldName, fieldAddress, fieldDate, fieldTime, fieldPrice, total)
        setupClickListeners(total)
    }

    private fun setupUI(
        name: String, address: String, date: String,
        time: String, price: Int, total: Int
    ) {
        binding.tvFieldName.text = name
        binding.tvFieldAddress.text = address
        binding.tvFieldDate.text = date
        binding.tvFieldTime.text = "$time (1 Jam)"
        binding.tvRincianSewa.text = "Rp ${formatPrice(price)}"
        binding.tvTotalBayar.text = "Rp ${formatPrice(total)}"
        binding.tvVirtualAccount.text = "8077 0812 3456 7890"
    }

    private fun setupClickListeners(total: Int) {
        binding.btnBack.setOnClickListener { finish() }

        binding.cardDana.setOnClickListener {
            selectedPayment = "dana"
            updatePaymentSelection()
        }

        binding.cardOvo.setOnClickListener {
            selectedPayment = "ovo"
            updatePaymentSelection()
        }

        binding.cardBca.setOnClickListener {
            selectedPayment = "transfer"
            updatePaymentSelection()
        }

        binding.btnCopy.setOnClickListener {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("VA", binding.tvVirtualAccount.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Nomor VA disalin!", Toast.LENGTH_SHORT).show()
        }

        binding.btnConfirm.setOnClickListener {
            Toast.makeText(this, "Pembayaran dikonfirmasi!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun updatePaymentSelection() {
        // Reset semua
        binding.rbDana.isChecked = false
        binding.rbOvo.isChecked = false
        binding.rbBca.isChecked = false

        // Aktifkan yang dipilih
        when (selectedPayment) {
            "dana" -> binding.rbDana.isChecked = true
            "ovo" -> binding.rbOvo.isChecked = true
            "transfer" -> binding.rbBca.isChecked = true
        }
    }

    private fun formatPrice(price: Int): String {
        return String.format("%,d", price).replace(",", ".")
    }
}