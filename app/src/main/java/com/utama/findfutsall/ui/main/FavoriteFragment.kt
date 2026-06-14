package com.utama.findfutsall.ui.main

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.utama.findfutsall.R
import com.utama.findfutsall.adapter.ExploreAdapter
import com.utama.findfutsall.data.api.ApiClient
import com.utama.findfutsall.data.model.Field
import com.utama.findfutsall.databinding.FragmentFavoriteBinding
import com.utama.findfutsall.ui.detail.DetailFieldActivity
import com.utama.findfutsall.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!
    private lateinit var session: SessionManager
    private lateinit var favoriteAdapter: ExploreAdapter
    private var allFavorites = mutableListOf<Field>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session = SessionManager(requireContext())
        setupRecyclerView()
        setupChips()
        loadFavorites()
    }

    private fun setupRecyclerView() {
        favoriteAdapter = ExploreAdapter(
            fields = emptyList(),
            onItemClick = { field ->
                val intent = Intent(requireContext(), DetailFieldActivity::class.java).apply {
                    putExtra("field_id", field.id)
                    putExtra("field_name", field.name)
                    putExtra("field_address", field.address)
                    putExtra("field_price", field.price)
                    putExtra("field_rating", field.rating)
                    putExtra("field_photo", field.photo)
                    putExtra("field_category", field.category)
                    putExtra("field_description", field.description)
                    putExtra("field_facilities", field.facilities)
                    putExtra("field_open_time", field.openTime)
                    putExtra("field_close_time", field.closeTime)
                    putExtra("field_phone", field.phone)
                }
                startActivity(intent)
            },
            onFavoriteClick = { field -> removeFavorite(field) },
            alwaysFavorite = true
        )
        binding.rvFavorite.adapter = favoriteAdapter
    }

    private fun setupChips() {
        binding.chipSemua.setOnClickListener {
            setActiveChip("semua")
            favoriteAdapter.updateData(allFavorites)
        }
        binding.chipFutsal.setOnClickListener {
            setActiveChip("futsal")
            favoriteAdapter.updateData(
                allFavorites.filter {
                    it.category.contains("Sintetis", ignoreCase = true) ||
                            it.category.contains("Vinyl", ignoreCase = true) ||
                            it.category.contains("Indoor", ignoreCase = true) ||
                            it.category.contains("Rumput", ignoreCase = true)
                }
            )
        }
        binding.chipBadminton.setOnClickListener {
            setActiveChip("badminton")
            favoriteAdapter.updateData(
                allFavorites.filter { it.category.contains("Badminton", ignoreCase = true) }
            )
        }
        binding.chipBasket.setOnClickListener {
            setActiveChip("basket")
            favoriteAdapter.updateData(
                allFavorites.filter { it.category.contains("Basket", ignoreCase = true) }
            )
        }
    }

    private fun setActiveChip(active: String) {
        val chips = mapOf(
            "semua"     to binding.chipSemua,
            "futsal"    to binding.chipFutsal,
            "badminton" to binding.chipBadminton,
            "basket"    to binding.chipBasket
        )
        chips.forEach { (key, chip) ->
            if (key == active) {
                chip.setBackgroundResource(R.drawable.bg_chip_active)
                chip.setTextColor(Color.WHITE)
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_inactive)
                chip.setTextColor(Color.parseColor("#121212"))
            }
        }
    }

    private fun loadFavorites() {
        val userId = session.getUserId()
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.instance.getFavorites(mapOf("user_id" to userId))
                }
                if (_binding == null) return@launch
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    @Suppress("UNCHECKED_CAST")
                    val list = body["data"] as? List<Map<String, Any>> ?: emptyList()
                    allFavorites.clear()
                    allFavorites.addAll(list.map {
                        Field(
                            id          = (it["id"] as? Double)?.toInt() ?: 0,
                            name        = it["name"]?.toString() ?: "",
                            address     = it["address"]?.toString() ?: "",
                            price       = (it["price"] as? Double)?.toInt() ?: 0,
                            rating      = (it["rating"] as? Double)?.toFloat() ?: 0f,
                            photo       = it["photo"]?.toString(),
                            category    = it["category"]?.toString() ?: "",
                            distance    = null,
                            phone       = it["phone"]?.toString(),
                            description = it["description"]?.toString(),
                            facilities  = it["facilities"]?.toString(),
                            openTime    = it["openTime"]?.toString() ?: "06:00",
                            closeTime   = it["closeTime"]?.toString() ?: "23:00"
                        )
                    })
                    favoriteAdapter.updateData(allFavorites)
                    updateEmptyState(allFavorites.isEmpty())
                }
            } catch (e: Exception) {
                if (_binding == null) return@launch
                updateEmptyState(true)
            }
        }
    }

    private fun removeFavorite(field: Field) {
        val userId = session.getUserId()
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.instance.toggleFavorite(
                        mapOf("user_id" to userId, "field_id" to field.id)
                    )
                }
                if (response.isSuccessful) {
                    allFavorites.removeIf { it.id == field.id }
                    favoriteAdapter.updateData(allFavorites)
                    updateEmptyState(allFavorites.isEmpty())
                    Toast.makeText(requireContext(), "Dihapus dari favorit", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (_binding == null) return
        binding.layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvFavorite.visibility  = if (isEmpty) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}