package com.utama.findfutsall.ui.owner

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.utama.findfutsall.R
import com.utama.findfutsall.data.api.ApiClient
import com.utama.findfutsall.databinding.ActivityOwnerBookingDetailBinding
import com.utama.findfutsall.utils.PriceFormatter
import com.utama.findfutsall.utils.SessionManager
import kotlinx.coroutines.launch

class OwnerBookingDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOwnerBookingDetailBinding
    private lateinit var sessionManager: SessionManager

    companion object {
        const val EXTRA_BOOKING_ID     = "extra_booking_id"
        const val EXTRA_USER_NAME      = "extra_user_name"
        const val EXTRA_USER_PHONE     = "extra_user_phone"
        const val EXTRA_USER_PHOTO     = "extra_user_photo"
        const val EXTRA_USER_EMAIL     = "extra_user_email"
        const val EXTRA_FIELD_NAME     = "extra_field_name"
        const val EXTRA_FIELD_ADDRESS  = "extra_field_address"
        const val EXTRA_FIELD_PHOTO    = "extra_field_photo"
        const val EXTRA_PLAY_DATE      = "extra_play_date"
        const val EXTRA_START_TIME     = "extra_start_time"
        const val EXTRA_END_TIME       = "extra_end_time"
        const val EXTRA_TOTAL_PRICE    = "extra_total_price"
        const val EXTRA_STATUS         = "extra_status"
        const val EXTRA_CREATED_AT     = "extra_created_at"
        const val EXTRA_PAYMENT_METHOD = "extra_payment_method"
        const val EXTRA_PROOF_IMAGE    = "extra_proof_image"
    }

    private var bookingId  = 0
    private var userPhone  = ""
    private var userName   = ""
    private var fieldName  = ""
    private var playDate   = ""
    private var startTime  = ""
    private var endTime    = ""
    private var currentStatus = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOwnerBookingDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        binding.btnBack.setOnClickListener { finish() }

        loadData()
    }

    private fun loadData() {
        bookingId         = intent.getIntExtra(EXTRA_BOOKING_ID, 0)
        userName           = intent.getStringExtra(EXTRA_USER_NAME) ?: "-"
        userPhone          = intent.getStringExtra(EXTRA_USER_PHONE) ?: ""
        val userPhoto      = intent.getStringExtra(EXTRA_USER_PHOTO) ?: ""
        val userEmail      = intent.getStringExtra(EXTRA_USER_EMAIL) ?: "-"
        fieldName          = intent.getStringExtra(EXTRA_FIELD_NAME) ?: "-"
        val fieldAddress   = intent.getStringExtra(EXTRA_FIELD_ADDRESS) ?: "-"
        val fieldPhoto     = intent.getStringExtra(EXTRA_FIELD_PHOTO) ?: ""
        playDate           = intent.getStringExtra(EXTRA_PLAY_DATE) ?: "-"
        startTime          = intent.getStringExtra(EXTRA_START_TIME) ?: "-"
        endTime            = intent.getStringExtra(EXTRA_END_TIME) ?: "-"
        val totalPrice     = intent.getDoubleExtra(EXTRA_TOTAL_PRICE, 0.0)
        currentStatus      = intent.getStringExtra(EXTRA_STATUS) ?: "-"
        val createdAt      = intent.getStringExtra(EXTRA_CREATED_AT) ?: "-"
        val paymentMethod  = intent.getStringExtra(EXTRA_PAYMENT_METHOD) ?: ""
        val proofImage     = intent.getStringExtra(EXTRA_PROOF_IMAGE) ?: ""

        binding.tvBookingId.text    = "#BK-${bookingId.toString().padStart(3, '0')}"
        binding.tvUserName.text     = userName
        binding.tvUserPhone.text    = userPhone.ifEmpty { "Nomor tidak tersedia" }
        binding.tvUserEmail.text    = userEmail
        binding.tvFieldName.text    = fieldName
        binding.tvFieldAddress.text = fieldAddress
        binding.tvDate.text         = playDate
        binding.tvTime.text         = "$startTime - $endTime"
        binding.tvCreatedAt.text    = createdAt
        binding.tvTotalPrice.text   = PriceFormatter.format(totalPrice)
        binding.tvStatus.text       = currentStatus.uppercase()

        setStatusColor(currentStatus)
        loadUserPhoto(userPhoto)
        loadFieldPhoto(fieldPhoto)
        setupProofOfPayment(paymentMethod, proofImage)
        setupActionButtons()

        binding.btnWhatsapp.setOnClickListener { openWhatsapp() }
    }

    private fun setupProofOfPayment(paymentMethod: String, proofImage: String) {
        if (proofImage.isEmpty()) {
            binding.cardProof.visibility = View.GONE
            return
        }
        binding.cardProof.visibility = View.VISIBLE
        binding.tvPaymentMethod.text = "Metode: ${paymentMethod.ifEmpty { "-" }}"

        Glide.with(this)
            .load(proofImage)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.placeholder_image_error)
            .centerCrop()
            .into(binding.ivPaymentProof)
    }

    private fun setupActionButtons() {
        val canTakeAction = currentStatus.equals("Menunggu", ignoreCase = true)
        binding.layoutAksi.visibility = if (canTakeAction) View.VISIBLE else View.GONE

        binding.btnKonfirmasi.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Konfirmasi Booking")
                .setMessage("Konfirmasi booking dari $userName? Pastikan bukti pembayaran sudah benar.")
                .setPositiveButton("Konfirmasi") { _, _ -> updateStatus("Terkonfirmasi") }
                .setNegativeButton("Batal", null)
                .show()
        }

        binding.btnTolak.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Tolak Booking")
                .setMessage("Tolak booking dari $userName?")
                .setPositiveButton("Tolak") { _, _ -> updateStatus("Batal") }
                .setNegativeButton("Tidak", null)
                .show()
        }
    }

    private fun updateStatus(newStatus: String) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.instance.updateBookingStatus(
                    mapOf(
                        "booking_id" to bookingId,
                        "status"     to newStatus,
                        "user_id"    to sessionManager.getUserId()
                    )
                )
                if (response.isSuccessful && response.body()?.get("success") == true) {
                    Toast.makeText(this@OwnerBookingDetailActivity, "Booking $newStatus", Toast.LENGTH_SHORT).show()
                    currentStatus = newStatus
                    binding.tvStatus.text = newStatus.uppercase()
                    setStatusColor(newStatus)
                    binding.layoutAksi.visibility = View.GONE
                } else {
                    val msg = response.body()?.get("message")?.toString() ?: "Gagal update status"
                    Toast.makeText(this@OwnerBookingDetailActivity, msg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@OwnerBookingDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setStatusColor(status: String) {
        when (status.lowercase()) {
            "terkonfirmasi" -> {
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

    private fun loadUserPhoto(photoUrl: String) {
        if (photoUrl.isNotEmpty()) {
            Glide.with(this)
                .load(photoUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .circleCrop()
                .into(binding.ivUserPhoto)
        } else {
            binding.ivUserPhoto.setImageResource(R.drawable.ic_profile)
        }
    }

    private fun loadFieldPhoto(photoUrl: String) {
        if (photoUrl.isNotEmpty()) {
            Glide.with(this)
                .load(photoUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image_error)
                .centerCrop()
                .into(binding.ivFieldPhoto)
        } else {
            binding.ivFieldPhoto.setImageResource(R.drawable.placeholder_image)
        }
    }

    private fun openWhatsapp() {
        val phone = userPhone.replace(Regex("[^0-9]"), "")
        if (phone.isEmpty()) {
            Toast.makeText(this, "Nomor telepon tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        val waPhone = if (phone.startsWith("0")) "62${phone.substring(1)}" else phone
        val message = "Halo $userName, terkait booking lapangan $fieldName pada $playDate pukul $startTime-$endTime."
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$waPhone?text=${Uri.encode(message)}")))
    }
}