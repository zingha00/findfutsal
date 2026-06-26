package com.utama.findfutsall.ui.main

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.utama.findfutsall.R
import com.utama.findfutsall.adapter.ExploreAdapter
import com.utama.findfutsall.adapter.LapanganBaruAdapter
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
    private lateinit var lapanganBaruAdapter: LapanganBaruAdapter
    private var allFields = listOf<Field>()
    private var activeCategory = "Semua"
    private var sortAscending: Boolean? = null

    private val itemsPerPage = 10
    private var currentPage = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupSearch()
        setupCategoryFilter()
        loadFields()
    }

    private fun setupRecyclerViews() {
        exploreAdapter = ExploreAdapter(
            emptyList(),
            onItemClick = { field -> openDetail(field) },
            onFavoriteClick = { field -> syncFavoriteToServer(field, exploreAdapter) }
        )
        binding.rvSearchResult.adapter = exploreAdapter

        lapanganBaruAdapter = LapanganBaruAdapter(
            emptyList(),
            onItemClick = { field -> openDetail(field) },
            onFavoriteClick = { field -> syncFavoriteToServer(field, lapanganBaruAdapter) }
        )
        binding.rvLapanganBaru.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvLapanganBaru.adapter = lapanganBaruAdapter
    }

    private fun syncFavoriteToServer(field: Field, adapter: Any) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                com.utama.findfutsall.data.api.ApiClient.instance.toggleFavorite(
                    mapOf("user_id" to com.utama.findfutsall.utils.SessionManager(requireContext()).getUserId(), "field_id" to field.id)
                )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (_binding == null) return@withContext
                    when (adapter) {
                        is ExploreAdapter -> adapter.toggleFavorite(field.id)
                        is LapanganBaruAdapter -> adapter.toggleFavorite(field.id)
                    }
                }
            }
        }
    }

    private fun setupCategoryFilter() {
        val chips = mapOf(
            "Semua"           to binding.chipSemua,
            "Vinyl"           to binding.chipVinyl,
            "Rumput Sintetis" to binding.chipRumputSintetis,
            "Parquet"         to binding.chipParquet
        )
        chips.forEach { (category, chip) ->
            chip.setOnClickListener {
                activeCategory = category
                chips.values.forEach {
                    it.setBackgroundResource(R.drawable.bg_chip_inactive)
                    it.setTextColor(android.graphics.Color.parseColor("#121212"))
                }
                chip.setBackgroundResource(R.drawable.bg_chip_active)
                chip.setTextColor(android.graphics.Color.WHITE)
                currentPage = 1
                filterFields(binding.etSearch.text.toString())
            }
        }

        binding.chipHarga.setOnClickListener {
            sortAscending = if (sortAscending == true) false else true
            binding.chipHarga.text = if (sortAscending == true) "Harga ▲ Termurah" else "Harga ▼ Termahal"
            currentPage = 1
            filterFields(binding.etSearch.text.toString())
        }
    }

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
                currentPage = 1
                filterFields(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterFields(query: String) {
        var filtered = if (query.isEmpty()) allFields
        else allFields.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.address.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true)
        }

        if (activeCategory != "Semua") {
            filtered = filtered.filter { it.category.equals(activeCategory, ignoreCase = true) }
        }

        filtered = when (sortAscending) {
            true  -> filtered.sortedBy { it.price }
            false -> filtered.sortedByDescending { it.price }
            null  -> filtered
        }

        binding.tvResultCount.text = "${filtered.size} Lapangan ditemukan"
        renderPage(filtered)
    }


    private fun renderPage(filtered: List<Field>) {
        val totalPages = if (filtered.isEmpty()) 1 else ((filtered.size - 1) / itemsPerPage) + 1
        if (currentPage > totalPages) currentPage = totalPages

        val start = (currentPage - 1) * itemsPerPage
        val end   = minOf(start + itemsPerPage, filtered.size)
        val pageItems = if (start < end) filtered.subList(start, end) else emptyList()

        exploreAdapter.updateData(pageItems)
        renderPaginationControls(totalPages)
    }

    private fun renderPaginationControls(totalPages: Int) {
        binding.layoutPagination.removeAllViews()
        if (totalPages <= 1) return

        for (page in 1..totalPages) {
            val pageView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_page_number, binding.layoutPagination, false) as TextView
            pageView.text = page.toString()

            if (page == currentPage) {
                pageView.setBackgroundResource(R.drawable.bg_chip_active)
                pageView.setTextColor(android.graphics.Color.WHITE)
            } else {
                pageView.setBackgroundResource(R.drawable.bg_chip_inactive)
                pageView.setTextColor(android.graphics.Color.parseColor("#121212"))
            }

            pageView.setOnClickListener {
                currentPage = page
                filterFields(binding.etSearch.text.toString())
                binding.root.let {
                    // Scroll halaman kembali ke atas list, bukan ke paling atas
                    // fragment, supaya user langsung lihat hasil halaman baru
                }
            }

            binding.layoutPagination.addView(pageView)
        }
    }

    private fun loadFields() {
        lifecycleScope.launch {
            try {
                val fields = withContext(Dispatchers.IO) { fetchFields() }
                val favIds = withContext(Dispatchers.IO) { fetchFavoriteIds() }
                if (_binding == null) return@launch
                allFields = fields

                lapanganBaruAdapter.updateData(allFields.take(8))
                lapanganBaruAdapter.setFavorites(favIds)
                binding.layoutLapanganBaru.visibility =
                    if (allFields.isEmpty()) View.GONE else View.VISIBLE

                currentPage = 1
                binding.tvResultCount.text = "${allFields.size} Lapangan ditemukan"
                renderPage(allFields)
                exploreAdapter.setFavorites(favIds)
            } catch (e: Exception) {
                if (_binding == null) return@launch
                allFields = emptyList()
                lapanganBaruAdapter.updateData(emptyList())
                binding.layoutLapanganBaru.visibility = View.GONE
                exploreAdapter.updateData(emptyList())
                binding.tvResultCount.text = "0 Lapangan ditemukan"
            }
        }
    }

    private suspend fun fetchFavoriteIds(): Set<Int> {
        return try {
            val userId = com.utama.findfutsall.utils.SessionManager(requireContext()).getUserId()
            val response = com.utama.findfutsall.data.api.ApiClient.instance.getFavorites(
                mapOf("user_id" to userId)
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                @Suppress("UNCHECKED_CAST")
                val list = body["data"] as? List<Map<String, Any>> ?: emptyList()
                list.map { (it["id"] as? Double)?.toInt() ?: 0 }.toSet()
            } else emptySet()
        } catch (e: Exception) { emptySet() }
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