package com.utama.findfutsall.ui.main

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.utama.findfutsall.R
import com.utama.findfutsall.adapter.FieldAdapter
import com.utama.findfutsall.adapter.FieldHorizontalAdapter
import com.utama.findfutsall.adapter.PromoAdapter
import com.utama.findfutsall.data.model.Field
import com.utama.findfutsall.data.model.Promo
import com.utama.findfutsall.databinding.FragmentHomeBinding
import com.utama.findfutsall.ui.detail.DetailFieldActivity
import com.utama.findfutsall.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var fieldAdapter: FieldAdapter
    private lateinit var fieldHorizontalAdapter: FieldHorizontalAdapter
    private lateinit var promoAdapter: PromoAdapter
    private val handler = Handler(Looper.getMainLooper())
    private var currentPage = 0
    private val dots = mutableListOf<ImageView>()

    private val slideRunnable = Runnable {
        _binding?.let { b ->
            val count = promoAdapter.itemCount
            if (count > 0) {
                currentPage = (currentPage + 1) % count
                b.vpPromo.setCurrentItem(currentPage, true)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPromo()
        setupRecyclerViews()
        setupClickListeners()
        loadFields()
        binding.rvCategory.visibility = View.GONE
    }

    private fun setupPromo() {
        val promos = listOf(
            Promo(1, "Diskon 30%", "SPECIAL WEEKEND", "Main bareng tim jadi makin hemat!", "#00A86B", null),
            Promo(2, "Gratis 1 Jam", "FIRST BOOKING", "Booking pertama gratis 1 jam!", "#1A4D2E", null),
            Promo(3, "Cashback 20%", "PROMO BULANAN", "Bayar pakai dompet digital dapat cashback!", "#00796B", null)
        )
        promoAdapter = PromoAdapter(promos)
        binding.vpPromo.adapter = promoAdapter
        binding.vpPromo.offscreenPageLimit = 1
        setupDots(promos.size)

        binding.vpPromo.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentPage = position
                updateDots(position)
                handler.removeCallbacks(slideRunnable)
                handler.postDelayed(slideRunnable, 3000)
            }
        })
        handler.postDelayed(slideRunnable, 3000)
    }

    private fun setupDots(count: Int) {
        dots.clear()
        binding.layoutDots.removeAllViews()
        for (i in 0 until count) {
            val dot = ImageView(requireContext())
            val params = LinearLayout.LayoutParams(8, 8).apply { setMargins(4, 0, 4, 0) }
            dot.layoutParams = params
            dot.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.dot_inactive))
            dots.add(dot)
            binding.layoutDots.addView(dot)
        }
        updateDots(0)
    }

    private fun updateDots(position: Int) {
        val ctx = context ?: return
        dots.forEachIndexed { index, dot ->
            dot.setImageDrawable(
                ContextCompat.getDrawable(ctx,
                    if (index == position) R.drawable.dot_active else R.drawable.dot_inactive)
            )
        }
    }

    private fun setupRecyclerViews() {
        // Horizontal — lapangan terdekat
        fieldHorizontalAdapter = FieldHorizontalAdapter(emptyList()) { field ->
            openDetail(field)
        }
        binding.rvFieldsHorizontal.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvFieldsHorizontal.adapter = fieldHorizontalAdapter

        // Vertical — rekomendasi
        fieldAdapter = FieldAdapter(emptyList()) { field ->
            openDetail(field)
        }
        binding.rvFields.adapter = fieldAdapter
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
        }
        startActivity(intent)
    }

    private fun setupClickListeners() {
        binding.tvSearch.setOnClickListener {
            (activity as? com.utama.findfutsall.MainActivity)
                ?.setSelectedNavItem(R.id.nav_explore)
        }
    }

    private fun loadFields() {
        lifecycleScope.launch {
            try {
                val fields = withContext(Dispatchers.IO) { fetchFields() }
                if (_binding == null) return@launch
                fieldHorizontalAdapter.updateData(fields)
                fieldAdapter.updateData(fields)
            } catch (e: Exception) {
                if (_binding == null) return@launch
                fieldHorizontalAdapter.updateData(emptyList())
                fieldAdapter.updateData(emptyList())
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

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(slideRunnable)
    }

    override fun onResume() {
        super.onResume()
        loadFields()
        handler.postDelayed(slideRunnable, 3000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(slideRunnable)
        _binding = null
    }
}