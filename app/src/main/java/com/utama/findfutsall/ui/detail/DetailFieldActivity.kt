package com.utama.findfutsall.ui.detail

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.gridlayout.widget.GridLayout
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.utama.findfutsall.R
import com.utama.findfutsall.data.api.ApiClient
import com.utama.findfutsall.databinding.ActivityDetailFieldBinding
import com.utama.findfutsall.utils.PriceFormatter
import com.utama.findfutsall.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DetailFieldActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailFieldBinding
    private lateinit var sessionManager: SessionManager

    private var fieldId       = 0
    private var fieldPrice    = 0
    private var selectedDate  = ""
    private var selectedStart = ""
    private var selectedEnd   = ""
    private var isFavorite    = false

    private val bookedSlots = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailFieldBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        fieldId    = intent.getIntExtra("field_id", 0)
        fieldPrice = intent.getIntExtra("field_price", 0)

        val fieldName     = intent.getStringExtra("field_name") ?: "Lapangan"
        val fieldAddress  = intent.getStringExtra("field_address") ?: ""
        val fieldRating   = intent.getFloatExtra("field_rating", 0f)
        val fieldPhoto    = intent.getStringExtra("field_photo")
        val fieldCategory = intent.getStringExtra("field_category") ?: ""
        val fieldDesc     = intent.getStringExtra("field_description") ?: ""
        val fieldFac      = intent.getStringExtra("field_facilities") ?: ""
        val openTime      = intent.getStringExtra("field_open_time") ?: "06:00"
        val closeTime     = intent.getStringExtra("field_close_time") ?: "23:00"

        // Bind data
        binding.tvFieldName.text = fieldName
        binding.tvAddress.text   = fieldAddress
        binding.tvRating.text    = if (fieldRating > 0) String.format("%.1f", fieldRating) else "0.0"
        binding.tvCategory.text  = fieldCategory.ifEmpty { "Futsal" }
        binding.tvJamBuka.text   = "Buka $openTime - $closeTime"
        binding.tvDescription.text = fieldDesc.ifEmpty { "Lapangan futsal berkualitas di Bandung." }
        binding.tvTotalPrice.text = "${PriceFormatter.format(fieldPrice)}/jam"

        // Load foto
        val photoName = fieldPhoto ?: ""
        val baseUrl   = com.utama.findfutsall.utils.Constants.BASE_URL.replace("/api/", "/")
        val fullUrl   = when {
            photoName.startsWith("http") -> photoName
            photoName.isNotEmpty()       -> "$baseUrl$photoName"
            else                         -> null
        }

        if (fullUrl != null) {
            Glide.with(this)
                .load(fullUrl)
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                .placeholder(R.color.divider)
                .error(R.drawable.findfutsall)
                .centerCrop()
                .into(binding.ivFieldPhoto)
        } else {
            binding.ivFieldPhoto.setImageResource(R.drawable.findfutsall)
        }

        // Fasilitas
        setupFasilitas(fieldFac)

        // Date pills
        setupDatePills(openTime, closeTime)

        // Cek status favorit awal
        checkFavoriteStatus()

        // Bottom bar
        binding.btnBookNow.isEnabled = false
        binding.btnBookNow.alpha     = 0.5f
        binding.btnBack.setOnClickListener { finish() }
        binding.btnFavorite.setOnClickListener { toggleFavorite() }
        binding.btnBookNow.setOnClickListener { doBooking() }
    }

    private fun checkFavoriteStatus() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.instance.getFavorites(
                    mapOf("user_id" to sessionManager.getUserId())
                )
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    @Suppress("UNCHECKED_CAST")
                    val list = body["data"] as? List<Map<String, Any>> ?: emptyList()
                    val favIds = list.map { (it["id"] as? Double)?.toInt() ?: 0 }.toSet()
                    isFavorite = favIds.contains(fieldId)
                    updateFavoriteIcon()
                }
            } catch (e: Exception) {
                // Diamkan, biarkan default false
            }
        }
    }

    private fun toggleFavorite() {
        val willBeFav = !isFavorite
        isFavorite = willBeFav
        updateFavoriteIcon()

        // Animasi bounce
        binding.btnFavorite.animate()
            .scaleX(1.3f).scaleY(1.3f)
            .setDuration(120)
            .withEndAction {
                binding.btnFavorite.animate()
                    .scaleX(1f).scaleY(1f)
                    .setDuration(120)
                    .start()
            }.start()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                ApiClient.instance.toggleFavorite(
                    mapOf("user_id" to sessionManager.getUserId(), "field_id" to fieldId)
                )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Rollback kalau gagal
                    isFavorite = !willBeFav
                    updateFavoriteIcon()
                }
            }
        }
    }

    private fun updateFavoriteIcon() {
        binding.btnFavorite.setColorFilter(
            ContextCompat.getColor(this, if (isFavorite) R.color.error_red else android.R.color.white)
        )
    }

    private fun setupFasilitas(facilities: String) {
        binding.layoutFasilitas.removeAllViews()
        if (facilities.isEmpty()) {
            binding.layoutFasilitas.visibility = View.GONE
            return
        }
        val list = facilities.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        list.forEach { fac ->
            val item = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity     = Gravity.CENTER
                val p = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                layoutParams = p
            }
            val icon = android.widget.ImageView(this).apply {
                val p = LinearLayout.LayoutParams(32.dp, 32.dp)
                layoutParams = p
                setImageResource(R.drawable.ic_search)
                setBackgroundResource(R.drawable.bg_facility)
                setPadding(6.dp, 6.dp, 6.dp, 6.dp)
                setColorFilter(Color.parseColor("#00A86B"))
            }
            val label = TextView(this).apply {
                text      = fac
                textSize  = 10f
                setTextColor(Color.parseColor("#666666"))
                gravity   = Gravity.CENTER
                val p     = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                p.topMargin = 6.dp
                layoutParams = p
            }
            item.addView(icon)
            item.addView(label)
            binding.layoutFasilitas.addView(item)
        }
    }

    private fun setupDatePills(openTime: String, closeTime: String) {
        binding.layoutDatePills.removeAllViews()
        val calendar = Calendar.getInstance()
        val dayNames = listOf("MIN", "SEN", "SEL", "RAB", "KAM", "JUM", "SAB")
        val sdfDisplay = SimpleDateFormat("dd", Locale("id"))
        val sdfFull    = SimpleDateFormat("yyyy-MM-dd", Locale("id"))
        val sdfMonth   = SimpleDateFormat("MMMM yyyy", Locale("id"))

        binding.tvSelectedMonth.text = sdfMonth.format(calendar.time)

        for (i in 0 until 7) {
            val cal      = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_MONTH, i)
            val dayName  = dayNames[cal.get(Calendar.DAY_OF_WEEK) - 1]
            val dayNum   = sdfDisplay.format(cal.time)
            val fullDate = sdfFull.format(cal.time)

            val pill = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity     = Gravity.CENTER
                val p       = LinearLayout.LayoutParams(52.dp, LinearLayout.LayoutParams.WRAP_CONTENT)
                p.marginEnd = 8.dp
                layoutParams = p
                setBackgroundResource(R.drawable.bg_chip_inactive)
                setPadding(0, 10.dp, 0, 10.dp)
                isClickable  = true
                isFocusable  = true
            }

            val tvDay = TextView(this).apply {
                text      = dayName
                textSize  = 10f
                gravity   = Gravity.CENTER
                setTextColor(Color.parseColor("#999999"))
            }
            val tvNum = TextView(this).apply {
                text      = dayNum
                textSize  = 14f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                gravity   = Gravity.CENTER
                setTextColor(Color.parseColor("#121212"))
            }

            pill.addView(tvDay)
            pill.addView(tvNum)

            pill.setOnClickListener {
                for (j in 0 until binding.layoutDatePills.childCount) {
                    val p2 = binding.layoutDatePills.getChildAt(j) as LinearLayout
                    p2.setBackgroundResource(R.drawable.bg_chip_inactive)
                    (p2.getChildAt(0) as TextView).setTextColor(Color.parseColor("#999999"))
                    (p2.getChildAt(1) as TextView).setTextColor(Color.parseColor("#121212"))
                }
                pill.setBackgroundResource(R.drawable.bg_chip_active)
                tvDay.setTextColor(Color.WHITE)
                tvNum.setTextColor(Color.WHITE)

                selectedDate = fullDate
                binding.tvSelectedMonth.text = sdfMonth.format(cal.time)
                setupTimeSlots(openTime, closeTime)
                binding.cardPilihJam.visibility = View.VISIBLE
            }

            binding.layoutDatePills.addView(pill)
        }
    }

    private fun setupTimeSlots(openTime: String, closeTime: String) {
        binding.gridTimeSlots.removeAllViews()
        selectedStart = ""
        selectedEnd   = ""
        binding.cardRincian.visibility = View.GONE
        binding.btnBookNow.isEnabled   = false
        binding.btnBookNow.alpha       = 0.5f

        val slots = generateSlots(openTime, closeTime)

        slots.forEach { slot ->
            val startHour = slot.split(" - ")[0]
            val endHour   = slot.split(" - ")[1]
            val isBooked  = bookedSlots.contains(startHour)

            val card = androidx.cardview.widget.CardView(this).apply {
                val p = GridLayout.LayoutParams().apply {
                    width      = 0
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(6.dp, 6.dp, 6.dp, 6.dp)
                }
                layoutParams     = p
                radius           = 10.dp.toFloat()
                cardElevation    = 0f
                setCardBackgroundColor(
                    if (isBooked) Color.parseColor("#F5F5F5")
                    else Color.parseColor("#E8FFF5")
                )
                isClickable      = !isBooked
                isFocusable      = !isBooked
            }

            val inner = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity     = Gravity.CENTER
                setPadding(0, 14.dp, 0, 14.dp)
            }

            val tvSlot = TextView(this).apply {
                text      = slot
                textSize  = 11f
                gravity   = Gravity.CENTER
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setTextColor(
                    if (isBooked) Color.parseColor("#BBBBBB")
                    else Color.parseColor("#00A86B")
                )
            }

            val tvPrice = TextView(this).apply {
                text      = PriceFormatter.format(fieldPrice)
                textSize  = 10f
                gravity   = Gravity.CENTER
                setTextColor(
                    if (isBooked) Color.parseColor("#BBBBBB")
                    else Color.parseColor("#1A4D2E")
                )
            }

            if (isBooked) {
                val tvFull = TextView(this).apply {
                    text      = "Penuh"
                    textSize  = 9f
                    gravity   = Gravity.CENTER
                    setTextColor(Color.parseColor("#FF3B30"))
                }
                inner.addView(tvSlot)
                inner.addView(tvFull)
            } else {
                inner.addView(tvSlot)
                inner.addView(tvPrice)
                card.setOnClickListener {
                    selectedStart = startHour
                    selectedEnd   = endHour
                    updateSlotSelection(slot, slots)
                    showRincian()
                }
            }

            card.addView(inner)
            binding.gridTimeSlots.addView(card)
        }
    }

    private fun generateSlots(openTime: String, closeTime: String): List<String> {
        val slots    = mutableListOf<String>()
        val openHour = openTime.split(":")[0].toIntOrNull() ?: 6
        val closeHour = closeTime.split(":")[0].toIntOrNull() ?: 23
        for (h in openHour until closeHour) {
            slots.add(String.format("%02d:00 - %02d:00", h, h + 1))
        }
        return slots
    }

    private fun updateSlotSelection(selected: String, allSlots: List<String>) {
        for (i in 0 until binding.gridTimeSlots.childCount) {
            val card = binding.gridTimeSlots.getChildAt(i) as? androidx.cardview.widget.CardView
                ?: continue
            val inner = card.getChildAt(0) as? LinearLayout ?: continue
            val tvSlot = inner.getChildAt(0) as? TextView ?: continue
            val slot   = tvSlot.text.toString()
            val startH = slot.split(" - ")[0]

            if (bookedSlots.contains(startH)) return

            if (slot == selected) {
                card.setCardBackgroundColor(Color.parseColor("#1A4D2E"))
                tvSlot.setTextColor(Color.WHITE)
                (inner.getChildAt(1) as? TextView)?.setTextColor(Color.parseColor("#A8D5B5"))
            } else {
                card.setCardBackgroundColor(Color.parseColor("#E8FFF5"))
                tvSlot.setTextColor(Color.parseColor("#00A86B"))
                (inner.getChildAt(1) as? TextView)?.setTextColor(Color.parseColor("#1A4D2E"))
            }
        }
    }

    private fun showRincian() {
        val serviceFee = 5000
        val total      = fieldPrice + serviceFee
        binding.tvRincianSewa.text  = PriceFormatter.format(fieldPrice)
        binding.tvTotalRincian.text = PriceFormatter.format(total)
        binding.tvTotalPrice.text   = PriceFormatter.format(total)
        binding.cardRincian.visibility = View.VISIBLE
        binding.btnBookNow.isEnabled   = true
        binding.btnBookNow.alpha       = 1f
    }

    private fun doBooking() {
        if (selectedDate.isEmpty() || selectedStart.isEmpty()) return
        val intent = Intent(this, PaymentActivity::class.java).apply {
            putExtra("field_id",      fieldId)
            putExtra("field_name",    binding.tvFieldName.text.toString())
            putExtra("field_address", binding.tvAddress.text.toString())
            putExtra("field_date",    selectedDate)
            putExtra("field_start",   selectedStart)
            putExtra("field_end",     selectedEnd)
            putExtra("field_price",   fieldPrice)
        }
        startActivity(intent)
    }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
}