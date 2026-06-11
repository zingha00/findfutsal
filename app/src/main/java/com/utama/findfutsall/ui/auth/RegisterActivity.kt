package com.utama.findfutsall.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.utama.findfutsall.MainActivity
import com.utama.findfutsall.R
import com.utama.findfutsall.databinding.ActivityRegisterBinding
import com.utama.findfutsall.utils.SessionManager
import com.utama.findfutsall.viewmodel.AuthViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var googleSignInClient: GoogleSignInClient
    private val viewModel: AuthViewModel by viewModels()

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            viewModel.googleAuth(
                name = account.displayName ?: "",
                email = account.email ?: "",
                photo = account.photoUrl?.toString() ?: "",
                token = account.idToken ?: ""
            )
        } catch (e: ApiException) {
            Toast.makeText(this, "Google Sign-In gagal: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        setupGoogleSignIn()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val namaLengkap = binding.etNamaLengkap.text.toString().trim()
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val nomorHp = binding.etNomorHp.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            var hasError = false

            if (namaLengkap.isEmpty()) {
                binding.tilNamaLengkap.error = "Nama lengkap tidak boleh kosong"
                hasError = true
            } else {
                binding.tilNamaLengkap.error = null
            }

            if (username.isEmpty()) {
                binding.tilUsername.error = "Username tidak boleh kosong"
                hasError = true
            } else if (username.length < 3) {
                binding.tilUsername.error = "Username minimal 3 karakter"
                hasError = true
            } else {
                binding.tilUsername.error = null
            }

            if (email.isEmpty()) {
                binding.tilEmail.error = "Email tidak boleh kosong"
                hasError = true
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.error = "Email tidak valid"
                hasError = true
            } else {
                binding.tilEmail.error = null
            }

            if (nomorHp.isEmpty()) {
                binding.tilNomorHp.error = "Nomor HP tidak boleh kosong"
                hasError = true
            } else if (nomorHp.length < 10) {
                binding.tilNomorHp.error = "Nomor HP minimal 10 digit"
                hasError = true
            } else {
                binding.tilNomorHp.error = null
            }

            if (password.isEmpty()) {
                binding.tilPassword.error = "Password tidak boleh kosong"
                hasError = true
            } else if (password.length < 8) {
                binding.tilPassword.error = "Password minimal 8 karakter"
                hasError = true
            } else {
                binding.tilPassword.error = null
            }

            if (confirmPassword.isEmpty()) {
                binding.tilConfirmPassword.error = "Konfirmasi password tidak boleh kosong"
                hasError = true
            } else if (confirmPassword != password) {
                binding.tilConfirmPassword.error = "Password tidak sama"
                hasError = true
            } else {
                binding.tilConfirmPassword.error = null
            }

            if (hasError) return@setOnClickListener

            viewModel.register(
                namaLengkap = namaLengkap,
                username = username,
                email = email,
                nomorHp = nomorHp,
                password = password,
                passwordConfirmation = confirmPassword
            )
        }

        binding.btnGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        binding.tvMasuk.setOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.btnRegister.isEnabled = !isLoading
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.registerResult.observe(this) { result ->
            result?.onSuccess { response ->
                if (response.success) {
                    Toast.makeText(this, "Registrasi berhasil! Silakan login menggunakan akun yang telah dibuat.", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                }
            }
            result?.onFailure { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.googleAuthResult.observe(this) { result ->
            result.onSuccess { response ->
                if (response.success) {
                    response.user?.let { user ->
                        sessionManager.saveLoginSession(
                            token = response.token ?: "",
                            userId = user.id,
                            name = user.name,
                            username = user.username,
                            email = user.email,
                            phone = user.nomorHp ?: user.phone,
                            photo = user.photo,
                            role = user.role
                        )
                    }
                    Toast.makeText(this, "Registrasi Google berhasil!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                } else {
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                }
            }
            result.onFailure { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
