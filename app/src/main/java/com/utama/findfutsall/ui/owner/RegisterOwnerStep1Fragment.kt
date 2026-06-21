package com.utama.findfutsall.ui.owner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.utama.findfutsall.databinding.FragmentRegisterOwnerStep1Binding

class RegisterOwnerStep1Fragment : RegisterOwnerBaseFragment() {

    private var _binding: FragmentRegisterOwnerStep1Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterOwnerStep1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupKotaDropdown()
        restoreData()
    }

    private fun setupKotaDropdown() {
        val kotaList = listOf("Bandung") // Sementara hanya Bandung
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            kotaList
        )
        binding.actvKota.setAdapter(adapter)
        binding.actvKota.setText("Bandung", false)
    }

    private fun restoreData() {
        val activity = requireActivity() as RegisterOwnerActivity
        val data = activity.step1Data
        if (data.isNotEmpty()) {
            binding.etNamaLapangan.setText(data["nama_lapangan"])
            binding.etAlamat.setText(data["alamat"])
            binding.etNoTelepon.setText(data["no_telepon"])
            binding.etDeskripsi.setText(data["deskripsi"])
            binding.etMapsLink.setText(data["maps_link"])
        }
    }

    /**
     * Validasi diperketat -- sebelumnya cuma cek .isEmpty(), sehingga
     * input asal seperti "sepz" (4 karakter) lolos sebagai nama venue.
     * Sekarang ada batas MINIMAL PANJANG yang masuk akal untuk tiap field,
     * supaya data yang masuk lebih realistis.
     */
    override fun validate(): Boolean {
        val nama = binding.etNamaLapangan.text.toString().trim()
        val alamat = binding.etAlamat.text.toString().trim()
        val noTelp = binding.etNoTelepon.text.toString().trim()
        val mapsLink = binding.etMapsLink.text.toString().trim()

        return when {
            nama.isEmpty() -> {
                binding.etNamaLapangan.error = "Nama venue wajib diisi"
                binding.etNamaLapangan.requestFocus()
                false
            }
            nama.length < 5 -> {
                binding.etNamaLapangan.error = "Nama venue minimal 5 karakter"
                binding.etNamaLapangan.requestFocus()
                false
            }
            alamat.isEmpty() -> {
                binding.etAlamat.error = "Alamat wajib diisi"
                binding.etAlamat.requestFocus()
                false
            }
            alamat.length < 10 -> {
                binding.etAlamat.error = "Alamat terlalu singkat, mohon tulis lebih lengkap"
                binding.etAlamat.requestFocus()
                false
            }
            // Link Maps OPSIONAL -- tapi kalau diisi, harus format URL yang valid
            mapsLink.isNotEmpty() && !mapsLink.startsWith("http") -> {
                binding.etMapsLink.error = "Link harus dimulai dengan http:// atau https://"
                binding.etMapsLink.requestFocus()
                false
            }
            noTelp.isEmpty() -> {
                binding.etNoTelepon.error = "Nomor telepon wajib diisi"
                binding.etNoTelepon.requestFocus()
                false
            }
            !noTelp.all { it.isDigit() } -> {
                binding.etNoTelepon.error = "Nomor telepon hanya boleh berisi angka"
                binding.etNoTelepon.requestFocus()
                false
            }
            noTelp.length < 9 || noTelp.length > 14 -> {
                binding.etNoTelepon.error = "Nomor telepon tidak valid (9-14 digit)"
                binding.etNoTelepon.requestFocus()
                false
            }
            else -> {
                val activity = requireActivity() as RegisterOwnerActivity
                activity.step1Data["nama_lapangan"] = nama
                activity.step1Data["alamat"] = alamat
                activity.step1Data["kota"] = binding.actvKota.text.toString()
                activity.step1Data["no_telepon"] = noTelp
                activity.step1Data["deskripsi"] = binding.etDeskripsi.text.toString().trim()
                activity.step1Data["maps_link"] = mapsLink
                true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}