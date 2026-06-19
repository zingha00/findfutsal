package com.utama.findfutsall.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.utama.findfutsall.ui.main.MainActivity
import com.utama.findfutsall.R
import com.utama.findfutsall.databinding.ActivityLoginBinding
import com.utama.findfutsall.ui.owner.OwnerDashboardActivity
import com.utama.findfutsall.utils.SessionManager
import com.utama.findfutsall.viewmodel.AuthViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
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
                name  = account.displayName ?: "",
                email = account.email ?: "",
                photo = account.photoUrl?.toString() ?: "",
                token = account.idToken ?: ""
            )
        } catch (e: ApiException) {
            // Kalau user menekan back/batal di halaman pilih akun Google,
            // ini SELALU melempar ApiException dengan kode SIGN_IN_CANCELLED.
            // Itu BUKAN error sungguhan -- jangan tampilkan toast untuk kasus ini,
            // supaya user tidak bingung melihat "gagal" padahal dia cuma berubah pikiran.
            if (e.statusCode != GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                Toast.makeText(this, "Google Sign-In gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        setupGoogleSignIn()
        setupClickListeners()
        setupTextWatchers()
        observeViewModel()
    }

    private fun setupTextWatchers() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tilIdentifier.error = null
                binding.tilPassword.error   = null
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        binding.etIdentifier.addTextChangedListener(watcher)
        binding.etPassword.addTextChangedListener(watcher)
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
        binding.btnLogin.setOnClickListener {
            val identifier = binding.etIdentifier.text.toString().trim()
            val password   = binding.etPassword.text.toString().trim()

            if (identifier.isEmpty()) {
                binding.tilIdentifier.error = "Username, Email, atau Nomor HP tidak boleh kosong"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.tilPassword.error = "Kata Sandi tidak boleh kosong"
                return@setOnClickListener
            }

            binding.tilIdentifier.error = null
            binding.tilPassword.error   = null
            viewModel.login(identifier, password)
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        binding.tvDaftar.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.btnGoogle.setOnClickListener {
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }
    }

    private fun navigateAfterLogin() {
        val intent = when {
            sessionManager.isOwner() -> Intent(this, OwnerDashboardActivity::class.java)
            else                     -> Intent(this, MainActivity::class.java)
        }
        startActivity(intent)
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.btnLogin.isEnabled     = !isLoading
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.loginResult.observe(this) { result ->
            result?.onSuccess { response ->
                if (response.success) {
                    response.user?.let { user ->
                        sessionManager.clearSession()
                        sessionManager.saveLoginSession(
                            token    = response.token ?: "",
                            userId   = user.id,
                            name     = user.name,
                            username = user.username,
                            email    = user.email,
                            phone    = user.nomorHp ?: user.phone,
                            photo    = user.photo,
                            role     = user.role
                        )
                    }
                    navigateAfterLogin()
                    finishAffinity()
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
                        sessionManager.clearSession()
                        sessionManager.saveLoginSession(
                            token    = response.token ?: "",
                            userId   = user.id,
                            name     = user.name,
                            username = user.username,
                            email    = user.email,
                            phone    = user.nomorHp ?: user.phone,
                            photo    = user.photo,
                            role     = user.role
                        )
                    }
                    Toast.makeText(this, "Login Google berhasil!", Toast.LENGTH_SHORT).show()
                    navigateAfterLogin()
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