package com.utama.findfutsall.ui.owner

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.utama.findfutsall.R
import com.utama.findfutsall.databinding.FragmentOwnerProfilBinding
import com.utama.findfutsall.ui.auth.LoginActivity
import com.utama.findfutsall.utils.Constants
import com.utama.findfutsall.utils.SessionManager

class OwnerProfilFragment : Fragment() {

    private var _binding: FragmentOwnerProfilBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOwnerProfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        setupProfile()
        setupDarkMode()
        setupLogout()
    }

    private fun setupProfile() {
        val name = sessionManager.getUserName() ?: "Arena Manager"
        binding.tvProfilName.text  = name
        binding.tvProfilName2.text = name
        binding.tvProfilEmail.text = sessionManager.getUserEmail() ?: "-"
        binding.tvProfilPhone.text = sessionManager.getUserPhone() ?: "-"

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

        binding.btnEditNama.setOnClickListener {
            showEditDialog("Nama Lengkap", name) { newVal ->
                binding.tvProfilName.text  = newVal
                binding.tvProfilName2.text = newVal
                sessionManager.updateName(newVal)
            }
        }

        binding.btnEditPhone.setOnClickListener {
            showEditDialog("Nomor Telepon", sessionManager.getUserPhone() ?: "") { newVal ->
                binding.tvProfilPhone.text = newVal
                sessionManager.updatePhone(newVal)
            }
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

    private fun showEditDialog(field: String, current: String, onSave: (String) -> Unit) {
        val input = EditText(requireContext()).apply {
            setText(current)
            setPadding(48, 32, 48, 32)
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Edit $field")
            .setView(input)
            .setPositiveButton("Simpan") { _, _ ->
                val value = input.text.toString().trim()
                if (value.isNotEmpty()) onSave(value)
            }
            .setNegativeButton("Batal", null)
            .show()
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