package com.utama.findfutsall.ui.owner

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.utama.findfutsall.data.api.ApiClient
import com.utama.findfutsall.data.model.OwnerRegisterRequest
import com.utama.findfutsall.databinding.ActivityRegisterOwnerBinding
import com.utama.findfutsall.ui.auth.LoginActivity
import com.utama.findfutsall.utils.SessionManager
import kotlinx.coroutines.launch

/**
 * Pendaftaran owner sekarang HANYA 2 langkah (sebelumnya 3):
 * 1. Info Venue (nama, alamat, kota, telepon, deskripsi)
 * 2. Jam Operasional + Fasilitas Umum
 *
 * Step lama "Foto Lapangan" DIHAPUS dari sini -- detail per-lapangan
 * (harga, jenis permukaan, foto) sekarang diisi lewat alur "Tambah Lapangan"
 * terpisah di Dashboard, karena owner bisa punya banyak lapangan dengan
 * data berbeda-beda, tidak masuk akal dipaksa isi 1 set data di pendaftaran.
 */
class RegisterOwnerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterOwnerBinding
    private lateinit var sessionManager: SessionManager

    val step1Data = mutableMapOf<String, String>()
    val step2Data = mutableMapOf<String, Any>()

    private val stepLabels = listOf(
        "Langkah 1 dari 2 — Info Venue",
        "Langkah 2 dari 2 — Operasional & Fasilitas"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterOwnerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupViewPager()
        setupStepper()
        setupButtons()
    }

    private fun setupViewPager() {
        val fragments = listOf(
            RegisterOwnerStep1Fragment(),
            RegisterOwnerStep2Fragment()
        )

        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = fragments.size
            override fun createFragment(position: Int): Fragment = fragments[position]
        }

        binding.viewPager.isUserInputEnabled = false
    }

    private fun setupStepper() {
        binding.viewPager.registerOnPageChangeCallback(object :
            androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateStepper(position)
                binding.tvStepLabel.text = stepLabels[position]
                binding.btnNext.text = if (position == 1) "✓  Daftar Sekarang" else "Lanjutkan  ›"
            }
        })
    }

    private fun updateStepper(currentPage: Int) {
        val stepViews = listOf(binding.tvStep1, binding.tvStep2)
        val doneDrawable = resources.getDrawable(com.utama.findfutsall.R.drawable.bg_step_done, theme)
        val activeDrawable = resources.getDrawable(com.utama.findfutsall.R.drawable.bg_step_active, theme)
        val inactiveDrawable = resources.getDrawable(com.utama.findfutsall.R.drawable.bg_step_inactive, theme)

        stepViews.forEachIndexed { index, tv ->
            when {
                index < currentPage -> {
                    tv.background = doneDrawable
                    tv.text = "✓"
                    tv.setTextColor(0xFFFFFFFF.toInt())
                }
                index == currentPage -> {
                    tv.background = activeDrawable
                    tv.text = (index + 1).toString()
                    tv.setTextColor(0xFF1A4D2E.toInt())
                }
                else -> {
                    tv.background = inactiveDrawable
                    tv.text = (index + 1).toString()
                    tv.setTextColor(0xFFFFFFFF.toInt())
                }
            }
        }
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener {
            val current = binding.viewPager.currentItem
            if (current == 0) finish()
            else binding.viewPager.currentItem = current - 1
        }

        binding.btnNext.setOnClickListener {
            val current = binding.viewPager.currentItem
            val currentFragment = getCurrentFragment(current)

            if (currentFragment?.validate() == true) {
                if (current < 1) {
                    binding.viewPager.currentItem = current + 1
                } else {
                    submitRegistration()
                }
            }
        }
    }

    private fun getCurrentFragment(position: Int): RegisterOwnerBaseFragment? {
        return supportFragmentManager.findFragmentByTag("f$position") as? RegisterOwnerBaseFragment
    }

    private fun submitRegistration() {
        val venueName   = step1Data["nama_lapangan"] ?: ""
        val address     = step1Data["alamat"] ?: ""
        val city        = step1Data["kota"] ?: "Bandung"
        val phone       = step1Data["no_telepon"] ?: ""
        val description = step1Data["deskripsi"] ?: ""

        val openTime  = step2Data["jam_buka"] as? String ?: "06:00"
        val closeTime = step2Data["jam_tutup"] as? String ?: "23:00"
        @Suppress("UNCHECKED_CAST")
        val facilities = step2Data["fasilitas"] as? List<String> ?: emptyList()

        val userId = sessionManager.getUserId()

        // Field level-lapangan (jenis_permukaan, jumlah_lapangan, harga_per_jam)
        // tidak lagi dikirim di sini -- nilai default/placeholder dikirim
        // supaya backend tetap kompatibel, lapangan sungguhan ditambahkan
        // nanti lewat fitur "Tambah Lapangan".
        val request = OwnerRegisterRequest(
            user_id         = userId,
            nama_lapangan   = venueName,
            alamat          = address,
            kota            = city,
            no_telepon      = phone,
            deskripsi       = description,
            jenis_permukaan = emptyList(),
            jumlah_lapangan = 0,
            harga_per_jam   = 0L,
            jam_buka        = openTime,
            jam_tutup       = closeTime,
            fasilitas       = facilities
        )

        binding.btnNext.isEnabled = false
        binding.btnNext.text = "Mendaftarkan..."

        lifecycleScope.launch {
            try {
                val response = ApiClient.instance.registerOwner(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    sessionManager.updateRole("owner")
                    sessionManager.setOwnerStatus("approved")
                    showSuccessDialog()
                } else {
                    val msg = response.body()?.message ?: "Pendaftaran gagal"
                    Toast.makeText(this@RegisterOwnerActivity, msg, Toast.LENGTH_LONG).show()
                    binding.btnNext.isEnabled = true
                    binding.btnNext.text = "✓  Daftar Sekarang"
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@RegisterOwnerActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                binding.btnNext.isEnabled = true
                binding.btnNext.text = "✓  Daftar Sekarang"
            }
        }
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("🎉 Pendaftaran Berhasil!")
            .setMessage(
                "Selamat! Anda telah terdaftar sebagai pemilik venue.\n\n" +
                        "Selanjutnya, tambahkan lapangan kamu lewat menu \"Tambah Lapangan\" " +
                        "di Dashboard untuk mulai menerima booking.\n\n" +
                        "Silakan login ulang untuk masuk ke dashboard pemilik."
            )
            .setCancelable(false)
            .setPositiveButton("Login Ulang") { _, _ ->
                sessionManager.clearSession()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .show()
    }

    override fun onBackPressed() {
        val current = binding.viewPager.currentItem
        if (current == 0) super.onBackPressed()
        else binding.viewPager.currentItem = current - 1
    }
}