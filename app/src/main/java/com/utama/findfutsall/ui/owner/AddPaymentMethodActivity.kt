package com.utama.findfutsall.ui.owner

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.utama.findfutsall.R
import com.utama.findfutsall.adapter.ProviderSelectAdapter
import com.utama.findfutsall.data.api.ApiClient
import com.utama.findfutsall.databinding.ActivityAddPaymentMethodBinding
import com.utama.findfutsall.utils.SessionManager
import kotlinx.coroutines.launch

class AddPaymentMethodActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPaymentMethodBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var providerAdapter: ProviderSelectAdapter
    private var isBank = true
    private var selectedProvider: String? = null

    private val bankList    = listOf("BCA", "BNI", "BRI", "Mandiri", "SeaBank", "Jago")
    private val ewalletList = listOf("OVO", "DANA", "GoPay")

    private val logoMap = mapOf(
        "BCA"      to R.drawable.logo_bca,
        "BNI"      to R.drawable.logo_bni,
        "BRI"      to R.drawable.logo_bri,
        "Mandiri"  to R.drawable.logo_mandiri,
        "SeaBank"  to R.drawable.logo_seabank,
        "Jago"     to R.drawable.logo_jago,
        "OVO"      to R.drawable.logo_ovo,
        "DANA"     to R.drawable.logo_dana,
        "GoPay"    to R.drawable.logo_gopay
    )

    companion object {
        private const val TAG = "AddPaymentMethod"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPaymentMethodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupProviderList()
        setupTabs()

        binding.btnBack.setOnClickListener { finish() }
        binding.btnSave.setOnClickListener { save() }
    }

    private fun setupProviderList() {
        binding.rvProviders.layoutManager = LinearLayoutManager(this)
        renderProviderList()
    }

    private fun renderProviderList() {
        val items = if (isBank) bankList else ewalletList
        Log.d(TAG, "Render list provider: $items (isBank=$isBank)")

        providerAdapter = ProviderSelectAdapter(items, logoMap) { provider ->
            Log.d(TAG, "Provider terpilih dari adapter: '$provider'")
            selectedProvider = provider
            showSelectedProvider(provider)
        }
        binding.rvProviders.adapter = providerAdapter

        // Reset pilihan setiap kali ganti tab Bank/E-Wallet
        selectedProvider = null
        binding.layoutSelectedProvider.visibility = View.GONE
    }

    private fun showSelectedProvider(provider: String) {
        binding.layoutSelectedProvider.visibility = View.VISIBLE
        binding.tvSelectedProviderName.text = provider
        binding.ivSelectedLogo.setImageResource(logoMap[provider] ?: R.drawable.ic_wallet)
    }

    private fun setupTabs() {
        binding.btnTabBank.setOnClickListener {
            isBank = true
            Log.d(TAG, "Tab diganti ke: Bank")
            binding.btnTabBank.setBackgroundResource(R.drawable.bg_chip_active)
            binding.btnTabBank.setTextColor(getColor(android.R.color.white))
            binding.btnTabEwallet.setBackgroundResource(R.drawable.bg_chip_inactive)
            binding.btnTabEwallet.setTextColor(android.graphics.Color.parseColor("#121212"))
            renderProviderList()
        }
        binding.btnTabEwallet.setOnClickListener {
            isBank = false
            Log.d(TAG, "Tab diganti ke: E-Wallet")
            binding.btnTabEwallet.setBackgroundResource(R.drawable.bg_chip_active)
            binding.btnTabEwallet.setTextColor(getColor(android.R.color.white))
            binding.btnTabBank.setBackgroundResource(R.drawable.bg_chip_inactive)
            binding.btnTabBank.setTextColor(android.graphics.Color.parseColor("#121212"))
            renderProviderList()
        }
    }

    private fun save() {
        val provider      = selectedProvider
        val accountName   = binding.etAccountName.text.toString().trim()
        val accountNumber = binding.etAccountNumber.text.toString().trim()

        Log.d(TAG, "Mencoba simpan -- provider: '$provider', accountName: '$accountName', accountNumber: '$accountNumber'")

        if (provider.isNullOrEmpty()) {
            Toast.makeText(this, "Pilih provider terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }
        if (accountName.isEmpty()) {
            binding.tilAccountName.error = "Nama pemilik wajib diisi"
            return
        }
        if (accountNumber.isEmpty()) {
            binding.tilAccountNumber.error = "Nomor rekening wajib diisi"
            return
        }

        binding.tilAccountName.error   = null
        binding.tilAccountNumber.error = null
        binding.btnSave.isEnabled      = false
        binding.progressBar.visibility = View.VISIBLE

        val requestBody = mapOf(
            "user_id"        to sessionManager.getUserId(),
            "type"           to provider,
            "account_number" to accountNumber,
            "account_name"   to accountName
        )
        Log.d(TAG, "Request body yang dikirim ke server: $requestBody")

        lifecycleScope.launch {
            try {
                val response = ApiClient.instance.addPaymentMethod(requestBody)
                Log.d(TAG, "Response dari server: ${response.body()}")

                binding.progressBar.visibility = View.GONE
                binding.btnSave.isEnabled      = true

                if (response.isSuccessful && response.body()?.get("success") == true) {
                    Toast.makeText(this@AddPaymentMethodActivity, "Metode pembayaran ditambahkan", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val msg = response.body()?.get("message")?.toString() ?: "Gagal menambahkan"
                    Toast.makeText(this@AddPaymentMethodActivity, msg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saat simpan: ${e.message}", e)
                binding.progressBar.visibility = View.GONE
                binding.btnSave.isEnabled      = true
                Toast.makeText(this@AddPaymentMethodActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}