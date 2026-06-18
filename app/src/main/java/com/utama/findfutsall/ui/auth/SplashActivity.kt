package com.utama.findfutsall.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.utama.findfutsall.ui.main.MainActivity
import com.utama.findfutsall.databinding.ActivitySplashBinding
import com.utama.findfutsall.ui.owner.OwnerDashboardActivity
import com.utama.findfutsall.utils.SessionManager

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // Load dark mode sebelum setContentView
        val isDark = getSharedPreferences("app_pref", Context.MODE_PRIVATE)
            .getBoolean("dark_mode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        Handler(Looper.getMainLooper()).postDelayed({
            try {
                if (sessionManager.isLoggedIn()) {
                    val intent = when {
                        sessionManager.isOwner() -> Intent(this, OwnerDashboardActivity::class.java)
                        else                     -> Intent(this, MainActivity::class.java)
                    }
                    startActivity(intent)
                } else {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                finish()
            } catch (e: Exception) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }, 2000)
    }
}