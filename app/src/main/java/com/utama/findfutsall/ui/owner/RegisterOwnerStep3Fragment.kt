package com.utama.findfutsall.ui.owner

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.utama.findfutsall.databinding.FragmentRegisterOwnerStep3Binding

class RegisterOwnerStep3Fragment : RegisterOwnerBaseFragment() {

    private var _binding: FragmentRegisterOwnerStep3Binding? = null
    private val binding get() = _binding!!

    private var fotoUtamaUri: Uri? = null
    private val fotoGaleriUris = mutableListOf<Uri>()

    // Launcher untuk pilih foto utama
    private val pickFotoUtama = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            fotoUtamaUri = it
            Glide.with(this).load(it).centerCrop().into(binding.ivFotoUtama)
        }
    }

    // Launcher untuk pilih foto galeri
    private val pickFotoGaleri = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (fotoGaleriUris.size < 4) {
                fotoGaleriUris.add(it)
                refreshGaleriSlots()
                updateGaleriCount()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterOwnerStep3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        updateSummary()
    }

    private fun setupClickListeners() {
        // Ganti foto utama
        binding.tvGantiFoto.setOnClickListener {
            pickFotoUtama.launch("image/*")
        }
        binding.ivFotoUtama.setOnClickListener {
            pickFotoUtama.launch("image/*")
        }

        // Tambah foto galeri
        binding.btnTambahGaleri.setOnClickListener {
            if (fotoGaleriUris.size < 4) {
                pickFotoGaleri.launch("image/*")
            } else {
                android.widget.Toast.makeText(
                    requireContext(), "Maksimal 4 foto galeri", android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun refreshGaleriSlots() {
        val slots = listOf(
            binding.ivGaleri1, binding.ivGaleri2,
            binding.ivGaleri3, binding.ivGaleri4
        )
        slots.forEachIndexed { index, iv ->
            if (index < fotoGaleriUris.size) {
                iv.visibility = View.VISIBLE
                Glide.with(this).load(fotoGaleriUris[index]).centerCrop().into(iv)
            } else {
                iv.visibility = View.GONE
            }
        }
    }

    private fun updateGaleriCount() {
        binding.tvGaleriCount.text = "Tambahkan foto pendukung (${fotoGaleriUris.size}/4)"
    }

    private fun updateSummary() {
        val activity = requireActivity() as RegisterOwnerActivity
        val step1 = activity.step1Data
        val step2 = activity.step2Data

        binding.tvSummaryNama.text = step1["nama_lapangan"]?.takeIf { it.isNotEmpty() } ?: "-"
        binding.tvSummaryKota.text = step1["kota"] ?: "Bandung"

        val jumlah = step2["jumlah_lapangan"] as? Int ?: 1
        binding.tvSummaryLapangan.text = "$jumlah unit"

        val harga = step2["harga_per_jam"] as? Long ?: 0L
        binding.tvSummaryHarga.text = "Rp ${String.format("%,d", harga).replace(',', '.')}/jam"

        val jamBuka = step2["jam_buka"] as? String ?: "06:00"
        val jamTutup = step2["jam_tutup"] as? String ?: "23:00"
        binding.tvSummaryOperasional.text = "$jamBuka – $jamTutup"
    }

    override fun onResume() {
        super.onResume()
        // Update summary setiap kali step ini ditampilkan
        updateSummary()
    }

    override fun validate(): Boolean {
        // Foto utama opsional untuk sementara (bisa diubah jadi wajib)
        return true
    }

    fun getFotoUtamaUri(): Uri? = fotoUtamaUri
    fun getFotoGaleriUris(): List<Uri> = fotoGaleriUris

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}