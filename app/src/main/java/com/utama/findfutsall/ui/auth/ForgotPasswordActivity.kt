package com.utama.findfutsall.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.utama.findfutsall.databinding.ActivityForgotPasswordBinding
import com.utama.findfutsall.viewmodel.AuthViewModel

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val viewModel: AuthViewModel by viewModels()
    private var currentEmail = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSendOtp.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()

            if (email.isEmpty()) {
                binding.tilEmail.error = "Email tidak boleh kosong"
                return@setOnClickListener
            }

            binding.tilEmail.error = null
            currentEmail = email
            viewModel.forgotPassword(email)
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.btnSendOtp.isEnabled = !isLoading
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.forgotPasswordResult.observe(this) { result ->
            if (result == null) return@observe

            result.onSuccess { message ->
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                viewModel.resetForgotPasswordResult()

                val intent = Intent(this, VerifyOtpActivity::class.java)
                intent.putExtra(VerifyOtpActivity.EXTRA_EMAIL, currentEmail)
                startActivity(intent)
            }
            result.onFailure { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                viewModel.resetForgotPasswordResult()
            }
        }
    }
}