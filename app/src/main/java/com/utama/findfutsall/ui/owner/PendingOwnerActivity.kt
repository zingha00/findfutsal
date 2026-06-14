package com.utama.findfutsall.ui.owner

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.utama.findfutsall.ui.main.MainActivity
import com.utama.findfutsall.databinding.ActivityPendingOwnerBinding
import com.utama.findfutsall.ui.auth.LoginActivity
import com.utama.findfutsall.utils.SessionManager

class PendingOwnerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPendingOwnerBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPendingOwnerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        sessionManager.setOwnerStatus("pending")

        binding.btnKembali.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
