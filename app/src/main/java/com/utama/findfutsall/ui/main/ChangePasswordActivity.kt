package com.utama.findfutsall.ui.main

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.utama.findfutsall.databinding.ActivityChangePasswordBinding
import com.utama.findfutsall.utils.Constants
import com.utama.findfutsall.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * Toggle show/hide password sekarang pakai app:endIconMode="password_toggle"
 * bawaan Material Design TextInputLayout -- SAMA seperti di halaman Login.
 * Tidak perlu lagi ImageView manual + logic toggle Kotlin, karena
 * sebelumnya ikon yang dipakai salah (ic_favorite, bukan ikon mata).
 */
class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnSubmit.setOnClickListener { submitChangePassword() }
    }

    private fun submitChangePassword() {
        val oldPass = binding.etOldPassword.text.toString().trim()
        val newPass = binding.etNewPassword.text.toString().trim()
        val confirm = binding.etConfirmPassword.text.toString().trim()

        if (oldPass.isEmpty()) {
            binding.tilOldPassword.error = "Password lama wajib diisi"
            return
        }
        binding.tilOldPassword.error = null

        if (newPass.length < 6) {
            binding.tilNewPassword.error = "Minimal 6 karakter"
            return
        }
        binding.tilNewPassword.error = null

        if (newPass != confirm) {
            binding.tilConfirmPassword.error = "Konfirmasi tidak cocok"
            return
        }
        binding.tilConfirmPassword.error = null

        binding.tvSubmitText.text = "Menyimpan..."
        binding.btnSubmit.isEnabled = false

        lifecycleScope.launch {
            try {
                val url = Constants.BASE_URL + "change_password.php"
                val json = JSONObject().apply {
                    put("user_id", sessionManager.getUserId())
                    put("old_password", oldPass)
                    put("new_password", newPass)
                    put("confirm_password", confirm)
                }
                val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder().url(url).post(body).build()

                val response = withContext(Dispatchers.IO) {
                    OkHttpClient().newCall(request).execute()
                }
                val resBody = JSONObject(response.body?.string() ?: "{}")

                withContext(Dispatchers.Main) {
                    if (resBody.optBoolean("success")) {
                        Toast.makeText(this@ChangePasswordActivity, "Password berhasil diubah", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@ChangePasswordActivity, resBody.optString("message", "Gagal mengubah password"), Toast.LENGTH_SHORT).show()
                        binding.tvSubmitText.text = "Simpan Password Baru"
                        binding.btnSubmit.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ChangePasswordActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.tvSubmitText.text = "Simpan Password Baru"
                    binding.btnSubmit.isEnabled = true
                }
            }
        }
    }
}