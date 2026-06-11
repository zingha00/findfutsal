package com.utama.findfutsall.ui.owner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.utama.findfutsall.R
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
        // Restore data jika user kembali dari step 2
        val activity = requireActivity() as RegisterOwnerActivity
        val data = activity.step1Data
        if (data.isNotEmpty()) {
            binding.etNamaLapangan.setText(data["nama_lapangan"])
            binding.etAlamat.setText(data["alamat"])
            binding.etNoTelepon.setText(data["no_telepon"])
            binding.etDeskripsi.setText(data["deskripsi"])
        }
    }

    override fun validate(): Boolean {
        val nama = binding.etNamaLapangan.text.toString().trim()
        val alamat = binding.etAlamat.text.toString().trim()
        val noTelp = binding.etNoTelepon.text.toString().trim()

        return when {
            nama.isEmpty() -> {
                binding.etNamaLapangan.error = "Nama lapangan wajib diisi"
                binding.etNamaLapangan.requestFocus()
                false
            }
            alamat.isEmpty() -> {
                binding.etAlamat.error = "Alamat wajib diisi"
                binding.etAlamat.requestFocus()
                false
            }
            noTelp.isEmpty() -> {
                binding.etNoTelepon.error = "Nomor telepon wajib diisi"
                binding.etNoTelepon.requestFocus()
                false
            }
            noTelp.length < 9 -> {
                binding.etNoTelepon.error = "Nomor telepon tidak valid"
                binding.etNoTelepon.requestFocus()
                false
            }
            else -> {
                // Simpan data ke Activity
                val activity = requireActivity() as RegisterOwnerActivity
                activity.step1Data["nama_lapangan"] = nama
                activity.step1Data["alamat"] = alamat
                activity.step1Data["kota"] = binding.actvKota.text.toString()
                activity.step1Data["no_telepon"] = noTelp
                activity.step1Data["deskripsi"] = binding.etDeskripsi.text.toString().trim()
                true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}