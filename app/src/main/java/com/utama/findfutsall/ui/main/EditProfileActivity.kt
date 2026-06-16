package com.utama.findfutsall.ui.main

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.utama.findfutsall.databinding.ActivityEditProfileBinding
import com.utama.findfutsall.utils.Constants
import com.utama.findfutsall.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var sessionManager: SessionManager
    private var selectedPhotoUri: Uri? = null

    companion object {
        const val RESULT_PROFILE_UPDATED = 200
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedPhotoUri = result.data?.data
            selectedPhotoUri?.let {
                Glide.with(this).load(it).circleCrop().into(binding.ivPhoto)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupUserData()
        setupLoginType()
        setupClickListeners()
    }

    private fun setupUserData() {
        binding.etName.setText(sessionManager.getUserName() ?: "")
        binding.etPhone.setText(sessionManager.getUserPhone() ?: "")
        binding.tvUsername.text = sessionManager.getUserUsername() ?: "-"
        binding.tvEmail.text    = sessionManager.getUserEmail() ?: "-"

        val photo = sessionManager.getUserPhoto()
        if (!photo.isNullOrEmpty()) {
            Glide.with(this)
                .load(photo)
                .circleCrop()
                .placeholder(com.utama.findfutsall.R.drawable.ic_profile)
                .into(binding.ivPhoto)
        }
    }

    private fun setupLoginType() {
        // Sembunyikan ganti password jika login Google (tidak ada password = token Google)
        val token = sessionManager.getToken() ?: ""
        val isGoogle = token.startsWith("google_")
        binding.cardChangePassword.visibility = if (isGoogle) View.GONE else View.VISIBLE
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnChangePhoto.setOnClickListener { openGallery() }
        binding.ivPhoto.setOnClickListener { openGallery() }

        binding.menuChangePassword.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        binding.btnSave.setOnClickListener { saveProfile() }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun saveProfile() {
        val name  = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()

        if (name.isEmpty()) {
            binding.etName.error = "Nama tidak boleh kosong"
            return
        }

        binding.btnSave.isEnabled = false
        binding.btnSave.text = "Menyimpan..."

        lifecycleScope.launch {
            try {
                val userId = sessionManager.getUserId()
                val url    = Constants.BASE_URL + "edit_profile.php"

                val multipart = MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("user_id", userId.toString())
                    .addFormDataPart("name", name)
                    .addFormDataPart("phone", phone)

                // Attach foto jika dipilih
                selectedPhotoUri?.let { uri ->
                    val file = uriToFile(uri)
                    if (file != null) {
                        multipart.addFormDataPart(
                            "photo", file.name,
                            file.asRequestBody("image/*".toMediaTypeOrNull())
                        )
                    }
                }

                val request = Request.Builder()
                    .url(url)
                    .post(multipart.build())
                    .build()

                val response = withContext(Dispatchers.IO) {
                    OkHttpClient().newCall(request).execute()
                }

                val resBody = JSONObject(response.body?.string() ?: "{}")

                withContext(Dispatchers.Main) {
                    if (resBody.optBoolean("success")) {
                        val data = resBody.optJSONObject("data")
                        data?.let {
                            sessionManager.updateName(it.optString("name", name))
                            sessionManager.updatePhone(it.optString("phone", phone))
                            val photo = it.optString("photo", "")
                            if (photo.isNotEmpty()) sessionManager.updatePhoto(photo)
                        }
                        Toast.makeText(this@EditProfileActivity, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_PROFILE_UPDATED)
                        finish()
                    } else {
                        Toast.makeText(this@EditProfileActivity, resBody.optString("message", "Gagal menyimpan"), Toast.LENGTH_SHORT).show()
                        binding.btnSave.isEnabled = true
                        binding.btnSave.text = "Simpan"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "Simpan"
                }
            }
        }
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val file = File(cacheDir, "photo_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            null
        }
    }
}