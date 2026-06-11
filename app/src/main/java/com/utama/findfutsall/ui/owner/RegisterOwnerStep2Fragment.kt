package com.utama.findfutsall.ui.owner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.utama.findfutsall.databinding.FragmentRegisterOwnerStep2Binding

class RegisterOwnerStep2Fragment : RegisterOwnerBaseFragment() {

    private var _binding: FragmentRegisterOwnerStep2Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterOwnerStep2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDropdowns()
    }

    private fun setupDropdowns() {
        // Jumlah Lapangan
        val jumlahList = (1..10).map { "$it Lapangan" }
        val adapterJumlah = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            jumlahList
        )
        binding.actvJumlahLapangan.setAdapter(adapterJumlah)
        binding.actvJumlahLapangan.setText("1 Lapangan", false)

        // Jam Buka
        val jamList = (0..23).map { String.format("%02d:00", it) }
        val adapterJam = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            jamList
        )
        binding.actvJamBuka.setAdapter(adapterJam)
        binding.actvJamBuka.setText("06:00", false)

        // Jam Tutup (adapter sama)
        val adapterJamTutup = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            jamList
        )
        binding.actvJamTutup.setAdapter(adapterJamTutup)
        binding.actvJamTutup.setText("23:00", false)
    }

    override fun validate(): Boolean {
        val hargaStr = binding.etHargaPerJam.text.toString().trim()

        // Validasi minimal 1 jenis permukaan dipilih
        val anyPermukaan = listOf(
            binding.cbRumputSintetis, binding.cbParquet,
            binding.cbVinyl, binding.cbSemen
        ).any { it.isChecked }

        return when {
            !anyPermukaan -> {
                showToast("Pilih minimal 1 jenis permukaan")
                false
            }
            hargaStr.isEmpty() -> {
                binding.etHargaPerJam.error = "Harga per jam wajib diisi"
                binding.etHargaPerJam.requestFocus()
                false
            }
            else -> {
                // Simpan ke Activity
                val activity = requireActivity() as RegisterOwnerActivity

                val permukaan = mutableListOf<String>()
                if (binding.cbRumputSintetis.isChecked) permukaan.add("Rumput Sintetis")
                if (binding.cbParquet.isChecked) permukaan.add("Parquet")
                if (binding.cbVinyl.isChecked) permukaan.add("Vinyl")
                if (binding.cbSemen.isChecked) permukaan.add("Semen")

                val fasilitas = mutableListOf<String>()
                if (binding.cbParkir.isChecked) fasilitas.add("Parkir Luas")
                if (binding.cbToilet.isChecked) fasilitas.add("Toilet")
                if (binding.cbRuangGanti.isChecked) fasilitas.add("Ruang Ganti")
                if (binding.cbKantin.isChecked) fasilitas.add("Kantin")
                if (binding.cbWifi.isChecked) fasilitas.add("WiFi")
                if (binding.cbCctv.isChecked) fasilitas.add("CCTV")

                val jumlahText = binding.actvJumlahLapangan.text.toString()
                val jumlah = jumlahText.filter { it.isDigit() }.toIntOrNull() ?: 1

                activity.step2Data["jenis_permukaan"] = permukaan
                activity.step2Data["jumlah_lapangan"] = jumlah
                activity.step2Data["harga_per_jam"] = hargaStr.replace(".", "").toLongOrNull() ?: 0L
                activity.step2Data["jam_buka"] = binding.actvJamBuka.text.toString()
                activity.step2Data["jam_tutup"] = binding.actvJamTutup.text.toString()
                activity.step2Data["fasilitas"] = fasilitas
                true
            }
        }
    }

    private fun showToast(msg: String) {
        android.widget.Toast.makeText(requireContext(), msg, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}