package com.utama.findfutsall.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.utama.findfutsall.R
import com.utama.findfutsall.databinding.ActivityPaymentInstructionBinding
import com.utama.findfutsall.utils.Constants
import com.utama.findfutsall.utils.PriceFormatter
import com.utama.findfutsall.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.create
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

class PaymentInstructionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentInstructionBinding
    private lateinit var sessionManager: SessionManager
    private var countDownTimer: CountDownTimer? = null
    private var selectedImageUri: Uri? = null
    private var selectedImageFile: File? = null

    private var fieldId    = 0
    private var fieldName  = ""
    private var fieldDate  = ""
    private var fieldStart = ""
    private var fieldEnd   = ""
    private var total      = 0
    private var paymentType = ""

    companion object {
        const val COUNTDOWN_MILLIS = 15 * 60 * 1000L // 15 menit
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            selectedImageFile = uriToFile(uri)
            showImagePreview(uri)
            updateContinueButtonState()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentInstructionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        fieldId    = intent.getIntExtra("field_id", 0)
        fieldName  = intent.getStringExtra("field_name") ?: ""
        fieldDate  = intent.getStringExtra("field_date") ?: ""
        fieldStart = intent.getStringExtra("field_start") ?: ""
        fieldEnd   = intent.getStringExtra("field_end") ?: ""
        total      = intent.getIntExtra("total", 0)
        paymentType = intent.getStringExtra("payment_type") ?: ""
        val accountNumber = intent.getStringExtra("payment_account_number") ?: ""
        val accountName   = intent.getStringExtra("payment_account_name") ?: ""

        setupUI(accountNumber, accountName)
        startCountdown()

        binding.btnBack.setOnClickListener { confirmCancel() }
        binding.btnCancel.setOnClickListener { confirmCancel() }

        binding.layoutUploadBox.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnCopy.setOnClickListener {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("Nomor", accountNumber)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Nomor disalin!", Toast.LENGTH_SHORT).show()
        }

        binding.btnContinue.setOnClickListener {
            submitBooking()
        }
    }

    private fun setupUI(accountNumber: String, accountName: String) {
        binding.tvTotalBayar.text = PriceFormatter.format(total)

        if (paymentType.equals("QRIS", ignoreCase = true)) {
            binding.tvPaymentTypeTitle.text = "Scan QRIS untuk Membayar"
            binding.ivQris.visibility = android.view.View.VISIBLE
            binding.layoutAccountNumber.visibility = android.view.View.GONE
            binding.tvAccountName.text = accountName.ifEmpty { "Scan untuk membayar" }
        } else {
            binding.tvPaymentTypeTitle.text = "Transfer ke $paymentType"
            binding.ivQris.visibility = android.view.View.GONE
            binding.layoutAccountNumber.visibility = android.view.View.VISIBLE
            binding.tvAccountNumber.text = accountNumber
            binding.tvAccountName.text = "a.n. $accountName"
        }
    }

    private fun startCountdown() {
        countDownTimer = object : CountDownTimer(COUNTDOWN_MILLIS, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                binding.tvCountdown.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                binding.tvCountdown.text = "00:00"
                Toast.makeText(
                    this@PaymentInstructionActivity,
                    "Waktu pembayaran habis, booking dibatalkan",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
        countDownTimer?.start()
    }

    private fun showImagePreview(uri: Uri) {
        binding.ivPreview.visibility = android.view.View.VISIBLE
        binding.tvUploadHint.visibility = android.view.View.GONE
        binding.ivUploadIcon.visibility = android.view.View.GONE
        Glide.with(this).load(uri).centerCrop().into(binding.ivPreview)
    }

    private fun updateContinueButtonState() {
        binding.btnContinue.isEnabled = selectedImageFile != null
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val file = File(cacheDir, "proof_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { output -> inputStream.copyTo(output) }
            file
        } catch (e: Exception) { null }
    }

    private fun confirmCancel() {
        AlertDialog.Builder(this)
            .setTitle("Batalkan Booking?")
            .setMessage("Booking ini belum diproses sama sekali, kamu bisa keluar dengan aman.")
            .setPositiveButton("Ya, Batal") { _, _ -> finish() }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun submitBooking() {
        val file = selectedImageFile
        if (file == null) {
            Toast.makeText(this, "Upload bukti transfer dulu", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnContinue.isEnabled = false
        binding.btnContinue.text = "Memproses..."
        countDownTimer?.cancel()

        val userId       = sessionManager.getUserId()
        val customerName = sessionManager.getUserName() ?: "Customer"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("user_id", userId.toString())
                    .addFormDataPart("field_id", fieldId.toString())
                    .addFormDataPart("customer_name", customerName)
                    .addFormDataPart("play_date", fieldDate)
                    .addFormDataPart("start_time", "$fieldStart:00")
                    .addFormDataPart("end_time", "$fieldEnd:00")
                    .addFormDataPart("total_price", total.toString())
                    .addFormDataPart("payment_method", paymentType)
                    .addFormDataPart(
                        "proof", file.name,
                        file.asRequestBody("image/*".toMediaTypeOrNull())
                    )
                    .build()

                val request = Request.Builder()
                    .url(Constants.BASE_URL + "booking_with_proof.php")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val resStr = response.body?.string() ?: "{}"
                val resJson = JSONObject(resStr)

                withContext(Dispatchers.Main) {
                    if (resJson.optBoolean("success")) {
                        val bookingId = resJson.optInt("booking_id", 0)
                        val intent = Intent(this@PaymentInstructionActivity, BookingSuccessActivity::class.java).apply {
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
                        val msg = resJson.optString("message", "Booking gagal")
                        Toast.makeText(this@PaymentInstructionActivity, msg, Toast.LENGTH_LONG).show()
                        binding.btnContinue.isEnabled = true
                        binding.btnContinue.text = "Lanjutkan"
                        startCountdown()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PaymentInstructionActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.btnContinue.isEnabled = true
                    binding.btnContinue.text = "Lanjutkan"
                    startCountdown()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}