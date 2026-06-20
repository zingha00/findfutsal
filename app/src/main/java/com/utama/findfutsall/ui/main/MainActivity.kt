package com.utama.findfutsall.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.utama.findfutsall.R
import com.utama.findfutsall.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            handleNavigateToExtra()
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_explore -> loadFragment(ExploreFragment())
                R.id.nav_booking -> loadFragment(BookingFragment())
                R.id.nav_favorite -> loadFragment(FavoriteFragment())
                R.id.nav_profile -> loadFragment(ProfileFragment())
            }
            true
        }
    }

    /**
     * Baca extra "navigate_to" yang dikirim dari activity lain (misal
     * BookingSuccessActivity) supaya bisa langsung pindah ke tab tertentu,
     * bukan selalu default ke Home.
     */
    private fun handleNavigateToExtra() {
        val navigateTo = intent.getStringExtra("navigate_to")
        when (navigateTo) {
            "pesanan" -> {
                binding.bottomNavigationView.selectedItemId = R.id.nav_booking
                loadFragment(BookingFragment())
            }
            else -> {
                loadFragment(HomeFragment())
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun setSelectedNavItem(itemId: Int) {
        binding.bottomNavigationView.selectedItemId = itemId
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.bottomNavigationView.selectedItemId != R.id.nav_home) {
            binding.bottomNavigationView.selectedItemId = R.id.nav_home
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }
}