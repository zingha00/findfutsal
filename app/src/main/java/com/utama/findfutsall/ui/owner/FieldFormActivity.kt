package com.utama.findfutsall.ui.owner

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.utama.findfutsall.data.model.Field
import com.utama.findfutsall.databinding.ActivityFieldFormBinding
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
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class FieldFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFieldFormBinding
    private lateinit var sessionManager: SessionManager

    private var photoUri: Uri? = null
    private var editField: Field? = null
    private var isEditMode = false
    private var isFormattingHarga = false

    companion object {
        const val EXTRA_FIELD  = "extra_field"
        const val RESULT_SAVED = 200
    }

    private val pickPhoto = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            photoUri = it
            Glide.with(this).load(it).centerCrop().into(binding.ivPhoto)
            binding.layoutUploadHint.visibility = View.GONE
            binding.tvGantiFoto.visibility      = View.VISIBLE
        }
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFieldFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        editField      = intent.getParcelableExtra(EXTRA_FIELD)
        isEditMode     = editField != null

        setupHeader()
        setupDropdowns()
        setupPhotoUpload()
        setupHargaFormatter()
        setupSaveButton()

        if (isEditMode) populateForm(editField!!)
    }

    private fun setupHeader() {
        binding.tvTitle.text = if (isEditMode) "Edit Lapangan" else "Tambah Lapangan"
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupDropdowns() {
        val jamList = (0..23).map { String.format("%02d:00", it) }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, jamList)
        binding.actvJamBuka.setAdapter(adapter)
        binding.actvJamTutup.setAdapter(adapter)
        binding.actvJamBuka.setText("06:00", false)
        binding.actvJamTutup.setText("23:00", false)
    }

    private fun setupPhotoUpload() {
        binding.framePhoto.setOnClickListener { pickPhoto.launch("image/*") }
        binding.tvGantiFoto.setOnClickListener { pickPhoto.launch("image/*") }
    }

    private fun setupHargaFormatter() {
        binding.etHarga.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isFormattingHarga) return
                isFormattingHarga = true

                val raw    = s.toString().replace(".", "")
                val number = raw.toLongOrNull() ?: 0L
                val formatted = formatRupiah(number)

                binding.etHarga.setText(formatted)
                binding.etHarga.setSelection(formatted.length)

                isFormattingHarga = false
            }
        })
    }

    private fun formatRupiah(number: Long): String {
        if (number == 0L) return ""
        val str    = number.toString()
        val result = StringBuilder()
        str.reversed().forEachIndexed { index, c ->
            if (index > 0 && index % 3 == 0) result.append('.')
            result.append(c)
        }
        return result.reverse().toString()
    }

    private fun populateForm(field: Field) {
        binding.etNama.setText(field.name)
        binding.etAlamat.setText(field.address)
        binding.etTelepon.setText(field.phone ?: "")
        binding.etDeskripsi.setText(field.description ?: "")

        // Format harga saat populate
        isFormattingHarga = true
        binding.etHarga.setText(formatRupiah(field.price.toLong()))
        isFormattingHarga = false

        binding.actvJamBuka.setText(field.openTime ?: "06:00", false)
        binding.actvJamTutup.setText(field.closeTime ?: "23:00", false)

        val photoUrl = field.photo ?: ""
        if (photoUrl.isNotEmpty()) {
            Glide.with(this)
                .load(photoUrl)
                .placeholder(com.utama.findfutsall.R.drawable.field_1)
                .centerCrop()
                .into(binding.ivPhoto)
            binding.layoutUploadHint.visibility = View.GONE
            binding.tvGantiFoto.visibility      = View.VISIBLE
        }

        val surfaces = field.category.split(",").map { it.trim() }
        binding.cbRumputSintetis.isChecked = surfaces.contains("Rumput Sintetis")
        binding.cbParquet.isChecked        = surfaces.contains("Parquet")
        binding.cbVinyl.isChecked          = surfaces.contains("Vinyl")
        binding.cbSemen.isChecked          = surfaces.contains("Semen")

        val fac = field.facilities?.split(",")?.map { it.trim() } ?: emptyList()
        binding.cbParkir.isChecked     = fac.contains("Parkir Luas")
        binding.cbToilet.isChecked     = fac.contains("Toilet")
        binding.cbRuangGanti.isChecked = fac.contains("Ruang Ganti")
        binding.cbKantin.isChecked     = fac.contains("Kantin")
        binding.cbWifi.isChecked       = fac.contains("WiFi")
        binding.cbCctv.isChecked       = fac.contains("CCTV")
    }

    private fun setupSaveButton() {
        binding.btnSimpan.setOnClickListener {
            val nama     = binding.etNama.text.toString().trim()
            val alamat   = binding.etAlamat.text.toString().trim()
            val hargaStr = binding.etHarga.text.toString().trim()

            when {
                nama.isEmpty()     -> { binding.etNama.error   = "Nama lapangan wajib diisi"; return@setOnClickListener }
                alamat.isEmpty()   -> { binding.etAlamat.error = "Alamat wajib diisi"; return@setOnClickListener }
                hargaStr.isEmpty() -> { binding.etHarga.error  = "Harga wajib diisi"; return@setOnClickListener }
                else               -> saveField()
            }
        }
    }

    private fun saveField() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSimpan.isEnabled    = false
        binding.btnSimpan.text         = "Menyimpan..."

        val userId    = sessionManager.getUserId()
        val nama      = binding.etNama.text.toString().trim()
        val alamat    = binding.etAlamat.text.toString().trim()
        val telepon   = binding.etTelepon.text.toString().trim()
        val deskripsi = binding.etDeskripsi.text.toString().trim()
        // Hapus titik sebelum dikirim ke server
        val harga     = binding.etHarga.text.toString().replace(".", "").trim()
        val jamBuka   = binding.actvJamBuka.text.toString().ifEmpty { "06:00" }
        val jamTutup  = binding.actvJamTutup.text.toString().ifEmpty { "23:00" }

        val surfaces = mutableListOf<String>().apply {
            if (binding.cbRumputSintetis.isChecked) add("Rumput Sintetis")
            if (binding.cbParquet.isChecked)        add("Parquet")
            if (binding.cbVinyl.isChecked)          add("Vinyl")
            if (binding.cbSemen.isChecked)          add("Semen")
        }

        val fasilitas = mutableListOf<String>().apply {
            if (binding.cbParkir.isChecked)     add("Parkir Luas")
            if (binding.cbToilet.isChecked)     add("Toilet")
            if (binding.cbRuangGanti.isChecked) add("Ruang Ganti")
            if (binding.cbKantin.isChecked)     add("Kantin")
            if (binding.cbWifi.isChecked)       add("WiFi")
            if (binding.cbCctv.isChecked)       add("CCTV")
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val endpoint = if (isEditMode) "edit_field.php" else "add_field.php"
                val url      = Constants.BASE_URL + endpoint
                val builder  = MultipartBody.Builder().setType(MultipartBody.FORM)

                if (isEditMode) {
                    builder.addFormDataPart("field_id", editField!!.id.toString())
                } else {
                    builder.addFormDataPart("user_id", userId.toString())
                }

                builder.addFormDataPart("name",           nama)
                builder.addFormDataPart("address",        alamat)
                builder.addFormDataPart("phone",          telepon)
                builder.addFormDataPart("description",    deskripsi)
                builder.addFormDataPart("price_per_hour", harga)
                builder.addFormDataPart("open_time",      jamBuka)
                builder.addFormDataPart("close_time",     jamTutup)
                builder.addFormDataPart("category",       surfaces.joinToString(","))
                builder.addFormDataPart("facilities",     fasilitas.joinToString(","))

                photoUri?.let { uri ->
                    val file = uriToFile(uri)
                    builder.addFormDataPart(
                        "photo", file.name,
                        file.asRequestBody("image/*".toMediaTypeOrNull())
                    )
                }

                val request  = Request.Builder().url(url).post(builder.build()).build()
                val response = httpClient.newCall(request).execute()
                val bodyStr  = response.body?.string() ?: ""
                val json     = if (bodyStr.isEmpty())
                    JSONObject("{\"success\":false,\"message\":\"Response kosong dari server\"}")
                else
                    try { JSONObject(bodyStr) }
                    catch (e: Exception) { JSONObject("{\"success\":false,\"message\":\"Parse error\"}") }

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSimpan.isEnabled    = true
                    binding.btnSimpan.text         = "Simpan Lapangan"

                    if (json.optBoolean("success")) {
                        Toast.makeText(
                            this@FieldFormActivity,
                            if (isEditMode) "Lapangan berhasil diperbarui!"
                            else "Lapangan berhasil ditambahkan!",
                            Toast.LENGTH_SHORT
                        ).show()
                        setResult(RESULT_SAVED)
                        finish()
                    } else {
                        Toast.makeText(
                            this@FieldFormActivity,
                            json.optString("message", "Gagal menyimpan"),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSimpan.isEnabled    = true
                    binding.btnSimpan.text         = "Simpan Lapangan"
                    Toast.makeText(
                        this@FieldFormActivity,
                        "Koneksi gagal: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)!!
        val file        = File(cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { output -> inputStream.copyTo(output) }
        return file
    }
}