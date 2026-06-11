package com.utama.findfutsall.ui.owner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.utama.findfutsall.R
import com.utama.findfutsall.databinding.ActivityOwnerDashboardBinding

class OwnerDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOwnerDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOwnerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNav()

        if (savedInstanceState == null) {
            loadFragment(OwnerBerandaFragment())
            binding.bottomNav.selectedItemId = R.id.nav_beranda
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_beranda  -> loadFragment(OwnerBerandaFragment())
                R.id.nav_booking  -> loadFragment(OwnerBookingFragment())
                R.id.nav_lapangan -> loadFragment(OwnerLapanganFragment())
                R.id.nav_keuangan -> loadFragment(OwnerPlaceholderFragment.newInstance(
                    "Keuangan", "Laporan pendapatan dan keuangan venue"
                ))
                R.id.nav_profil   -> loadFragment(OwnerProfilFragment())
            }
            true
        }
    }

    fun navigateTo(index: Int) {
        val menuItemId = when (index) {
            0    -> R.id.nav_beranda
            1    -> R.id.nav_booking
            2    -> R.id.nav_lapangan
            3    -> R.id.nav_keuangan
            4    -> R.id.nav_profil
            else -> R.id.nav_beranda
        }
        binding.bottomNav.selectedItemId = menuItemId
    }

    private fun loadFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        return true
    }
}