package com.utama.findfutsall.ui.detail

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.utama.findfutsall.R
import com.utama.findfutsall.adapter.PaymentMethodSelectAdapter
import com.utama.findfutsall.data.api.ApiClient
import com.utama.findfutsall.data.model.PaymentMethod
import com.utama.findfutsall.databinding.ActivityPaymentBinding
import com.utama.findfutsall.utils.PriceFormatter
import com.utama.findfutsall.utils.SessionManager
import kotlinx.coroutines.launch

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private lateinit var session: SessionManager
    private lateinit var paymentAdapter: PaymentMethodSelectAdapter

    private var fieldId      = 0
    private var fieldName    = ""
    private var fieldAddress = ""
    private var fieldDate    = ""
    private var fieldStart   = ""
    private var fieldEnd     = ""
    private var total        = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        fieldId      = intent.getIntExtra("field_id", 0)
        fieldName    = intent.getStringExtra("field_name") ?: ""
        fieldAddress = intent.getStringExtra("field_address") ?: ""
        fieldDate    = intent.getStringExtra("field_date") ?: ""
        fieldStart   = intent.getStringExtra("field_start") ?: ""
        fieldEnd     = intent.getStringExtra("field_end") ?: ""
        val fieldPrice = intent.getIntExtra("field_price", 0)
        val fieldPhoto = intent.getStringExtra("field_photo") ?: ""
        val serviceFee = 5000
        total = fieldPrice + serviceFee

        setupUI(fieldPhoto, fieldPrice)
        setupPaymentMethods()
        loadPaymentMethods()

        binding.btnBack.setOnClickListener { finish() }

        binding.btnConfirm.setOnClickListener {
            goToPaymentInstruction()
        }
    }

    private fun setupUI(photoUrl: String, price: Int) {
        binding.tvFieldName.text    = fieldName
        binding.tvFieldAddress.text = fieldAddress
        binding.tvFieldDate.text    = fieldDate
        binding.tvFieldTime.text    = "$fieldStart - $fieldEnd"
        binding.tvRincianSewa.text  = PriceFormatter.format(price)
        binding.tvTotalBayar.text   = PriceFormatter.format(total)

        if (photoUrl.isNotEmpty()) {
            Glide.with(this)
                .load(photoUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image_error)
                .centerCrop()
                .into(binding.ivFieldPhoto)
        }
    }

    private fun setupPaymentMethods() {
        paymentAdapter = PaymentMethodSelectAdapter(emptyList()) { /* tidak perlu aksi tambahan saat pilih */ }
        binding.rvPaymentMethods.layoutManager = LinearLayoutManager(this)
        binding.rvPaymentMethods.adapter = paymentAdapter
    }

    private fun loadPaymentMethods() {
        binding.progressPaymentMethods.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = ApiClient.instance.getOwnerPaymentMethods(
                    mapOf("field_id" to fieldId)
                )
                binding.progressPaymentMethods.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body["success"] == true) {
                        @Suppress("UNCHECKED_CAST")
                        val list = body["data"] as? List<Map<String, Any>> ?: emptyList()
                        val methods = list.map {
                            PaymentMethod(
                                id            = (it["id"] as? Double)?.toInt() ?: 0,
                                type          = it["type"]?.toString() ?: "",
                                accountNumber = it["account_number"]?.toString() ?: "",
                                accountName   = it["account_name"]?.toString() ?: ""
                            )
                        }
                        paymentAdapter.updateData(methods)
                    }
                }
            } catch (e: Exception) {
                binding.progressPaymentMethods.visibility = View.GONE
                Toast.makeText(this@PaymentActivity, "Gagal memuat metode pembayaran", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun goToPaymentInstruction() {
        val selected = paymentAdapter.getSelected()
        if (selected == null) {
            Toast.makeText(this, "Pilih metode pembayaran dulu", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, PaymentInstructionActivity::class.java).apply {
            putExtra("field_id",       fieldId)
            putExtra("field_name",     fieldName)
            putExtra("field_date",     fieldDate)
            putExtra("field_start",    fieldStart)
            putExtra("field_end",      fieldEnd)
            putExtra("total",          total)
            putExtra("payment_type",          selected.type)
            putExtra("payment_account_number", selected.accountNumber)
            putExtra("payment_account_name",   selected.accountName)
        }
        startActivity(intent)
    }
}