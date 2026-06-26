package com.utama.findfutsall.ui.owner

import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.datepicker.MaterialDatePicker
import com.utama.findfutsall.R
import com.utama.findfutsall.data.api.ApiClient
import com.utama.findfutsall.data.model.*
import com.utama.findfutsall.utils.SessionManager
import com.utama.findfutsall.viewmodel.KeuanganViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import android.content.Intent

class OwnerKeuanganFragment : Fragment() {

    private val viewModel: KeuanganViewModel by viewModels()
    private lateinit var session: SessionManager
    private lateinit var adapter: TransaksiAdapter

    // Header summary
    private lateinit var tvPctChange: TextView
    private lateinit var tvKeuanganBulan: TextView
    private lateinit var tvKeuanganBiaya: TextView
    private lateinit var tvKeuanganTotalKeuntungan: TextView
    private lateinit var tvKeuanganMinggu: TextView
    private lateinit var tvKeuanganDenda: TextView
    private lateinit var tvKeuanganTransaksi: TextView
    private lateinit var tvKeuanganOccupancy: TextView

    // Target
    private lateinit var progressTarget: ProgressBar
    private lateinit var tvKeuanganPencapaian: TextView
    private lateinit var tvKeuanganTarget: TextView
    private lateinit var tvTargetPercent: TextView
    private lateinit var btnEditTarget: View

    // Chart
    private lateinit var chartRevenue: LineChart
    private lateinit var tabChart7Hari: TextView
    private lateinit var tabChart30Hari: TextView
    private lateinit var tabChart2Bulan: TextView
    private lateinit var tabChart6Bulan: TextView
    private lateinit var cbPendapatan: CheckBox
    private lateinit var cbPengeluaran: CheckBox
    private var activeChartTab = "7_hari"

    // Export & list
    private lateinit var btnExportLaporan: Button
    private lateinit var rvTransaksi: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnLihatSemua: View

    private var allTransaksi = mutableListOf<TransaksiItem>()
    private var lastResponse: KeuanganResponse? = null

    private val colorPrimary   get() = ContextCompat.getColor(requireContext(), R.color.primary_green)
    private val colorError     get() = ContextCompat.getColor(requireContext(), R.color.error_red)
    private val colorDivider   get() = ContextCompat.getColor(requireContext(), R.color.divider)
    private val colorMuted     get() = ContextCompat.getColor(requireContext(), R.color.text_muted)

    // Target bulan ini -- diambil dari API (owner_targets), bukan hardcoded lagi.
    // Default 20jt dipakai SEMENTARA sebelum data dari API masuk pertama kali.
    private var targetBulanan = 20_000_000.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_owner_keuangan, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session = SessionManager(requireContext())
        view.findViewById<androidx.core.widget.NestedScrollView>(R.id.scrollKeuangan)
            ?.fullScroll(View.FOCUS_UP)
        bindViews(view)
        setupRecyclerView()
        setupChartFilterTabs()
        setupCheckbox()
        setupExport()
        setupTargetEdit()
        setupLihatSemua()
        observeViewModel()
        loadData()
    }

    private fun bindViews(v: View) {
        tvPctChange              = v.findViewById(R.id.tvPctChange)
        tvKeuanganBulan          = v.findViewById(R.id.tvKeuanganBulan)
        tvKeuanganBiaya          = v.findViewById(R.id.tvKeuanganBiaya)
        tvKeuanganTotalKeuntungan = v.findViewById(R.id.tvKeuanganTotalKeuntungan)
        tvKeuanganMinggu         = v.findViewById(R.id.tvKeuanganMinggu)
        tvKeuanganDenda          = v.findViewById(R.id.tvKeuanganDenda)
        tvKeuanganTransaksi      = v.findViewById(R.id.tvKeuanganTransaksi)
        tvKeuanganOccupancy      = v.findViewById(R.id.tvKeuanganOccupancy)
        progressTarget           = v.findViewById(R.id.progressTarget)
        tvKeuanganPencapaian     = v.findViewById(R.id.tvKeuanganPencapaian)
        tvKeuanganTarget         = v.findViewById(R.id.tvKeuanganTarget)
        tvTargetPercent          = v.findViewById(R.id.tvTargetPercent)
        btnEditTarget            = v.findViewById(R.id.btnEditTarget)
        chartRevenue             = v.findViewById(R.id.chartRevenue)
        tabChart7Hari            = v.findViewById(R.id.tabChart7Hari)
        tabChart30Hari           = v.findViewById(R.id.tabChart30Hari)
        tabChart2Bulan           = v.findViewById(R.id.tabChart2Bulan)
        tabChart6Bulan           = v.findViewById(R.id.tabChart6Bulan)
        cbPendapatan             = v.findViewById(R.id.cbPendapatan)
        cbPengeluaran            = v.findViewById(R.id.cbPengeluaran)
        btnExportLaporan         = v.findViewById(R.id.btnExportLaporan)
        rvTransaksi              = v.findViewById(R.id.rvTransaksiKeuangan)
        tvEmpty                  = v.findViewById(R.id.tvTransaksiEmpty)
        progressBar              = v.findViewById(R.id.progressKeuangan)
        btnLihatSemua            = v.findViewById(R.id.btnLihatSemuaTransaksi)
    }

    private fun setupRecyclerView() {
        adapter = TransaksiAdapter()
        rvTransaksi.layoutManager = LinearLayoutManager(requireContext())
        rvTransaksi.adapter = adapter
    }

    private fun setupChartFilterTabs() {
        val tabs = listOf(tabChart7Hari, tabChart30Hari, tabChart2Bulan, tabChart6Bulan)
        val keys = listOf("7_hari", "30_hari", "2_bulan", "6_bulan")
        tabs.forEachIndexed { i, tab ->
            tab.setOnClickListener {
                activeChartTab = keys[i]
                tabs.forEach { t ->
                    t.setBackgroundResource(R.drawable.bg_tab_inactive)
                    t.setTextColor(Color.parseColor("#666666"))
                }
                tab.setBackgroundResource(R.drawable.bg_tab_active)
                tab.setTextColor(Color.WHITE)
                // Filter sekarang request ULANG ke server dengan rentang yang sesuai,
                // supaya summary card (Pendapatan, Total Booking, dll) ikut berubah,
                // bukan cuma chart-nya saja yang dipotong dari data lama.
                loadData()
            }
        }
    }

    private fun setupCheckbox() {
        cbPendapatan.setOnCheckedChangeListener { _, _ -> updateChartByTab() }
        cbPengeluaran.setOnCheckedChangeListener { _, _ -> updateChartByTab() }
    }

    /**
     * Dialog custom untuk ubah target bulanan -- chip rekomendasi nominal
     * (1jt/5jt/10jt/20jt/50jt) untuk pilih cepat, plus input manual dengan
     * format otomatis titik pemisah ribuan saat mengetik.
     */
    private fun setupTargetEdit() {
        btnEditTarget.setOnClickListener { showTargetDialog() }
    }

    private fun showTargetDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_target, null)

        val layoutChips    = dialogView.findViewById<LinearLayout>(R.id.layoutChipsTarget)
        val etAmount        = dialogView.findViewById<EditText>(R.id.etTargetAmount)
        val btnBatal        = dialogView.findViewById<Button>(R.id.btnBatalTarget)
        val btnSimpan        = dialogView.findViewById<Button>(R.id.btnSimpanTarget)

        etAmount.setText(formatNumberOnly(targetBulanan.toLong()))

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_dialog_rounded)

        // Chip rekomendasi nominal cepat
        val recommendations = listOf(
            1_000_000L  to "1 Jt",
            5_000_000L  to "5 Jt",
            10_000_000L to "10 Jt",
            20_000_000L to "20 Jt",
            50_000_000L to "50 Jt"
        )
        recommendations.forEach { (amount, label) ->
            val chip = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_chip_target, layoutChips, false) as TextView
            chip.text = label
            chip.setOnClickListener {
                etAmount.setText(formatNumberOnly(amount))
                etAmount.setSelection(etAmount.text.length)
            }
            layoutChips.addView(chip)
        }

        // Format otomatis titik pemisah ribuan saat user mengetik manual
        etAmount.addTextChangedListener(object : android.text.TextWatcher {
            private var isEditing = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (isEditing) return
                isEditing = true
                val raw = s.toString().replace(".", "")
                if (raw.isNotEmpty()) {
                    val number = raw.toLongOrNull() ?: 0L
                    val formatted = formatNumberOnly(number)
                    etAmount.setText(formatted)
                    etAmount.setSelection(formatted.length)
                }
                isEditing = false
            }
        })

        btnBatal.setOnClickListener { dialog.dismiss() }
        btnSimpan.setOnClickListener {
            val raw = etAmount.text.toString().replace(".", "")
            val newTarget = raw.toDoubleOrNull()
            if (newTarget != null && newTarget > 0) {
                updateTarget(newTarget)
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Masukkan nominal yang valid", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun formatNumberOnly(number: Long): String {
        val fmt = NumberFormat.getNumberInstance(Locale("id", "ID"))
        return fmt.format(number)
    }

    private fun updateTarget(newTarget: Double) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.instance.updateOwnerTarget(
                    mapOf("user_id" to session.getUserId(), "target_amount" to newTarget)
                )
                if (response.isSuccessful && response.body()?.get("success") == true) {
                    Toast.makeText(requireContext(), "Target berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    targetBulanan = newTarget
                    lastResponse?.summary?.let { bindTargetSection(it.bulan, targetBulanan) }
                } else {
                    Toast.makeText(requireContext(), "Gagal memperbarui target", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * "Lihat Semua Transaksi" -- buka dialog terpisah berisi RecyclerView
     * semua transaksi (tidak dibatasi 3 seperti di halaman utama), bisa di-scroll.
     */
    private fun setupLihatSemua() {
        btnLihatSemua.setOnClickListener { showAllTransaksiDialog() }
    }

    private fun showAllTransaksiDialog() {
        if (allTransaksi.isEmpty()) {
            Toast.makeText(requireContext(), "Belum ada transaksi", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_semua_transaksi, null)

        val rv          = dialogView.findViewById<RecyclerView>(R.id.rvSemuaTransaksi)
        val btnClose    = dialogView.findViewById<View>(R.id.btnCloseDialog)

        val dialogAdapter = TransaksiAdapter()
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = dialogAdapter
        dialogAdapter.submitList(allTransaksi)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_dialog_rounded)
        // Tinggi dialog dibuat hampir penuh layar supaya RecyclerView punya
        // ruang cukup untuk scroll, bukan cuma tinggi sependek isi pertama
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            (resources.displayMetrics.heightPixels * 0.8).toInt()
        )

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun updateChartByTab() {
        val charts = lastResponse?.charts ?: return
        val points = when (activeChartTab) {
            "7_hari"  -> fillMissingDays(charts.daily, 7)
            "30_hari" -> fillMissingDays(charts.daily, 30)
            "2_bulan" -> fillMissingMonths(charts.monthly, 3)
            "6_bulan" -> fillMissingMonths(charts.monthly, 12)
            else      -> fillMissingDays(charts.daily, 7)
        }
        setupRevenueChart(points)
    }

    /**
     * Sama seperti fillMissingDays, tapi untuk rentang bulanan (3 Bulan/1 Tahun).
     * Kalau owner baru punya transaksi di 1 bulan saja, charts.monthly cuma
     * berisi 1 entry sehingga chart tidak bisa menggambar garis (butuh >=2 titik).
     * Fungsi ini mengisi bulan-bulan kosong dengan nilai 0 supaya garis tetap
     * tergambar penuh sepanjang rentang yang dipilih.
     */
    private fun fillMissingMonths(monthly: List<ChartPoint>, totalMonths: Int): List<ChartPoint> {
        val sdf = SimpleDateFormat("yyyy-MM", Locale("id"))
        val existingMap = monthly.associateBy { it.label }
        val result = mutableListOf<ChartPoint>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -(totalMonths - 1))

        for (i in 0 until totalMonths) {
            val monthStr = sdf.format(cal.time)
            result.add(existingMap[monthStr] ?: ChartPoint(monthStr, 0.0))
            cal.add(Calendar.MONTH, 1)
        }
        return result
    }

    /**
     * LineChart butuh minimal 2 titik untuk menggambar garis. Kalau booking
     * baru ada di 1-2 hari saja, data dari API jadi cuma 1-2 entry dan chart
     * terlihat kosong/titik doang. Fungsi ini mengisi hari-hari yang TIDAK
     * ada transaksi dengan nilai 0, supaya selalu ada rentang penuh (7/30 hari)
     * dan garis chart tetap tergambar dengan baik.
     */
    private fun fillMissingDays(daily: List<ChartPoint>, totalDays: Int): List<ChartPoint> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale("id"))
        val existingMap = daily.associateBy { it.label }
        val result = mutableListOf<ChartPoint>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, -(totalDays - 1))

        for (i in 0 until totalDays) {
            val dateStr = sdf.format(cal.time)
            result.add(existingMap[dateStr] ?: ChartPoint(dateStr, 0.0))
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return result
    }

    private fun setupRevenueChart(points: List<ChartPoint>) {
        if (points.isEmpty()) {
            chartRevenue.clear()
            chartRevenue.invalidate()
            return
        }

        val datasets = mutableListOf<ILineDataSet>()

        if (cbPendapatan.isChecked) {
            val entries = points.mapIndexed { i, p -> Entry(i.toFloat(), p.value.toFloat()) }
            val ds = LineDataSet(entries, "Pendapatan").apply {
                color = colorPrimary
                setCircleColor(colorPrimary)
                lineWidth = 2.5f
                circleRadius = 4f
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
                setDrawFilled(true)
                fillAlpha = 30
                fillColor = colorPrimary
            }
            datasets.add(ds)
        }

        if (cbPengeluaran.isChecked) {
            val entries = points.mapIndexed { i, _ -> Entry(i.toFloat(), 0f) }
            val ds = LineDataSet(entries, "Pengeluaran").apply {
                color = Color.parseColor("#D85A30")
                setCircleColor(Color.parseColor("#D85A30"))
                lineWidth = 2f
                circleRadius = 3f
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
                setDrawFilled(true)
                fillAlpha = 20
                fillColor = Color.parseColor("#D85A30")
            }
            datasets.add(ds)
        }

        if (datasets.isEmpty()) {
            chartRevenue.clear()
            chartRevenue.invalidate()
            return
        }

        chartRevenue.apply {
            data = LineData(datasets)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(points.map { formatChartLabel(it.label) })
                granularity = 1f
                setDrawGridLines(false)
                textColor = colorMuted
                textSize = 10f
            }
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = colorDivider
                textColor = colorMuted
                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float) = when {
                        value >= 1_000_000 -> "Rp ${(value / 1_000_000).toInt()} jt"
                        value >= 1_000     -> "Rp ${(value / 1_000).toInt()} rb"
                        else               -> "Rp ${value.toInt()}"
                    }
                }
            }
            axisRight.isEnabled   = false
            legend.isEnabled      = true
            description.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(false)
            animateX(600)
        }
    }

    private fun formatChartLabel(label: String): String {
        val bulan = listOf("","Jan","Feb","Mar","Apr","Mei","Jun","Jul","Agu","Sep","Okt","Nov","Des")
        return try {
            if (label.length == 7) bulan[label.substring(5).toInt()]
            else {
                val p = label.split("-")
                "${p[2].toInt()} ${bulan[p[1].toInt()]}"
            }
        } catch (e: Exception) { label }
    }

    private fun loadData() {
        // Map tab chart ke parameter filter PHP yang sesuai rentang waktunya:
        // 7 hari/30 hari -> filter custom dengan range tanggal pas
        // 2 bulan/6 bulan -> filter custom dengan range bulan
        val today = Calendar.getInstance()
        val (dateFrom, dateTo, filterParam) = when (activeChartTab) {
            "7_hari" -> Triple(
                formatDateForApi(addDays(today, -6)), formatDateForApi(today), "custom"
            )
            "30_hari" -> Triple(
                formatDateForApi(addDays(today, -29)), formatDateForApi(today), "custom"
            )
            "2_bulan" -> Triple(
                formatDateForApi(addMonths(today, -2)), formatDateForApi(today), "custom"
            )
            "6_bulan" -> Triple(
                formatDateForApi(addMonths(today, -12)), formatDateForApi(today), "custom"
            )
            else -> Triple("", "", "bulan")
        }

        viewModel.loadKeuangan(
            userId   = session.getUserId(),
            filter   = filterParam,
            dateFrom = dateFrom,
            dateTo   = dateTo
        )
    }

    private fun addDays(base: Calendar, days: Int): Calendar {
        val cal = base.clone() as Calendar
        cal.add(Calendar.DAY_OF_MONTH, days)
        return cal
    }

    private fun addMonths(base: Calendar, months: Int): Calendar {
        val cal = base.clone() as Calendar
        cal.add(Calendar.MONTH, months)
        return cal
    }

    private fun formatDateForApi(cal: Calendar): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale("id")).format(cal.time)
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }
        viewModel.error.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrEmpty())
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
        }
        viewModel.keuangan.observe(viewLifecycleOwner) { data ->
            data ?: return@observe
            lastResponse = data
            bindSummary(data)
            updateChartByTab()
            bindTable(data)
        }
    }

    private fun bindSummary(data: KeuanganResponse) {
        val s = data.summary ?: return

        // Pct change badge
        val pct = s.pctChange
        tvPctChange.text = "${if (pct >= 0) "+" else ""}${pct}% vs Bln Lalu"
        tvPctChange.setTextColor(if (pct >= 0) Color.parseColor("#4AE89A") else colorError)

        // Card laba bersih
        tvKeuanganBulan.text           = formatRp(s.bulan)
        tvKeuanganBiaya.text           = formatRp(0.0) // biaya dari PHP belum ada, placeholder
        tvKeuanganTotalKeuntungan.text = formatRp(s.bulan)

        // Card booking & denda
        tvKeuanganMinggu.text = formatRp(s.minggu)
        tvKeuanganDenda.text  = formatRp(0.0)

        // Card total booking & occupancy
        tvKeuanganTransaksi.text = "${s.totalTransaksi}"
        val occupancy = if (s.totalTransaksi > 0)
            ((s.totalTransaksi.toFloat() / (s.totalTransaksi + 5)) * 100).toInt()
        else 0
        tvKeuanganOccupancy.text = "$occupancy%"

        // Target -- sekarang pakai target_bulanan dari API (bisa diubah owner),
        // bukan hardcoded lagi
        targetBulanan = s.targetBulanan.takeIf { it > 0 } ?: targetBulanan
        bindTargetSection(s.bulan, targetBulanan)
    }

    private fun bindTargetSection(pencapaian: Double, target: Double) {
        val targetPct = if (target > 0)
            ((pencapaian / target) * 100).toInt().coerceIn(0, 100)
        else 0
        progressTarget.progress    = targetPct
        tvTargetPercent.text       = "$targetPct%"
        tvKeuanganPencapaian.text  = formatRp(pencapaian)
        tvKeuanganTarget.text      = formatRp(target)
    }

    private fun bindTable(data: KeuanganResponse) {
        val page = data.transaksi ?: return
        allTransaksi = page.data.toMutableList()
        // Tampilkan MAKSIMAL 3 transaksi di card "Transaksi Terbaru".
        // Sisanya bisa dilihat lewat dialog "Lihat Semua Transaksi".
        adapter.submitList(allTransaksi.take(3))
        tvEmpty.visibility = if (allTransaksi.isEmpty()) View.VISIBLE else View.GONE
    }

    // ─── Export ──────────────────────────────────────────────────────────────
    private fun setupExport() {
        btnExportLaporan.setOnClickListener { showExportPopup() }
    }

    private fun showExportPopup() {
        val popupView = LayoutInflater.from(requireContext())
            .inflate(R.layout.popup_export_laporan, null)

        val density    = resources.displayMetrics.density
        val screenW    = resources.displayMetrics.widthPixels
        val popupWidth = screenW - (32 * density).toInt()

        val popup = PopupWindow(
            popupView, popupWidth, ViewGroup.LayoutParams.WRAP_CONTENT, true
        ).apply {
            elevation = 24f
            isOutsideTouchable = true
            setBackgroundDrawable(
                ContextCompat.getDrawable(requireContext(), android.R.color.transparent)
            )
            setOnDismissListener {
                requireActivity().window.attributes =
                    requireActivity().window.attributes.also { it.alpha = 1.0f }
            }
        }

        requireActivity().window.attributes =
            requireActivity().window.attributes.also { it.alpha = 0.6f }

        val tvFrom            = popupView.findViewById<TextView>(R.id.popupDateFrom)
        val tvTo              = popupView.findViewById<TextView>(R.id.popupDateTo)
        val layoutJenis       = popupView.findViewById<LinearLayout>(R.id.layoutJenisLaporan)
        val btnPdf            = popupView.findViewById<Button>(R.id.popupBtnPdf)
        val btnClose          = popupView.findViewById<View>(R.id.popupBtnClose)
        btnClose.setOnClickListener { popup.dismiss() }

        val sdfDisplay = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        tvFrom.text = sdfDisplay.format(Date())
        tvTo.text   = sdfDisplay.format(Date())

        tvFrom.setOnClickListener {
            MaterialDatePicker.Builder.datePicker().setTitleText("Dari Tanggal").build().also { p ->
                p.addOnPositiveButtonClickListener { ms -> tvFrom.text = sdfDisplay.format(Date(ms)) }
                p.show(parentFragmentManager, "from")
            }
        }
        tvTo.setOnClickListener {
            MaterialDatePicker.Builder.datePicker().setTitleText("Sampai Tanggal").build().also { p ->
                p.addOnPositiveButtonClickListener { ms -> tvTo.text = sdfDisplay.format(Date(ms)) }
                p.show(parentFragmentManager, "to")
            }
        }

        // Chip pilihan jenis laporan -- ganti Spinner default yang terlihat kaku
        val jenisLaporanList = listOf("Laporan Booking", "Laporan Pendapatan", "Rekap Bulanan")
        var selectedIndex = 0
        val chipViews = mutableListOf<View>()
        jenisLaporanList.forEachIndexed { index, label ->
            val chip = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_jenis_laporan, layoutJenis, false)
            val tvName  = chip.findViewById<TextView>(R.id.tvJenisLaporanName)
            val ivCheck = chip.findViewById<View>(R.id.ivJenisLaporanCheck)
            tvName.text = label
            ivCheck.visibility = if (index == 0) View.VISIBLE else View.INVISIBLE
            chip.setOnClickListener {
                selectedIndex = index
                chipViews.forEachIndexed { i, v ->
                    v.findViewById<View>(R.id.ivJenisLaporanCheck).visibility =
                        if (i == index) View.VISIBLE else View.INVISIBLE
                }
            }
            chipViews.add(chip)
            layoutJenis.addView(chip)
        }

        btnPdf.setOnClickListener { popup.dismiss(); exportPdf() }

        requireView().post {
            val btnLoc = IntArray(2)
            btnExportLaporan.getLocationInWindow(btnLoc)
            val xPos = (screenW - popupWidth) / 2
            val yPos = btnLoc[1] + btnExportLaporan.height + (8 * density).toInt()
            popup.showAtLocation(requireView(), android.view.Gravity.NO_GRAVITY, xPos, yPos)
        }
    }

    private fun exportPdf() {
        val data = lastResponse ?: run {
            Toast.makeText(requireContext(), "Data belum tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val dir  = requireContext().cacheDir
            val name = "laporan_keuangan_${System.currentTimeMillis()}.pdf"
            val file = File(dir, name)
            val doc  = com.itextpdf.text.Document()
            com.itextpdf.text.pdf.PdfWriter.getInstance(doc, file.outputStream())
            doc.open()

            val green     = com.itextpdf.text.BaseColor(26, 77, 46)
            val titleFont = com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18f, com.itextpdf.text.Font.BOLD, green)
            val headFont  = com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12f, com.itextpdf.text.Font.BOLD)
            val bodyFont  = com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10f)

            doc.add(com.itextpdf.text.Paragraph("FindFutsall", titleFont).apply { alignment = com.itextpdf.text.Element.ALIGN_CENTER })
            doc.add(com.itextpdf.text.Paragraph("Laporan Keuangan — ${data.period?.from} s/d ${data.period?.to}", bodyFont).apply { alignment = com.itextpdf.text.Element.ALIGN_CENTER })
            doc.add(com.itextpdf.text.Paragraph("Dicetak: ${SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id")).format(Date())}", bodyFont).apply { alignment = com.itextpdf.text.Element.ALIGN_CENTER })
            doc.add(com.itextpdf.text.Paragraph("\n"))
            doc.add(com.itextpdf.text.Paragraph("RINGKASAN KEUANGAN", headFont))

            val s = data.summary
            if (s != null) {
                val sumTable = com.itextpdf.text.pdf.PdfPTable(2).apply { widthPercentage = 100f }
                listOf(
                    "Pendapatan Hari Ini"   to formatRp(s.hari),
                    "Pendapatan Minggu Ini" to formatRp(s.minggu),
                    "Pendapatan Bulan Ini"  to formatRp(s.bulan),
                    "Pendapatan Tahun Ini"  to formatRp(s.tahun),
                    "Total Transaksi"       to "${s.totalTransaksi} booking",
                    "Rata-rata per Booking" to formatRp(s.rataRata)
                ).forEach { (k, v) ->
                    sumTable.addCell(com.itextpdf.text.pdf.PdfPCell(com.itextpdf.text.Phrase(k, bodyFont)).apply { border = 0; paddingBottom = 6f })
                    sumTable.addCell(com.itextpdf.text.pdf.PdfPCell(com.itextpdf.text.Phrase(v, bodyFont)).apply { border = 0; paddingBottom = 6f; horizontalAlignment = com.itextpdf.text.Element.ALIGN_RIGHT })
                }
                doc.add(sumTable)
            }

            doc.add(com.itextpdf.text.Paragraph("\n"))
            doc.add(com.itextpdf.text.Paragraph("RIWAYAT BOOKING", headFont))

            val cols  = arrayOf("ID","Penyewa","Lapangan","Tanggal","Waktu","Status","Nominal")
            val tbl   = com.itextpdf.text.pdf.PdfPTable(cols.size).apply {
                widthPercentage = 100f
                setWidths(floatArrayOf(0.5f,1.5f,1.5f,1f,1f,1f,1.2f))
            }
            val whiteFont = com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9f, com.itextpdf.text.Font.BOLD, com.itextpdf.text.BaseColor.WHITE)
            cols.forEach { col ->
                tbl.addCell(com.itextpdf.text.pdf.PdfPCell(com.itextpdf.text.Phrase(col, whiteFont)).apply { backgroundColor = green; paddingBottom = 6f })
            }
            allTransaksi.forEach { t ->
                listOf(
                    "#${t.id}",
                    t.customerName ?: "-",
                    t.fieldName ?: "-",
                    t.playDate ?: "-",
                    "${t.startTime ?: "-"}-${t.endTime ?: "-"}",
                    t.bookingStatus ?: "-",
                    formatRp(t.totalPrice ?: 0.0)
                ).forEach { cell ->
                    tbl.addCell(com.itextpdf.text.pdf.PdfPCell(com.itextpdf.text.Phrase(cell, bodyFont)).apply { paddingBottom = 4f })
                }
            }
            doc.add(tbl)
            doc.close()

            val uri = androidx.core.content.FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Buka PDF dengan..."))

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gagal export PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun formatRp(amount: Double): String {
        val fmt = NumberFormat.getNumberInstance(Locale("id", "ID"))
        return "Rp ${fmt.format(amount.toLong())}"
    }
}