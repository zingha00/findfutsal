package com.utama.findfutsall.ui.owner

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
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
        setupLogout()
    }

    private fun setupProfile() {
        binding.tvProfilName.text  = sessionManager.getUserName() ?: "Arena Manager"
        binding.tvProfilEmail.text = sessionManager.getUserEmail() ?: "-"
        binding.tvProfilPhone.text = sessionManager.getUserPhone() ?: "-"

        val photoUrl = sessionManager.getUserPhoto()
        if (!photoUrl.isNullOrEmpty()) {
            val fullUrl = if (photoUrl.startsWith("http")) photoUrl else Constants.BASE_URL.replace("api/", "") + photoUrl
            Glide.with(this)
                .load(fullUrl)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .into(binding.ivProfilPhoto)
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