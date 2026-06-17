package com.utama.findfutsall.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.utama.findfutsall.R
import com.utama.findfutsall.databinding.FragmentProfileBinding
import com.utama.findfutsall.ui.auth.LoginActivity
import com.utama.findfutsall.ui.owner.RegisterOwnerActivity
import com.utama.findfutsall.utils.Constants
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
        binding.tvName.text  = sessionManager.getUserName() ?: "Nama User"
        binding.tvPhone.text = sessionManager.getUserPhone() ?: "-"

        val photo = sessionManager.getUserPhoto()
        if (!photo.isNullOrEmpty()) {
            Glide.with(this)
                .load(photo)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .circleCrop()
                .into(binding.ivPhoto)
        } else {
            binding.ivPhoto.setImageResource(R.drawable.ic_profile)
        }
    }

    private fun setupOwnerStatus() {
        val role   = sessionManager.getUserRole()
        val status = sessionManager.getOwnerStatus()

        when {
            role == "owner" -> {
                binding.cardOwnerStatus.visibility   = View.GONE
                binding.cardDaftarPemilik.visibility = View.GONE
            }
            status == "pending" -> {
                binding.cardOwnerStatus.visibility   = View.VISIBLE
                binding.cardDaftarPemilik.visibility = View.GONE
                binding.tvOwnerStatusIcon.text  = "⏳"
                binding.tvOwnerStatusTitle.text = "Menunggu Verifikasi"
                binding.tvOwnerStatusDesc.text  = "Akun pemilik lapangan Anda sedang diproses."
                binding.btnSimulasiVerifikasi.visibility = View.VISIBLE
            }
            else -> {
                binding.cardOwnerStatus.visibility   = View.GONE
                binding.cardDaftarPemilik.visibility = View.VISIBLE
            }
        }
    }

    private fun setupClickListeners() {
        binding.menuEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        binding.menuSecurity.setOnClickListener {
            startActivity(Intent(requireContext(), SecurityPrivacyActivity::class.java))
        }

        binding.menuTerms.setOnClickListener {
            val intent = Intent(requireContext(), StaticPageActivity::class.java)
            intent.putExtra(StaticPageActivity.EXTRA_TYPE, StaticPageActivity.TYPE_TERMS)
            startActivity(intent)
        }

        binding.menuPrivacy.setOnClickListener {
            val intent = Intent(requireContext(), StaticPageActivity::class.java)
            intent.putExtra(StaticPageActivity.EXTRA_TYPE, StaticPageActivity.TYPE_PRIVACY)
            startActivity(intent)
        }

        binding.menuHelp.setOnClickListener {
            startActivity(Intent(requireContext(), HelpCenterActivity::class.java))
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