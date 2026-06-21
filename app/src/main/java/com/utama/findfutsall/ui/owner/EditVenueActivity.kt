package com.utama.findfutsall.ui.owner

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.utama.findfutsall.data.api.ApiClient
import com.utama.findfutsall.databinding.ActivityEditVenueBinding
import com.utama.findfutsall.utils.SessionManager
import kotlinx.coroutines.launch
import java.util.Calendar

class EditVenueActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditVenueBinding
    private lateinit var sessionManager: SessionManager

    companion object {
        const val EXTRA_NAME       = "extra_name"
        const val EXTRA_ADDRESS    = "extra_address"
        const val EXTRA_PHONE      = "extra_phone"
        const val EXTRA_OPEN_TIME  = "extra_open_time"
        const val EXTRA_CLOSE_TIME = "extra_close_time"
        const val EXTRA_PRICE      = "extra_price"
        const val EXTRA_MAPS_LINK  = "extra_maps_link"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditVenueBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        binding.etVenueName.setText(intent.getStringExtra(EXTRA_NAME) ?: "")
        binding.etVenueAddress.setText(intent.getStringExtra(EXTRA_ADDRESS) ?: "")
        binding.etVenuePhone.setText(intent.getStringExtra(EXTRA_PHONE) ?: "")
        binding.etMapsLink.setText(intent.getStringExtra(EXTRA_MAPS_LINK) ?: "")
        binding.etOpenTime.setText(intent.getStringExtra(EXTRA_OPEN_TIME) ?: "06:00")
        binding.etCloseTime.setText(intent.getStringExtra(EXTRA_CLOSE_TIME) ?: "23:00")
        val price = intent.getDoubleExtra(EXTRA_PRICE, 0.0)
        if (price > 0) binding.etVenuePrice.setText(price.toLong().toString())

        binding.btnBack.setOnClickListener { finish() }
        binding.etOpenTime.setOnClickListener { showTimePicker(binding.etOpenTime) }
        binding.etCloseTime.setOnClickListener { showTimePicker(binding.etCloseTime) }
        binding.btnSimpan.setOnClickListener { saveChanges() }
    }

    private fun showTimePicker(target: android.widget.EditText) {
        val current = target.text.toString().split(":")
        val hour = current.getOrNull(0)?.toIntOrNull() ?: 6
        val minute = current.getOrNull(1)?.toIntOrNull() ?: 0

        TimePickerDialog(this, { _, h, m ->
            target.setText(String.format("%02d:%02d", h, m))
        }, hour, minute, true).show()
    }

    private fun saveChanges() {
        val name    = binding.etVenueName.text.toString().trim()
        val address = binding.etVenueAddress.text.toString().trim()
        val phone   = binding.etVenuePhone.text.toString().trim()
        val mapsLink = binding.etMapsLink.text.toString().trim()
        val openTime  = binding.etOpenTime.text.toString().trim()
        val closeTime = binding.etCloseTime.text.toString().trim()
        val price = binding.etVenuePrice.text.toString().trim().toDoubleOrNull() ?: 0.0

        if (name.isEmpty()) {
            binding.tilVenueName.error = "Nama venue wajib diisi"
            return
        }
        if (address.isEmpty()) {
            binding.tilVenueAddress.error = "Alamat wajib diisi"
            return
        }
        binding.tilVenueName.error = null
        binding.tilVenueAddress.error = null

        binding.btnSimpan.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = ApiClient.instance.updateOwnerData(
                    mapOf(
                        "user_id"    to sessionManager.getUserId(),
                        "name"       to name,
                        "address"    to address,
                        "phone"      to phone,
                        "openTime"   to openTime,
                        "closeTime"  to closeTime,
                        "price"      to price,
                        "maps_link"  to mapsLink
                    )
                )
                binding.progressBar.visibility = View.GONE
                binding.btnSimpan.isEnabled = true

                if (response.isSuccessful && response.body()?.get("success") == true) {
                    Toast.makeText(this@EditVenueActivity, "Info venue berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    val msg = response.body()?.get("message")?.toString() ?: "Gagal menyimpan"
                    Toast.makeText(this@EditVenueActivity, msg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.btnSimpan.isEnabled = true
                Toast.makeText(this@EditVenueActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}