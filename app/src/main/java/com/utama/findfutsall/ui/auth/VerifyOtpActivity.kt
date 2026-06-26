package com.utama.findfutsall.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.utama.findfutsall.databinding.ActivityVerifyOtpBinding
import com.utama.findfutsall.viewmodel.AuthViewModel

class VerifyOtpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerifyOtpBinding
    private val viewModel: AuthViewModel by viewModels()
    private var email = ""
    private lateinit var otpBoxes: List<EditText>
    private var countDownTimer: CountDownTimer? = null

    companion object {
        const val EXTRA_EMAIL = "extra_email"
        const val RESEND_COOLDOWN_SECONDS = 60
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        email = intent.getStringExtra(EXTRA_EMAIL) ?: ""
        binding.tvEmailInfo.text = "Kode telah dikirim ke $email"

        setupOtpBoxes()
        setupClickListeners()
        observeViewModel()
        startResendCooldown()
    }

    private fun setupOtpBoxes() {
        otpBoxes = listOf(
            binding.etOtp1, binding.etOtp2, binding.etOtp3,
            binding.etOtp4, binding.etOtp5, binding.etOtp6
        )

        otpBoxes.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && index < otpBoxes.size - 1) {
                        // Otomatis pindah fokus ke kotak berikutnya
                        otpBoxes[index + 1].requestFocus()
                    }
                }
            })

            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN
                    && editText.text.isEmpty() && index > 0) {
                    otpBoxes[index - 1].requestFocus()
                    otpBoxes[index - 1].setText("")
                    true
                } else {
                    false
                }
            }
        }

        // Fokus otomatis ke kotak pertama saat halaman dibuka
        otpBoxes[0].requestFocus()
    }

    private fun getOtpValue(): String {
        return otpBoxes.joinToString("") { it.text.toString() }
    }

    private fun clearOtpBoxes() {
        otpBoxes.forEach { it.setText("") }
        otpBoxes[0].requestFocus()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnVerify.setOnClickListener {
            val otp = getOtpValue()

            if (otp.length != 6) {
                Toast.makeText(this, "Masukkan 6 digit kode OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.verifyOtp(email, otp)
        }

        binding.tvResendOtp.setOnClickListener {
            if (binding.tvResendOtp.isEnabled) {
                viewModel.forgotPassword(email)
                clearOtpBoxes()
                Toast.makeText(this, "Mengirim ulang kode OTP...", Toast.LENGTH_SHORT).show()
                startResendCooldown()
            }
        }
    }

    private fun startResendCooldown() {
        binding.tvResendOtp.isEnabled = false
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(RESEND_COOLDOWN_SECONDS * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                binding.tvResendOtp.text = "Kirim Ulang Kode OTP (${secondsLeft}s)"
                binding.tvResendOtp.alpha = 0.5f
            }

            override fun onFinish() {
                binding.tvResendOtp.text = "Kirim Ulang Kode OTP"
                binding.tvResendOtp.alpha = 1f
                binding.tvResendOtp.isEnabled = true
            }
        }
        countDownTimer?.start()
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.btnVerify.isEnabled  = !isLoading
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.verifyOtpResult.observe(this) { result ->
            if (result == null) return@observe

            result.onSuccess {
                val otp = getOtpValue()
                val intent = Intent(this, ResetPasswordActivity::class.java).apply {
                    putExtra(ResetPasswordActivity.EXTRA_EMAIL, email)
                    putExtra(ResetPasswordActivity.EXTRA_OTP, otp)
                }
                startActivity(intent)
                viewModel.resetVerifyOtpResult()
            }
            result.onFailure { error ->
                Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
                clearOtpBoxes()
                viewModel.resetVerifyOtpResult()
            }
        }

        // Hasil kirim ulang OTP (reuse forgotPasswordResult karena pakai endpoint yang sama)
        viewModel.forgotPasswordResult.observe(this) { result ->
            if (result == null) return@observe

            result.onSuccess { message ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                viewModel.resetForgotPasswordResult()
            }
            result.onFailure { error ->
                Toast.makeText(this, "Gagal kirim ulang: ${error.message}", Toast.LENGTH_SHORT).show()
                viewModel.resetForgotPasswordResult()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}