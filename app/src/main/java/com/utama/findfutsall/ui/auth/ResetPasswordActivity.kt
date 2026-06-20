package com.utama.findfutsall.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.utama.findfutsall.databinding.ActivityResetPasswordBinding
import com.utama.findfutsall.viewmodel.AuthViewModel

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private val viewModel: AuthViewModel by viewModels()
    private var email = ""
    private var otp   = ""

    companion object {
        const val EXTRA_EMAIL = "extra_email"
        const val EXTRA_OTP   = "extra_otp"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        email = intent.getStringExtra(EXTRA_EMAIL) ?: ""
        otp   = intent.getStringExtra(EXTRA_OTP) ?: ""

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnSubmit.setOnClickListener {
            val newPassword     = binding.etNewPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (newPassword.isEmpty()) {
                binding.tilNewPassword.error = "Password tidak boleh kosong"
                return@setOnClickListener
            }
            if (newPassword.length < 6) {
                binding.tilNewPassword.error = "Password minimal 6 karakter"
                return@setOnClickListener
            }
            if (confirmPassword != newPassword) {
                binding.tilConfirmPassword.error = "Konfirmasi password tidak cocok"
                return@setOnClickListener
            }

            binding.tilNewPassword.error     = null
            binding.tilConfirmPassword.error = null
            viewModel.resetPassword(email, otp, newPassword)
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.btnSubmit.isEnabled    = !isLoading
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.resetPasswordResult.observe(this) { result ->
            if (result == null) return@observe

            result.onSuccess { message ->
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                viewModel.resetResetPasswordResult()

                // Selesai -- balik ke halaman Login, bersihkan semua activity di atasnya
                // (ForgotPassword, VerifyOtp, ResetPassword) supaya user tidak bisa
                // tekan back ke halaman-halaman reset password yang sudah tidak relevan
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            result.onFailure { error ->
                Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
                viewModel.resetResetPasswordResult()
            }
        }
    }
}