package com.utama.findfutsall.ui.owner

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.*
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.utama.findfutsall.R
import com.utama.findfutsall.data.model.*
import com.utama.findfutsall.utils.SessionManager
import com.utama.findfutsall.viewmodel.KeuanganViewModel
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class OwnerKeuanganFragment : Fragment() {

    private val viewModel: KeuanganViewModel by viewModels()
    private lateinit var session: SessionManager
    private lateinit var adapter: TransaksiAdapter

    // Views – Summary cards
    private lateinit var tvHari: TextView
    private lateinit var tvMinggu: TextView
    private lateinit var tvBulan: TextView
    private lateinit var tvTahun: TextView
    private lateinit var tvTransaksi: TextView
    private lateinit var tvRataRata: TextView
    private lateinit var tvPctChange: TextView
    private lateinit var ivPctIcon: ImageView

    // Views – Filter
    private lateinit var chipGroupFilter: ChipGroup
    private lateinit var chipHari: Chip
    private lateinit var chipMinggu: Chip
    private lateinit var chipBulan: Chip
    private lateinit var chipTahun: Chip
    private lateinit var chipCustom: Chip
    private lateinit var chipGroupStatus: ChipGroup
    private lateinit var layoutCustomDate: View
    private lateinit var tvDateFrom: TextView
    private lateinit var tvDateTo: TextView
    private lateinit var btnApplyDate: Button

    // Views – Chart
    private lateinit var chartMonthly: LineChart
    private lateinit var chartWeekly: BarChart
    private lateinit var chartDaily: LineChart
    private lateinit var chartMetode: PieChart
    private lateinit var chartCompare: LineChart
    private lateinit var tabCharts: TabHost

    // Views – Tabel
    private lateinit var rvTransaksi: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var tvEmpty: TextView
    private lateinit var btnPrev: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var tvPage: TextView

    // Views – Export
    private lateinit var btnExportPdf: Button
    private lateinit var btnExportExcel: Button

    // Views – Loading
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutContent: View

    // State
    private var activeFilter = "bulan"
    private var activeStatus = ""
    private var customDateFrom = ""
    private var customDateTo   = ""
    private var currentPage    = 1
    private var totalPages     = 1
    private var allTransaksi   = mutableListOf<TransaksiItem>()
    private var lastResponse: KeuanganResponse? = null

    // Colors
    private val colorGreen   = Color.parseColor("#2ECC71")
    private val colorNavy    = Color.parseColor("#1A2B4A")
    private val colorGold    = Color.parseColor("#F5A623")
    private val colorRed     = Color.parseColor("#E74C3C")
    private val colorPurple  = Color.parseColor("#9B59B6")
    private val colorGray    = Color.parseColor("#95A5A6")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_owner_keuangan, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session = SessionManager(requireContext())

        bindViews(view)
        setupRecyclerView()
        setupFilterChips()
        setupStatusChips()
        setupCustomDate()
        setupSearch()
        setupPagination()
        setupExport()
        observeViewModel()

        loadData()
    }

    // ─── Bind Views ────────────────────────────────────────────────────────────
    private fun bindViews(v: View) {
        tvHari        = v.findViewById(R.id.tvKeuanganHari)
        tvMinggu      = v.findViewById(R.id.tvKeuanganMinggu)
        tvBulan       = v.findViewById(R.id.tvKeuanganBulan)
        tvTahun       = v.findViewById(R.id.tvKeuanganTahun)
        tvTransaksi   = v.findViewById(R.id.tvKeuanganTransaksi)
        tvRataRata    = v.findViewById(R.id.tvKeuanganRataRata)
        tvPctChange   = v.findViewById(R.id.tvPctChange)
        ivPctIcon     = v.findViewById(R.id.ivPctIcon)

        chipGroupFilter  = v.findViewById(R.id.chipGroupFilterKeuangan)
        chipHari         = v.findViewById(R.id.chipKeuHari)
        chipMinggu       = v.findViewById(R.id.chipKeuMinggu)
        chipBulan        = v.findViewById(R.id.chipKeuBulan)
        chipTahun        = v.findViewById(R.id.chipKeuTahun)
        chipCustom       = v.findViewById(R.id.chipKeuCustom)
        chipGroupStatus  = v.findViewById(R.id.chipGroupStatusKeuangan)
        layoutCustomDate = v.findViewById(R.id.layoutCustomDate)
        tvDateFrom       = v.findViewById(R.id.tvDateFrom)
        tvDateTo         = v.findViewById(R.id.tvDateTo)
        btnApplyDate     = v.findViewById(R.id.btnApplyDate)

        chartMonthly  = v.findViewById(R.id.chartMonthly)
        chartWeekly   = v.findViewById(R.id.chartWeekly)
        chartDaily    = v.findViewById(R.id.chartDaily)
        chartMetode   = v.findViewById(R.id.chartMetode)
        chartCompare  = v.findViewById(R.id.chartCompare)
        tabCharts     = v.findViewById(R.id.tabCharts)

        rvTransaksi   = v.findViewById(R.id.rvTransaksiKeuangan)
        searchView    = v.findViewById(R.id.searchTransaksi)
        tvEmpty       = v.findViewById(R.id.tvTransaksiEmpty)
        btnPrev       = v.findViewById(R.id.btnPagePrev)
        btnNext       = v.findViewById(R.id.btnPageNext)
        tvPage        = v.findViewById(R.id.tvPageInfo)

        btnExportPdf   = v.findViewById(R.id.btnExportPdf)
        btnExportExcel = v.findViewById(R.id.btnExportExcel)

        progressBar   = v.findViewById(R.id.progressKeuangan)
        layoutContent = v.findViewById(R.id.layoutKeuanganContent)
    }

    // ─── RecyclerView ──────────────────────────────────────────────────────────
    private fun setupRecyclerView() {
        adapter = TransaksiAdapter()
        rvTransaksi.layoutManager = LinearLayoutManager(requireContext())
        rvTransaksi.adapter = adapter
    }

    // ─── Filter Chips ──────────────────────────────────────────────────────────
    private fun setupFilterChips() {
        chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            layoutCustomDate.visibility = View.GONE
            activeFilter = when {
                checkedIds.contains(R.id.chipKeuHari)   -> "hari"
                checkedIds.contains(R.id.chipKeuMinggu) -> "minggu"
                checkedIds.contains(R.id.chipKeuTahun)  -> "tahun"
                checkedIds.contains(R.id.chipKeuCustom) -> {
                    layoutCustomDate.visibility = View.VISIBLE
                    "custom"
                }
                else -> "bulan"
            }
            if (activeFilter != "custom") {
                currentPage = 1
                loadData()
            }
        }
    }

    private fun setupStatusChips() {
        chipGroupStatus.setOnCheckedStateChangeListener { _, checkedIds ->
            activeStatus = when {
                checkedIds.contains(R.id.chipStatusTerkonfirmasi) -> "Terkonfirmasi"
                checkedIds.contains(R.id.chipStatusMenunggu)      -> "Menunggu"
                checkedIds.contains(R.id.chipStatusBatal)         -> "Batal"
                else -> ""
            }
            currentPage = 1
            loadData()
        }
    }

    // ─── Custom Date ───────────────────────────────────────────────────────────
    private fun setupCustomDate() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfDisplay = SimpleDateFormat("dd MMM yyyy", Locale("id"))

        tvDateFrom.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Tanggal Mulai").build()
            picker.addOnPositiveButtonClickListener { ms ->
                customDateFrom = sdf.format(Date(ms))
                tvDateFrom.text = sdfDisplay.format(Date(ms))
            }
            picker.show(parentFragmentManager, "date_from")
        }

        tvDateTo.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Tanggal Akhir").build()
            picker.addOnPositiveButtonClickListener { ms ->
                customDateTo = sdf.format(Date(ms))
                tvDateTo.text = sdfDisplay.format(Date(ms))
            }
            picker.show(parentFragmentManager, "date_to")
        }

        btnApplyDate.setOnClickListener {
            if (customDateFrom.isEmpty() || customDateTo.isEmpty()) {
                Toast.makeText(requireContext(), "Pilih tanggal mulai dan akhir", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            currentPage = 1
            loadData()
        }
    }

    // ─── Search ────────────────────────────────────────────────────────────────
    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?) = false
            override fun onQueryTextChange(q: String?): Boolean {
                filterLocal(q.orEmpty())
                return true
            }
        })
    }

    private fun filterLocal(query: String) {
        val filtered = if (query.isBlank()) allTransaksi
        else allTransaksi.filter {
            it.customerName.contains(query, true) ||
                    it.fieldName.contains(query, true) ||
                    it.id.toString().contains(query)
        }
        adapter.submitList(filtered)
        tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    // ─── Pagination ────────────────────────────────────────────────────────────
    private fun setupPagination() {
        btnPrev.setOnClickListener {
            if (currentPage > 1) { currentPage--; loadData() }
        }
        btnNext.setOnClickListener {
            if (currentPage < totalPages) { currentPage++; loadData() }
        }
    }

    private fun updatePaginationUI() {
        tvPage.text = "Halaman $currentPage dari $totalPages"
        btnPrev.isEnabled = currentPage > 1
        btnNext.isEnabled = currentPage < totalPages
        btnPrev.alpha = if (currentPage > 1) 1f else 0.4f
        btnNext.alpha = if (currentPage < totalPages) 1f else 0.4f
    }

    // ─── Load Data ─────────────────────────────────────────────────────────────
    private fun loadData() {
        val userId = session.getUserId()
        viewModel.loadKeuangan(
            userId      = userId,
            filter      = activeFilter,
            status      = activeStatus,
            dateFrom    = if (activeFilter == "custom") customDateFrom else "",
            dateTo      = if (activeFilter == "custom") customDateTo else "",
            page        = currentPage
        )
    }

    // ─── Observer ──────────────────────────────────────────────────────────────
    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            progressBar.visibility  = if (loading) View.VISIBLE else View.GONE
            layoutContent.visibility = if (loading) View.GONE else View.VISIBLE
        }

        viewModel.error.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrEmpty())
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
        }

        viewModel.keuangan.observe(viewLifecycleOwner) { data ->
            data ?: return@observe
            lastResponse = data
            bindSummary(data)
            bindCharts(data)
            bindTable(data)
        }
    }

    // ─── Bind Summary Cards ────────────────────────────────────────────────────
    private fun bindSummary(data: KeuanganResponse) {
        val s = data.summary ?: return
        tvHari.text      = formatRp(s.hari)
        tvMinggu.text    = formatRp(s.minggu)
        tvBulan.text     = formatRp(s.bulan)
        tvTahun.text     = formatRp(s.tahun)
        tvTransaksi.text = "${s.totalTransaksi} booking"
        tvRataRata.text  = formatRp(s.rataRata)

        val pct = s.pctChange
        tvPctChange.text = "${if (pct >= 0) "+" else ""}${pct}% vs periode lalu"
        tvPctChange.setTextColor(if (pct >= 0) colorGreen else colorRed)
        ivPctIcon.setImageResource(
            if (pct >= 0) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down
        )
        ivPctIcon.setColorFilter(if (pct >= 0) colorGreen else colorRed)
    }

    // ─── Bind Charts ───────────────────────────────────────────────────────────
    private fun bindCharts(data: KeuanganResponse) {
        val charts = data.charts ?: return

        // Line chart – bulanan
        setupLineChart(
            chart  = chartMonthly,
            points = charts.monthly,
            label  = "Pendapatan Bulanan",
            color  = colorNavy
        )

        // Bar chart – mingguan
        setupBarChart(charts.weekly)

        // Line chart – harian (area style)
        setupLineChart(
            chart  = chartDaily,
            points = charts.daily,
            label  = "Pendapatan Harian",
            color  = colorGreen,
            filled = true
        )

        // Pie chart – metode pembayaran
        setupPieChart(charts.metode)

        // Compare – line chart dual
        setupCompareChart(charts.compare)
    }

    private fun setupLineChart(
        chart: LineChart,
        points: List<ChartPoint>,
        label: String,
        color: Int,
        filled: Boolean = false
    ) {
        if (points.isEmpty()) { chart.visibility = View.GONE; return }
        chart.visibility = View.VISIBLE

        val entries = points.mapIndexed { i, p -> Entry(i.toFloat(), p.value.toFloat()) }
        val ds = LineDataSet(entries, label).apply {
            this.color = color
            setCircleColor(color)
            lineWidth = 2.5f
            circleRadius = 4f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            if (filled) {
                setDrawFilled(true)
                fillAlpha = 40
                fillColor = color
            }
        }

        chart.apply {
            this.data = LineData(ds)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(points.map { it.label })
                granularity = 1f
                setDrawGridLines(false)
                textColor = Color.parseColor("#888888")
                textSize = 10f
                labelRotationAngle = -30f
            }
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#F0F0F0")
                textColor = Color.parseColor("#888888")
            }
            axisRight.isEnabled = false
            legend.isEnabled = false
            description.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(false)
            animateX(800)
        }
    }

    private fun setupBarChart(points: List<ChartPoint>) {
        if (points.isEmpty()) { chartWeekly.visibility = View.GONE; return }
        chartWeekly.visibility = View.VISIBLE

        val entries = points.mapIndexed { i, p -> BarEntry(i.toFloat(), p.value.toFloat()) }
        val ds = BarDataSet(entries, "Pendapatan Mingguan").apply {
            colors = listOf(colorNavy, colorGreen, colorGold, colorPurple)
            setDrawValues(true)
            valueTextSize = 9f
        }

        chartWeekly.apply {
            data = BarData(ds)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(points.map { it.label })
                granularity = 1f
                setDrawGridLines(false)
                textColor = Color.parseColor("#888888")
            }
            axisLeft.textColor = Color.parseColor("#888888")
            axisRight.isEnabled = false
            legend.isEnabled = false
            description.isEnabled = false
            setFitBars(true)
            animateY(800)
        }
    }

    private fun setupPieChart(metode: List<ChartMetode>) {
        if (metode.isEmpty()) { chartMetode.visibility = View.GONE; return }
        chartMetode.visibility = View.VISIBLE

        val entries = metode.map { PieEntry(it.value.toFloat(), it.label) }
        val colors  = listOf(colorNavy, colorGreen, colorGold, colorPurple, colorRed, colorGray)

        val ds = PieDataSet(entries, "").apply {
            this.colors = colors.take(entries.size)
            sliceSpace = 3f
            selectionShift = 6f
        }

        chartMetode.apply {
            data = PieData(ds).apply {
                setValueTextSize(11f)
                setValueTextColor(Color.WHITE)
            }
            isDrawHoleEnabled = true
            holeRadius = 52f
            setHoleColor(Color.WHITE)
            setTransparentCircleAlpha(0)
            setCenterText("Metode\nPembayaran")
            setCenterTextSize(12f)
            description.isEnabled = false
            legend.isEnabled = true
            animateY(1000)
        }
    }

    private fun setupCompareChart(compare: List<ChartCompare>) {
        if (compare.isEmpty()) { chartCompare.visibility = View.GONE; return }

        val thisYear = compare.mapIndexed { i, c -> Entry(i.toFloat(), c.thisYear.toFloat()) }
        val lastYear = compare.mapIndexed { i, c -> Entry(i.toFloat(), c.lastYear.toFloat()) }

        val dsThis = LineDataSet(thisYear, "Tahun Ini").apply {
            color = colorGreen; setCircleColor(colorGreen); lineWidth = 2.5f
            setDrawValues(false); mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        val dsLast = LineDataSet(lastYear, "Tahun Lalu").apply {
            color = colorGray; setCircleColor(colorGray); lineWidth = 2f
            enableDashedLine(10f, 5f, 0f)
            setDrawValues(false); mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        chartCompare.apply {
            data = LineData(dsThis, dsLast)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(compare.map { it.label })
                granularity = 1f; setDrawGridLines(false)
                textColor = Color.parseColor("#888888")
            }
            axisLeft.textColor = Color.parseColor("#888888")
            axisRight.isEnabled = false
            description.isEnabled = false
            animateX(800)
        }
    }

    // ─── Bind Table ────────────────────────────────────────────────────────────
    private fun bindTable(data: KeuanganResponse) {
        val page = data.transaksi ?: return
        totalPages = page.totalPages.coerceAtLeast(1)
        allTransaksi = page.data.toMutableList()
        adapter.submitList(page.data)
        tvEmpty.visibility = if (page.data.isEmpty()) View.VISIBLE else View.GONE
        updatePaginationUI()
    }

    // ─── Export ────────────────────────────────────────────────────────────────
    private fun setupExport() {
        btnExportPdf.setOnClickListener   { exportPdf() }
        btnExportExcel.setOnClickListener { exportExcel() }
    }

    private fun exportPdf() {
        val data = lastResponse ?: run {
            Toast.makeText(requireContext(), "Data belum tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val dir  = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val name = "laporan_keuangan_${System.currentTimeMillis()}.pdf"
            val file = File(dir, name)

            // iText PDF generation
            val doc = com.itextpdf.text.Document()
            com.itextpdf.text.pdf.PdfWriter.getInstance(doc, file.outputStream())
            doc.open()

            val titleFont = com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 18f,
                com.itextpdf.text.Font.BOLD, com.itextpdf.text.BaseColor(26, 43, 74)
            )
            val headerFont = com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 12f,
                com.itextpdf.text.Font.BOLD
            )
            val bodyFont = com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 10f
            )

            // Header
            doc.add(com.itextpdf.text.Paragraph("FindFutsall", titleFont).apply {
                alignment = com.itextpdf.text.Element.ALIGN_CENTER
            })
            doc.add(com.itextpdf.text.Paragraph(
                "Laporan Keuangan — ${data.period?.from} s/d ${data.period?.to}",
                bodyFont
            ).apply { alignment = com.itextpdf.text.Element.ALIGN_CENTER })
            doc.add(com.itextpdf.text.Paragraph(
                "Dicetak: ${java.text.SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id")).format(java.util.Date())}",
                bodyFont
            ).apply { alignment = com.itextpdf.text.Element.ALIGN_CENTER })
            doc.add(com.itextpdf.text.Paragraph("\n"))

            // Summary
            doc.add(com.itextpdf.text.Paragraph("RINGKASAN KEUANGAN", headerFont))
            val s = data.summary
            if (s != null) {
                val sumTable = com.itextpdf.text.pdf.PdfPTable(2).apply { widthPercentage = 100f }
                listOf(
                    "Pendapatan Hari Ini" to formatRp(s.hari),
                    "Pendapatan Minggu Ini" to formatRp(s.minggu),
                    "Pendapatan Bulan Ini" to formatRp(s.bulan),
                    "Pendapatan Tahun Ini" to formatRp(s.tahun),
                    "Total Transaksi" to "${s.totalTransaksi} booking",
                    "Rata-rata per Booking" to formatRp(s.rataRata)
                ).forEach { (k, v) ->
                    sumTable.addCell(com.itextpdf.text.pdf.PdfPCell(
                        com.itextpdf.text.Phrase(k, bodyFont)
                    ).apply { border = 0; paddingBottom = 6f })
                    sumTable.addCell(com.itextpdf.text.pdf.PdfPCell(
                        com.itextpdf.text.Phrase(v, bodyFont)
                    ).apply { border = 0; paddingBottom = 6f;
                        horizontalAlignment = com.itextpdf.text.Element.ALIGN_RIGHT })
                }
                doc.add(sumTable)
            }

            doc.add(com.itextpdf.text.Paragraph("\n"))

            // Tabel transaksi
            doc.add(com.itextpdf.text.Paragraph("DETAIL TRANSAKSI", headerFont))
            val cols = arrayOf("ID","Penyewa","Lapangan","Tanggal","Waktu","Metode","Status","Nominal","Bersih")
            val tbl = com.itextpdf.text.pdf.PdfPTable(cols.size).apply {
                widthPercentage = 100f
                setWidths(floatArrayOf(0.5f,1.5f,1.5f,1f,1f,1f,1f,1.2f,1.2f))
            }
            cols.forEach { col ->
                tbl.addCell(com.itextpdf.text.pdf.PdfPCell(
                    com.itextpdf.text.Phrase(col, headerFont)
                ).apply {
                    backgroundColor = com.itextpdf.text.BaseColor(26, 43, 74)
                    com.itextpdf.text.Phrase(col, com.itextpdf.text.Font(
                        com.itextpdf.text.Font.FontFamily.HELVETICA, 9f,
                        com.itextpdf.text.Font.BOLD,
                        com.itextpdf.text.BaseColor.WHITE
                    )).also { phrase -> this.phrase = phrase }
                    paddingBottom = 6f
                })
            }

            allTransaksi.forEach { t ->
                listOf(
                    "#${t.id}", t.customerName, t.fieldName, t.playDate,
                    "${t.startTime}-${t.endTime}", t.paymentMethod,
                    t.bookingStatus, formatRp(t.totalPrice), formatRp(t.pendapatanBersih)
                ).forEach { cell ->
                    tbl.addCell(com.itextpdf.text.pdf.PdfPCell(
                        com.itextpdf.text.Phrase(cell, bodyFont)
                    ).apply { paddingBottom = 4f })
                }
            }

            doc.add(tbl)
            doc.close()

            Toast.makeText(requireContext(), "PDF tersimpan: $name", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gagal export PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun exportExcel() {
        val data = lastResponse ?: run {
            Toast.makeText(requireContext(), "Data belum tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val wb    = org.apache.poi.xssf.usermodel.XSSFWorkbook()
            val sdf   = java.text.SimpleDateFormat("dd MMM yyyy", Locale("id"))

            // ── Sheet 1: Ringkasan ──────────────────────────────────────────
            val ws1  = wb.createSheet("Ringkasan")
            val s    = data.summary
            val hStyle = wb.createCellStyle().apply {
                fillForegroundColor = org.apache.poi.ss.usermodel.IndexedColors.BLUE.index
                fillPattern = org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND
                val f = wb.createFont().apply {
                    bold = true
                    color = org.apache.poi.ss.usermodel.IndexedColors.WHITE.index
                }
                setFont(f)
            }

            ws1.createRow(0).also { r ->
                r.createCell(0).apply { setCellValue("Laporan Keuangan FindFutsall"); cellStyle = hStyle }
            }
            ws1.createRow(1).also { r ->
                r.createCell(0).setCellValue("Periode: ${data.period?.from} s/d ${data.period?.to}")
            }
            ws1.createRow(3).also { r ->
                r.createCell(0).apply { setCellValue("Keterangan"); cellStyle = hStyle }
                r.createCell(1).apply { setCellValue("Nilai"); cellStyle = hStyle }
            }
            if (s != null) {
                listOf(
                    "Pendapatan Hari Ini" to s.hari,
                    "Pendapatan Minggu Ini" to s.minggu,
                    "Pendapatan Bulan Ini" to s.bulan,
                    "Pendapatan Tahun Ini" to s.tahun,
                    "Total Transaksi" to s.totalTransaksi.toDouble(),
                    "Rata-rata per Booking" to s.rataRata
                ).forEachIndexed { i, (k, v) ->
                    ws1.createRow(4 + i).also { r ->
                        r.createCell(0).setCellValue(k)
                        r.createCell(1).setCellValue(v)
                    }
                }
            }
            ws1.autoSizeColumn(0); ws1.autoSizeColumn(1)

            // ── Sheet 2: Transaksi ──────────────────────────────────────────
            val ws2 = wb.createSheet("Transaksi")
            ws2.createRow(0).also { r ->
                listOf("ID","Penyewa","Lapangan","Tanggal","Waktu","Metode",
                    "Status Booking","Status Bayar","Nominal","Biaya Admin","Pendapatan Bersih"
                ).forEachIndexed { i, h ->
                    r.createCell(i).apply { setCellValue(h); cellStyle = hStyle }
                }
            }
            allTransaksi.forEachIndexed { rowIdx, t ->
                ws2.createRow(rowIdx + 1).also { r ->
                    r.createCell(0).setCellValue(t.id.toDouble())
                    r.createCell(1).setCellValue(t.customerName)
                    r.createCell(2).setCellValue(t.fieldName)
                    r.createCell(3).setCellValue(t.playDate)
                    r.createCell(4).setCellValue("${t.startTime}-${t.endTime}")
                    r.createCell(5).setCellValue(t.paymentMethod)
                    r.createCell(6).setCellValue(t.bookingStatus)
                    r.createCell(7).setCellValue(t.paymentStatus)
                    r.createCell(8).setCellValue(t.totalPrice)
                    r.createCell(9).setCellValue(t.serviceFee)
                    r.createCell(10).setCellValue(t.pendapatanBersih)
                }
            }
            for (i in 0..10) ws2.autoSizeColumn(i)

            // ── Sheet 3: Rekap Bulanan ──────────────────────────────────────
            val ws3 = wb.createSheet("Rekap Bulanan")
            ws3.createRow(0).also { r ->
                r.createCell(0).apply { setCellValue("Bulan"); cellStyle = hStyle }
                r.createCell(1).apply { setCellValue("Total Pendapatan"); cellStyle = hStyle }
            }
            data.charts?.monthly?.forEachIndexed { i, p ->
                ws3.createRow(i + 1).also { r ->
                    r.createCell(0).setCellValue(p.label)
                    r.createCell(1).setCellValue(p.value)
                }
            }
            ws3.autoSizeColumn(0); ws3.autoSizeColumn(1)

            // ── Simpan file ─────────────────────────────────────────────────
            val dir  = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val name = "laporan_keuangan_${System.currentTimeMillis()}.xlsx"
            val file = File(dir, name)
            file.outputStream().use { wb.write(it) }
            wb.close()

            Toast.makeText(requireContext(), "Excel tersimpan: $name", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gagal export Excel: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // ─── Utility ───────────────────────────────────────────────────────────────
    private fun formatRp(amount: Double): String {
        val fmt = NumberFormat.getNumberInstance(Locale("id", "ID"))
        return "Rp ${fmt.format(amount.toLong())}"
    }
}