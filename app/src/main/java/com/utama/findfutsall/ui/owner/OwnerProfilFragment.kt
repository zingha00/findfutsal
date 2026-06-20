package com.utama.findfutsall.ui.owner

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.utama.findfutsall.R
import com.utama.findfutsall.databinding.FragmentOwnerProfileBinding
import com.utama.findfutsall.ui.auth.LoginActivity
import com.utama.findfutsall.ui.main.ChangePasswordActivity
import com.utama.findfutsall.ui.main.EditProfileActivity
import com.utama.findfutsall.ui.main.HelpCenterActivity
import com.utama.findfutsall.ui.main.StaticPageActivity
import com.utama.findfutsall.utils.Constants
import com.utama.findfutsall.utils.SessionManager

class OwnerProfilFragment : Fragment() {

    private var _binding: FragmentOwnerProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOwnerProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        setupProfile()
        setupDarkMode()
        setupMenus()
        setupLogout()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data tampilan setiap kembali ke halaman ini
        // (misal habis edit profil dari EditProfileActivity)
        refreshProfileDisplay()
    }

    private fun refreshProfileDisplay() {
        val name = sessionManager.getUserName() ?: "Arena Manager"
        binding.tvProfilName.text = name

        val photoUrl = sessionManager.getUserPhoto()
        if (!photoUrl.isNullOrEmpty()) {
            val fullUrl = if (photoUrl.startsWith("http")) photoUrl
            else Constants.BASE_URL.replace("api/", "") + photoUrl
            Glide.with(this)
                .load(fullUrl)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .into(binding.ivProfilPhoto)
        }
    }

    private fun setupProfile() {
        refreshProfileDisplay()

        // Informasi Pribadi -> EditProfileActivity (sama seperti pola di sisi user)
        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }
        binding.btnChangePhoto.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }
    }

    private fun setupMenus() {
        binding.btnPaymentMethods.setOnClickListener {
            startActivity(Intent(requireContext(), PaymentMethodsActivity::class.java))
        }
        binding.btnUbahPassword.setOnClickListener {
            startActivity(Intent(requireContext(), ChangePasswordActivity::class.java))
        }
        binding.btnHelpCenter.setOnClickListener {
            startActivity(Intent(requireContext(), HelpCenterActivity::class.java))
        }
        binding.btnPrivacyPolicy.setOnClickListener {
            val intent = Intent(requireContext(), StaticPageActivity::class.java)
            intent.putExtra(StaticPageActivity.EXTRA_TYPE, StaticPageActivity.TYPE_PRIVACY)
            startActivity(intent)
        }
        binding.btnAboutApp.setOnClickListener {
            startActivity(Intent(requireContext(), AboutAppActivity::class.java))
        }
    }

    private fun setupDarkMode() {
        val pref = requireContext().getSharedPreferences("app_pref", Context.MODE_PRIVATE)
        val isDark = pref.getBoolean("dark_mode", false)
        binding.switchDarkMode.isChecked = isDark

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            pref.edit().putBoolean("dark_mode", isChecked).apply()

            // Animasi fade sebelum mode berubah
            val rootView = requireActivity().window.decorView
            rootView.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction {
                    AppCompatDelegate.setDefaultNightMode(
                        if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                        else AppCompatDelegate.MODE_NIGHT_NO
                    )
                    rootView.animate()
                        .alpha(1f)
                        .setDuration(200)
                        .start()
                }
                .start()
        }
    }

    private fun setupLogout() {
        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Keluar")
                .setMessage("Apakah kamu yakin ingin keluar dari dashboard pemilik?")
                .setPositiveButton("Keluar") { _, _ ->
                    sessionManager.clearSession()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}