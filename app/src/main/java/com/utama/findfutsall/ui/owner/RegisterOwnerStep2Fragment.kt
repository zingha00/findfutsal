package com.utama.findfutsall.ui.owner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.utama.findfutsall.databinding.FragmentRegisterOwnerStep2Binding

/**
 * Step 2 sekarang HANYA berisi data level VENUE: jam operasional dan
 * fasilitas umum. Data level LAPANGAN (harga, jenis permukaan, jumlah
 * lapangan, foto) dipindah ke alur "Tambah Lapangan" terpisah di Dashboard,
 * karena owner bisa punya beberapa lapangan dengan harga/permukaan/foto
 * yang berbeda-beda -- tidak masuk akal dipaksa isi 1 set data di awal daftar.
 */
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
        val jamList = (0..23).map { String.format("%02d:00", it) }

        val adapterJamBuka = ArrayAdapter(
            requireContext(), android.R.layout.simple_dropdown_item_1line, jamList
        )
        binding.actvJamBuka.setAdapter(adapterJamBuka)
        binding.actvJamBuka.setText("06:00", false)

        val adapterJamTutup = ArrayAdapter(
            requireContext(), android.R.layout.simple_dropdown_item_1line, jamList
        )
        binding.actvJamTutup.setAdapter(adapterJamTutup)
        binding.actvJamTutup.setText("23:00", false)
    }

    override fun validate(): Boolean {
        val jamBuka  = binding.actvJamBuka.text.toString()
        val jamTutup = binding.actvJamTutup.text.toString()

        // Validasi sederhana: jam tutup harus lebih besar dari jam buka
        val bukaHour  = jamBuka.split(":")[0].toIntOrNull() ?: 0
        val tutupHour = jamTutup.split(":")[0].toIntOrNull() ?: 0

        if (tutupHour <= bukaHour) {
            Toast.makeText(requireContext(), "Jam tutup harus lebih besar dari jam buka", Toast.LENGTH_SHORT).show()
            return false
        }

        val fasilitas = mutableListOf<String>()
        if (binding.cbParkir.isChecked) fasilitas.add("Parkir Luas")
        if (binding.cbToilet.isChecked) fasilitas.add("Toilet")
        if (binding.cbRuangGanti.isChecked) fasilitas.add("Ruang Ganti")
        if (binding.cbKantin.isChecked) fasilitas.add("Kantin")
        if (binding.cbWifi.isChecked) fasilitas.add("WiFi")
        if (binding.cbCctv.isChecked) fasilitas.add("CCTV")

        val activity = requireActivity() as RegisterOwnerActivity
        activity.step2Data["jam_buka"]  = jamBuka
        activity.step2Data["jam_tutup"] = jamTutup
        activity.step2Data["fasilitas"] = fasilitas

        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}