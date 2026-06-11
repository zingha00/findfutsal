package com.utama.findfutsall.ui.main

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.utama.findfutsall.R
import com.utama.findfutsall.adapter.ExploreAdapter
import com.utama.findfutsall.data.model.Field
import com.utama.findfutsall.databinding.FragmentFavoriteBinding
import com.utama.findfutsall.ui.detail.DetailFieldActivity
import com.utama.findfutsall.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!
    private lateinit var favoriteAdapter: ExploreAdapter
    private var allFavorites = listOf<Field>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupChips()
        loadFields()
    }

    private fun setupRecyclerView() {
        favoriteAdapter = ExploreAdapter(
            emptyList(),
            onItemClick = { field ->
                val intent = Intent(requireContext(), DetailFieldActivity::class.java)
                intent.putExtra("field_id", field.id)
                intent.putExtra("field_name", field.name)
                intent.putExtra("field_address", field.address)
                intent.putExtra("field_price", field.price)
                intent.putExtra("field_rating", field.rating)
                intent.putExtra("field_photo", field.photo)
                intent.putExtra("field_category", field.category)
                startActivity(intent)
            },
            onFavoriteClick = { _ -> }
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
            "semua"    to binding.chipSemua,
            "futsal"   to binding.chipFutsal,
            "badminton" to binding.chipBadminton,
            "basket"   to binding.chipBasket
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

    private fun loadFields() {
        lifecycleScope.launch {
            try {
                val fields = withContext(Dispatchers.IO) { fetchFields() }
                if (_binding == null) return@launch
                allFavorites = fields
                favoriteAdapter.updateData(allFavorites)
                updateEmptyState(allFavorites.isEmpty())
            } catch (e: Exception) {
                if (_binding == null) return@launch
                updateEmptyState(true)
            }
        }
    }

    private fun fetchFields(): List<Field> {
        val response = OkHttpClient().newCall(
            Request.Builder().url(Constants.BASE_URL + "get_fields.php").get().build()
        ).execute()
        val body = response.body?.string() ?: return emptyList()
        val json = JSONObject(body)
        if (!json.optBoolean("success")) return emptyList()
        val arr = json.getJSONArray("data")
        return (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            Field(
                id          = obj.optInt("id"),
                name        = obj.optString("name"),
                address     = obj.optString("address"),
                price       = obj.optInt("price"),
                rating      = obj.optDouble("rating", 0.0).toFloat(),
                photo       = obj.optString("photo").ifEmpty { null },
                category    = obj.optString("category"),
                distance    = obj.optString("distance").ifEmpty { null },
                phone       = obj.optString("phone").ifEmpty { null },
                description = obj.optString("description").ifEmpty { null },
                facilities  = obj.optString("facilities").ifEmpty { null },
                openTime    = obj.optString("openTime").ifEmpty { "06:00" },
                closeTime   = obj.optString("closeTime").ifEmpty { "23:00" }
            )
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (_binding == null) return
        binding.layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvFavorite.visibility  = if (isEmpty) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        loadFields()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}