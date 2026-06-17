package com.utama.findfutsall.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.utama.findfutsall.databinding.ActivitySecurityPrivacyBinding
import com.utama.findfutsall.utils.SessionManager

class SecurityPrivacyActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecurityPrivacyBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecurityPrivacyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupLoginType()
        setupClickListeners()
    }

    private fun setupLoginType() {
        val token    = sessionManager.getToken() ?: ""
        val isGoogle = token.startsWith("google_")

        if (isGoogle) {
            binding.menuChangePassword.visibility = View.GONE
            binding.layoutGoogleInfo.visibility   = View.VISIBLE
        } else {
            binding.menuChangePassword.visibility = View.VISIBLE
            binding.layoutGoogleInfo.visibility   = View.GONE
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.menuChangePassword.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }
    }
}