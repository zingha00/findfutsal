package com.utama.findfutsall.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.utama.findfutsall.databinding.FragmentProfileBinding
import com.utama.findfutsall.ui.auth.LoginActivity
import com.utama.findfutsall.ui.owner.OwnerDashboardActivity
import com.utama.findfutsall.ui.owner.RegisterOwnerActivity
import com.utama.findfutsall.utils.SessionManager

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        setupUserData()
        setupOwnerStatus()
        setupClickListeners()
    }

    private fun setupUserData() {
        binding.tvName.text = sessionManager.getUserName() ?: "Nama User"
        binding.tvPhone.text = sessionManager.getUserPhone() ?: "+62 812 3456 7890"
    }

    // ✅ Selalu tampil, hanya sembunyi jika sudah owner
    private fun setupOwnerStatus() {
        val role   = sessionManager.getUserRole()
        val status = sessionManager.getOwnerStatus()

        android.util.Log.d("PROFILE_DEBUG", "role=$role, status=$status")

        when {
            role == "owner" -> {
                binding.cardOwnerStatus.visibility = View.GONE
                binding.cardDaftarPemilik.visibility = View.GONE
            }
            status == "pending" -> {
                binding.cardOwnerStatus.visibility = View.VISIBLE
                binding.cardDaftarPemilik.visibility = View.GONE
                binding.tvOwnerStatusIcon.text = "⏳"
                binding.tvOwnerStatusTitle.text = "Menunggu Verifikasi"
                binding.tvOwnerStatusDesc.text = "Akun pemilik lapangan Anda sedang diproses."
                binding.btnSimulasiVerifikasi.visibility = View.VISIBLE
            }
            else -> {
                binding.cardOwnerStatus.visibility = View.GONE
                binding.cardDaftarPemilik.visibility = View.VISIBLE
            }
        }
    }

    private fun setupClickListeners() {
        binding.menuEditProfile.setOnClickListener {
            Toast.makeText(requireContext(), "Edit profil akan segera hadir", Toast.LENGTH_SHORT).show()
        }

        binding.menuPayment.setOnClickListener {
            Toast.makeText(requireContext(), "Pengaturan pembayaran akan segera hadir", Toast.LENGTH_SHORT).show()
        }

        binding.menuTerms.setOnClickListener {
            Toast.makeText(requireContext(), "Syarat & ketentuan akan segera hadir", Toast.LENGTH_SHORT).show()
        }

        binding.menuHelp.setOnClickListener {
            Toast.makeText(requireContext(), "Pusat bantuan akan segera hadir", Toast.LENGTH_SHORT).show()
        }

        binding.menuPrivacy.setOnClickListener {
            Toast.makeText(requireContext(), "Kebijakan privasi akan segera hadir", Toast.LENGTH_SHORT).show()
        }

        binding.btnDaftarPemilik.setOnClickListener {
            startActivity(Intent(requireContext(), RegisterOwnerActivity::class.java))
        }

        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            val prefs = requireContext().getSharedPreferences("notifikasi", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("notifikasi_aktif", isChecked).apply()
        }

        binding.menuLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Keluar")
                .setMessage("Apakah kamu yakin ingin keluar?")
                .setPositiveButton("Keluar") { _, _ ->
                    sessionManager.clearSession()
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                    requireActivity().finishAffinity()
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        setupUserData()
        setupOwnerStatus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}