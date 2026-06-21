package com.utama.findfutsall.ui.main

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.utama.findfutsall.adapter.ExploreAdapter
import com.utama.findfutsall.data.model.Field
import com.utama.findfutsall.databinding.FragmentExploreBinding
import com.utama.findfutsall.ui.detail.DetailFieldActivity
import com.utama.findfutsall.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!
    private lateinit var exploreAdapter: ExploreAdapter
    private var allFields = listOf<Field>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        loadFields()
    }

    private fun setupRecyclerView() {
        exploreAdapter = ExploreAdapter(
            emptyList(),
            onItemClick = { field -> openDetail(field) },
            onFavoriteClick = { _ -> }
        )
        binding.rvSearchResult.adapter = exploreAdapter
    }

    /**
     * FIX PENTING: sebelumnya hanya 6 extra dikirim (id, name, address, price,
     * rating, photo, category) -- field_description, field_facilities,
     * field_open_time, field_close_time, field_phone, dan field_maps_link
     * TIDAK pernah diteruskan. Akibatnya Detail Lapangan yang dibuka dari
     * Pencarian kehilangan fitur-fitur yang sudah berfungsi normal kalau
     * dibuka dari Home (termasuk tombol "Lihat di Maps").
     * Sekarang konsisten dengan HomeFragment.openDetail().
     */
    private fun openDetail(field: Field) {
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
            putExtra("field_maps_link", field.mapsLink)
        }
        startActivity(intent)
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterFields(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterFields(query: String) {
        val filtered = if (query.isEmpty()) allFields
        else allFields.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.address.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true)
        }
        exploreAdapter.updateData(filtered)
        binding.tvResultCount.text = "${filtered.size} Lapangan ditemukan"
    }

    private fun loadFields() {
        lifecycleScope.launch {
            try {
                val fields = withContext(Dispatchers.IO) { fetchFields() }
                if (_binding == null) return@launch
                allFields = fields
                exploreAdapter.updateData(allFields)
                binding.tvResultCount.text = "${allFields.size} Lapangan ditemukan"
            } catch (e: Exception) {
                if (_binding == null) return@launch
                allFields = emptyList()
                exploreAdapter.updateData(emptyList())
                binding.tvResultCount.text = "0 Lapangan ditemukan"
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
                closeTime   = obj.optString("closeTime").ifEmpty { "23:00" },
                mapsLink    = obj.optString("maps_link").ifEmpty { null }
            )
        }
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