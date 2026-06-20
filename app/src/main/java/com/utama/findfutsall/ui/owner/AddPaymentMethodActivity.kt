package com.utama.findfutsall.ui.owner

import android.os.Bundle
import android.view.View
import com.utama.findfutsall.adapter.ProviderDropdownAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.utama.findfutsall.data.api.ApiClient
import com.utama.findfutsall.databinding.ActivityAddPaymentMethodBinding
import com.utama.findfutsall.utils.SessionManager
import kotlinx.coroutines.launch

class AddPaymentMethodActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPaymentMethodBinding
    private lateinit var sessionManager: SessionManager
    private var isBank = true

    private val bankList    = listOf("BCA", "BNI", "BRI", "Mandiri", "CIMB Niaga", "Permata", "BTN")
    private val ewalletList = listOf("OVO", "DANA", "GoPay", "ShopeePay", "LinkAja")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPaymentMethodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupDropdown()
        setupTabs()

        binding.btnBack.setOnClickListener { finish() }
        binding.btnSave.setOnClickListener { save() }
    }

    private fun setupDropdown() {
        updateDropdownItems()
    }

    private fun updateDropdownItems() {
        val items = if (isBank) bankList else ewalletList
        val adapter = ProviderDropdownAdapter(this, items)
        binding.dropdownProvider.setAdapter(adapter)
        binding.dropdownProvider.setText("", false)
        binding.tilProviderName.hint = if (isBank) "Pilih Bank" else "Pilih E-Wallet"
        binding.tilAccountNumber.hint = if (isBank) "Nomor Rekening" else "Nomor E-Wallet"
        binding.dropdownProvider.setOnItemClickListener { _, _, position, _ ->
            binding.dropdownProvider.setText(items[position], false)
        }
    }

    private fun setupTabs() {
        binding.btnTabBank.setOnClickListener {
            isBank = true
            binding.btnTabBank.setBackgroundResource(com.utama.findfutsall.R.drawable.bg_chip_active)
            binding.btnTabBank.setTextColor(getColor(android.R.color.white))
            binding.btnTabEwallet.setBackgroundResource(com.utama.findfutsall.R.drawable.bg_chip_inactive)
            binding.btnTabEwallet.setTextColor(android.graphics.Color.parseColor("#121212"))
            updateDropdownItems()
        }
        binding.btnTabEwallet.setOnClickListener {
            isBank = false
            binding.btnTabEwallet.setBackgroundResource(com.utama.findfutsall.R.drawable.bg_chip_active)
            binding.btnTabEwallet.setTextColor(getColor(android.R.color.white))
            binding.btnTabBank.setBackgroundResource(com.utama.findfutsall.R.drawable.bg_chip_inactive)
            binding.btnTabBank.setTextColor(android.graphics.Color.parseColor("#121212"))
            updateDropdownItems()
        }
    }

    private fun save() {
        val provider      = binding.dropdownProvider.text.toString().trim()
        val accountName   = binding.etAccountName.text.toString().trim()
        val accountNumber = binding.etAccountNumber.text.toString().trim()

        val validList = if (isBank) bankList else ewalletList
        if (provider.isEmpty() || provider !in validList) {
            Toast.makeText(
                this,
                if (isBank) "Pilih bank dari daftar yang tersedia" else "Pilih e-wallet dari daftar yang tersedia",
                Toast.LENGTH_SHORT
            ).show()
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

        lifecycleScope.launch {
            try {
                val response = ApiClient.instance.addPaymentMethod(
                    mapOf(
                        "user_id"        to sessionManager.getUserId(),
                        "type"           to provider,
                        "account_number" to accountNumber,
                        "account_name"   to accountName
                    )
                )
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
                binding.progressBar.visibility = View.GONE
                binding.btnSave.isEnabled      = true
                Toast.makeText(this@AddPaymentMethodActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}