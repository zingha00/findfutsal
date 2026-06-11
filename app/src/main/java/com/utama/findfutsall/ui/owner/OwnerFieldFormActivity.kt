package com.utama.findfutsall.ui.owner

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.utama.findfutsall.data.model.Field
import com.utama.findfutsall.databinding.ActivityOwnerFieldFormBinding
import com.utama.findfutsall.utils.SessionManager

class OwnerFieldFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOwnerFieldFormBinding
    private lateinit var sessionManager: SessionManager
    private var editingFieldId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOwnerFieldFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupExistingData()
        setupSaveButton()
    }

    private fun setupExistingData() {
        editingFieldId = if (intent.hasExtra("field_id")) {
            binding.toolbar.title = "Edit Lapangan"
            binding.etName.setText(intent.getStringExtra("field_name"))
            binding.etAddress.setText(intent.getStringExtra("field_address"))
            binding.etPhone.setText(intent.getStringExtra("field_phone"))
            binding.etPrice.setText(intent.getIntExtra("field_price", 0).toString())
            binding.etCategory.setText(intent.getStringExtra("field_category"))
            binding.etDescription.setText(intent.getStringExtra("field_description"))
            binding.etFacilities.setText(intent.getStringExtra("field_facilities"))
            binding.etOpenTime.setText(intent.getStringExtra("field_openTime"))
            binding.etCloseTime.setText(intent.getStringExtra("field_closeTime"))
            binding.etPhoto.setText(intent.getStringExtra("field_photo"))
            intent.getIntExtra("field_id", 0)
        } else {
            binding.toolbar.title = "Tambah Lapangan"
            null
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val address = binding.etAddress.text.toString().trim()
            val priceStr = binding.etPrice.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val category = binding.etCategory.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            val facilities = binding.etFacilities.text.toString().trim()
            val openTime = binding.etOpenTime.text.toString().trim()
            val closeTime = binding.etCloseTime.text.toString().trim()
            val photo = binding.etPhoto.text.toString().trim()

            if (name.isEmpty()) {
                binding.tilName.error = "Nama lapangan wajib diisi"
                return@setOnClickListener
            }
            if (address.isEmpty()) {
                binding.tilAddress.error = "Alamat wajib diisi"
                return@setOnClickListener
            }
            if (priceStr.isEmpty()) {
                binding.tilPrice.error = "Harga wajib diisi"
                return@setOnClickListener
            }

            val price = priceStr.toIntOrNull()
            if (price == null || price <= 0) {
                binding.tilPrice.error = "Harga harus angka positif"
                return@setOnClickListener
            }

            val currentFields = sessionManager.getOwnerFields().toMutableList()

            if (editingFieldId != null) {
                val index = currentFields.indexOfFirst { it.id == editingFieldId }
                if (index != -1) {
                    val updatedField = Field(
                        id = editingFieldId!!,
                        name = name,
                        address = address,
                        price = price,
                        rating = currentFields[index].rating,
                        photo = photo.ifEmpty { null },
                        category = category.ifEmpty { "Futsal" },
                        distance = currentFields[index].distance,
                        phone = phone.ifEmpty { null },
                        description = description.ifEmpty { null },
                        facilities = facilities.ifEmpty { null },
                        openTime = openTime.ifEmpty { null },
                        closeTime = closeTime.ifEmpty { null }
                    )
                    currentFields[index] = updatedField
                    sessionManager.saveOwnerFields(currentFields)
                    Toast.makeText(this, "Lapangan berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
            } else {
                val newField = Field(
                    id = sessionManager.getNextFieldId(),
                    name = name,
                    address = address,
                    price = price,
                    rating = 0f,
                    photo = photo.ifEmpty { null },
                    category = category.ifEmpty { "Futsal" },
                    distance = null,
                    phone = phone.ifEmpty { null },
                    description = description.ifEmpty { null },
                    facilities = facilities.ifEmpty { null },
                    openTime = openTime.ifEmpty { null },
                    closeTime = closeTime.ifEmpty { null }
                )
                currentFields.add(newField)
                sessionManager.saveOwnerFields(currentFields)
                Toast.makeText(this, "Lapangan berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        if (hasChanges()) {
            AlertDialog.Builder(this)
                .setTitle("Batal")
                .setMessage("Apakah Anda yakin ingin membatalkan? Perubahan tidak akan disimpan.")
                .setPositiveButton("Ya") { _, _ -> super.onBackPressed() }
                .setNegativeButton("Tidak", null)
                .show()
        } else {
            super.onBackPressed()
        }
    }

    private fun hasChanges(): Boolean {
        return !binding.etName.text.isNullOrEmpty() ||
                !binding.etAddress.text.isNullOrEmpty() ||
                !binding.etPrice.text.isNullOrEmpty()
    }
}
