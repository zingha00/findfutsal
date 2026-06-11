package com.utama.findfutsall

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.utama.findfutsall.databinding.ActivityMainBinding
import com.utama.findfutsall.ui.main.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
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