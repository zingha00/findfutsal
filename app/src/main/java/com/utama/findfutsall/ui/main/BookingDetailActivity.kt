package com.utama.findfutsall.ui.main

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.utama.findfutsall.R
import com.utama.findfutsall.data.api.ApiClient
import com.utama.findfutsall.databinding.ActivityBookingDetailBinding
import com.utama.findfutsall.utils.Constants
import com.utama.findfutsall.utils.SessionManager
import kotlinx.coroutines.launch

class BookingDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingDetailBinding
    private lateinit var sessionManager: SessionManager
    private var currentBookingId = 0
    private var currentStatus = ""

    companion object {
        const val EXTRA_BOOKING_ID     = "extra_booking_id"
        const val EXTRA_FIELD_NAME     = "extra_field_name"
        const val EXTRA_FIELD_ADDRESS  = "extra_field_address"
        const val EXTRA_FIELD_PHOTO    = "extra_field_photo"
        const val EXTRA_DATE           = "extra_date"
        const val EXTRA_TIME           = "extra_time"
        const val EXTRA_PRICE          = "extra_price"
        const val EXTRA_STATUS         = "extra_status"
        const val EXTRA_CREATED_AT     = "extra_created_at"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        binding.btnBack.setOnClickListener { finish() }

        loadData()
    }

    private fun loadData() {
        currentBookingId = intent.getIntExtra(EXTRA_BOOKING_ID, 0)
        val fieldName    = intent.getStringExtra(EXTRA_FIELD_NAME) ?: "-"
        val fieldAddress = intent.getStringExtra(EXTRA_FIELD_ADDRESS) ?: "-"
        val fieldPhoto   = intent.getStringExtra(EXTRA_FIELD_PHOTO) ?: ""
        val date         = intent.getStringExtra(EXTRA_DATE) ?: "-"
        val time         = intent.getStringExtra(EXTRA_TIME) ?: "-"
        val price        = intent.getStringExtra(EXTRA_PRICE) ?: "Rp 0"
        currentStatus    = intent.getStringExtra(EXTRA_STATUS) ?: "-"
        val createdAt    = intent.getStringExtra(EXTRA_CREATED_AT) ?: "-"

        binding.tvBookingId.text    = "#${currentBookingId.toString().padStart(4, '0')}"
        binding.tvFieldName.text    = fieldName
        binding.tvFieldAddress.text = fieldAddress
        binding.tvDate.text         = date
        binding.tvTime.text         = time
        binding.tvTotalPrice.text   = price
        binding.tvCreatedAt.text    = createdAt
        binding.tvStatus.text       = currentStatus.uppercase()

        setStatusColor(currentStatus)
        loadPhoto(fieldPhoto)
        setupCancelButton()
    }

    private fun setupCancelButton() {
        val canCancel = currentStatus.equals("Menunggu", ignoreCase = true) ||
                currentStatus.equals("Terkonfirmasi", ignoreCase = true)

        binding.btnCancel.visibility = if (canCancel) android.view.View.VISIBLE else android.view.View.GONE

        binding.btnCancel.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Batalkan Pesanan")
                .setMessage("Apakah Anda yakin ingin membatalkan pesanan ini? Pembatalan hanya dapat dilakukan minimal 2 jam sebelum waktu bermain.")
                .setPositiveButton("Ya, Batalkan") { _, _ -> doCancelBooking() }
                .setNegativeButton("Tidak", null)
                .show()
        }
    }

    private fun doCancelBooking() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.instance.cancelBooking(
                    mapOf(
                        "booking_id" to currentBookingId,
                        "user_id"    to sessionManager.getUserId()
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body["success"] == true) {
                        Toast.makeText(this@BookingDetailActivity, "Pesanan berhasil dibatalkan", Toast.LENGTH_SHORT).show()
                        currentStatus = "Batal"
                        binding.tvStatus.text = "BATAL"
                        setStatusColor(currentStatus)
                        binding.btnCancel.visibility = android.view.View.GONE
                    } else {
                        val message = body["message"]?.toString() ?: "Gagal membatalkan pesanan"
                        Toast.makeText(this@BookingDetailActivity, message, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@BookingDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setStatusColor(status: String) {
        when (status.lowercase()) {
            "terkonfirmasi", "mendatang" -> {
                binding.tvStatus.setBackgroundResource(R.drawable.bg_chip_active)
                binding.tvStatus.setTextColor(Color.WHITE)
            }
            "menunggu" -> {
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_selesai)
                binding.tvStatus.setTextColor(Color.parseColor("#B45309"))
            }
            "selesai" -> {
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_selesai)
                binding.tvStatus.setTextColor(Color.parseColor("#00A86B"))
            }
            "batal" -> {
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_batal)
                binding.tvStatus.setTextColor(Color.parseColor("#FF3B30"))
            }
        }
    }

    private fun loadPhoto(photoPath: String) {
        val baseUrl = Constants.BASE_URL.replace("/api/", "/")
        val fullUrl = when {
            photoPath.isEmpty()          -> null
            photoPath.startsWith("http") -> photoPath
            else                         -> baseUrl + photoPath
        }

        if (fullUrl != null) {
            Glide.with(this)
                .load(fullUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.bg_logo)
                .error(R.drawable.findfutsall)
                .centerCrop()
                .into(binding.ivFieldPhoto)
        } else {
            binding.ivFieldPhoto.setImageResource(R.drawable.findfutsall)
        }
    }
}