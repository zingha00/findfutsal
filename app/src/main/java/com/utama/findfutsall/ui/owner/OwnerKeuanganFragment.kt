package com.utama.findfutsall.ui.owner

import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
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

    private lateinit var tvHari: TextView
    private lateinit var tvMinggu: TextView
    private lateinit var tvBulan: TextView
    private lateinit var tvTahun: TextView
    private lateinit var tvTransaksi: TextView
    private lateinit var tvRataRata: TextView
    private lateinit var tvPctChange: TextView
    private lateinit var ivPctIcon: ImageView

    private lateinit var chartRevenue: LineChart
    private lateinit var tabChart7Hari: TextView
    private lateinit var tabChart30Hari: TextView
    private lateinit var tabChart2Bulan: TextView
    private lateinit var tabChart6Bulan: TextView
    private lateinit var tabChart1Tahun: TextView
    private var activeChartTab = "7_hari"

    private lateinit var btnExportLaporan: Button
    private lateinit var rvTransaksi: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var progressBar: ProgressBar

    private var allTransaksi = mutableListOf<TransaksiItem>()
    private var lastResponse: KeuanganResponse? = null

    private val colorPrimary   get() = ContextCompat.getColor(requireContext(), R.color.primary_green)
    private val colorError     get() = ContextCompat.getColor(requireContext(), R.color.error_red)
    private val colorDivider   get() = ContextCompat.getColor(requireContext(), R.color.divider)
    private val colorMuted     get() = ContextCompat.getColor(requireContext(), R.color.text_muted)
    private val colorSecondary get() = ContextCompat.getColor(requireContext(), R.color.text_secondary)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_owner_keuangan, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session = SessionManager(requireContext())
        bindViews(view)
        view.post {
            (view as? androidx.core.widget.NestedScrollView)?.scrollTo(0, 0)
        }
        setupRecyclerView()
        setupChartFilterTabs()
        setupExport()
        observeViewModel()
        loadData()
    }

    private fun bindViews(v: View) {
        tvHari           = v.findViewById(R.id.tvKeuanganHari)
        tvMinggu         = v.findViewById(R.id.tvKeuanganMinggu)
        tvBulan          = v.findViewById(R.id.tvKeuanganBulan)
        tvTahun          = v.findViewById(R.id.tvKeuanganTahun)
        tvTransaksi      = v.findViewById(R.id.tvKeuanganTransaksi)
        tvRataRata       = v.findViewById(R.id.tvKeuanganRataRata)
        tvPctChange      = v.findViewById(R.id.tvPctChange)
        ivPctIcon        = v.findViewById(R.id.ivPctIcon)
        chartRevenue     = v.findViewById(R.id.chartRevenue)
        tabChart7Hari    = v.findViewById(R.id.tabChart7Hari)
        tabChart30Hari   = v.findViewById(R.id.tabChart30Hari)
        tabChart2Bulan   = v.findViewById(R.id.tabChart2Bulan)
        tabChart6Bulan   = v.findViewById(R.id.tabChart6Bulan)
        tabChart1Tahun   = v.findViewById(R.id.tabChart1Tahun)
        btnExportLaporan = v.findViewById(R.id.btnExportLaporan)
        rvTransaksi      = v.findViewById(R.id.rvTransaksiKeuangan)
        tvEmpty          = v.findViewById(R.id.tvTransaksiEmpty)
        progressBar      = v.findViewById(R.id.progressKeuangan)
    }

    private fun setupRecyclerView() {
        adapter = TransaksiAdapter()
        rvTransaksi.layoutManager = LinearLayoutManager(requireContext())
        rvTransaksi.adapter = adapter
    }

    private fun setupChartFilterTabs() {
        val tabs = listOf(tabChart7Hari, tabChart30Hari, tabChart2Bulan, tabChart6Bulan, tabChart1Tahun)
        val keys = listOf("7_hari", "30_hari", "2_bulan", "6_bulan", "1_tahun")
        tabs.forEachIndexed { i, tab ->
            tab.setOnClickListener {
                activeChartTab = keys[i]
                tabs.forEach { t ->
                    t.setBackgroundColor(Color.TRANSPARENT)
                    t.setTextColor(colorSecondary)
                }
                tab.setBackgroundColor(colorPrimary)
                tab.setTextColor(Color.WHITE)
                updateChartByTab()
            }
        }
    }

    private fun updateChartByTab() {
        val charts = lastResponse?.charts ?: return
        val points = when (activeChartTab) {
            "7_hari"  -> charts.daily.takeLast(7)
            "30_hari" -> charts.daily.takeLast(30)
            "2_bulan" -> charts.monthly.takeLast(2)
            "6_bulan" -> charts.monthly.takeLast(6)
            "1_tahun" -> charts.monthly.takeLast(12)
            else      -> charts.daily.takeLast(7)
        }
        setupRevenueChart(points)
    }

    private fun setupRevenueChart(points: List<ChartPoint>) {
        if (points.isEmpty()) return
        val entries = points.mapIndexed { i, p -> Entry(i.toFloat(), p.value.toFloat()) }
        val ds = LineDataSet(entries, "Revenue").apply {
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
        chartRevenue.apply {
            this.data = LineData(ds)
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
            legend.isEnabled      = false
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
        viewModel.loadKeuangan(userId = session.getUserId())
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
        tvHari.text      = formatRp(s.hari)
        tvMinggu.text    = formatRp(s.minggu)
        tvBulan.text     = formatRp(s.bulan)
        tvTahun.text     = formatRp(s.tahun)
        tvTransaksi.text = "${s.totalTransaksi} transaksi booking"
        tvRataRata.text  = formatRp(s.rataRata)
        val pct = s.pctChange
        tvPctChange.text = "${if (pct >= 0) "+" else ""}${pct}% vs periode lalu"
        tvPctChange.setTextColor(if (pct >= 0) colorPrimary else colorError)
        ivPctIcon.setImageResource(if (pct >= 0) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down)
        ivPctIcon.setColorFilter(if (pct >= 0) colorPrimary else colorError)
    }

    private fun bindTable(data: KeuanganResponse) {
        val page = data.transaksi ?: return
        allTransaksi = page.data.toMutableList()
        adapter.submitList(page.data)
        tvEmpty.visibility = if (page.data.isEmpty()) View.VISIBLE else View.GONE
    }

    // ─── Export ────────────────────────────────────────────────────────────────
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
            popupView,
            popupWidth,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
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

        // Dim background
        requireActivity().window.attributes =
            requireActivity().window.attributes.also { it.alpha = 0.6f }

        val tvFrom   = popupView.findViewById<TextView>(R.id.popupDateFrom)
        val tvTo     = popupView.findViewById<TextView>(R.id.popupDateTo)
        val spinner  = popupView.findViewById<Spinner>(R.id.spinnerJenisLaporan)
        val btnPdf   = popupView.findViewById<Button>(R.id.popupBtnPdf)
        val btnExcel = popupView.findViewById<Button>(R.id.popupBtnExcel)

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

        spinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            arrayOf("Laporan Booking", "Laporan Pendapatan", "Rekap Bulanan")
        )

        btnPdf.setOnClickListener   { popup.dismiss(); exportPdf() }
        btnExcel.setOnClickListener { popup.dismiss(); exportExcel() }

        // Center horizontal, tepat di bawah tombol Export
        requireView().post {
            val btnLoc = IntArray(2)
            btnExportLaporan.getLocationInWindow(btnLoc)
            val xPos = (screenW - popupWidth) / 2
            val yPos = btnLoc[1] + btnExportLaporan.height + (8 * density).toInt()
            popup.showAtLocation(requireView(), android.view.Gravity.NO_GRAVITY, xPos, yPos)
        }
    }

    // ─── Export PDF ────────────────────────────────────────────────────────────
    private fun exportPdf() {
        val data = lastResponse ?: run {
            Toast.makeText(requireContext(), "Data belum tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val dir  = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val name = "laporan_keuangan_${System.currentTimeMillis()}.pdf"
            val file = File(dir, name)
            val doc  = com.itextpdf.text.Document()
            com.itextpdf.text.pdf.PdfWriter.getInstance(doc, file.outputStream())
            doc.open()

            val green     = com.itextpdf.text.BaseColor(0, 168, 107)
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

            val cols      = arrayOf("ID","Penyewa","Lapangan","Tanggal","Waktu","Status","Nominal")
            val tbl       = com.itextpdf.text.pdf.PdfPTable(cols.size).apply {
                widthPercentage = 100f
                setWidths(floatArrayOf(0.5f,1.5f,1.5f,1f,1f,1f,1.2f))
            }
            val whiteFont = com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9f, com.itextpdf.text.Font.BOLD, com.itextpdf.text.BaseColor.WHITE)
            cols.forEach { col ->
                tbl.addCell(com.itextpdf.text.pdf.PdfPCell(com.itextpdf.text.Phrase(col, whiteFont)).apply { backgroundColor = green; paddingBottom = 6f })
            }
            allTransaksi.forEach { t ->
                listOf("#${t.id}", t.customerName, t.fieldName, t.playDate, "${t.startTime}-${t.endTime}", t.bookingStatus, formatRp(t.totalPrice)).forEach { cell ->
                    tbl.addCell(com.itextpdf.text.pdf.PdfPCell(com.itextpdf.text.Phrase(cell, bodyFont)).apply { paddingBottom = 4f })
                }
            }
            doc.add(tbl)
            doc.close()
            Toast.makeText(requireContext(), "PDF tersimpan: $name", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gagal export PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // ─── Export Excel ──────────────────────────────────────────────────────────
    private fun exportExcel() {
        val data = lastResponse ?: run {
            Toast.makeText(requireContext(), "Data belum tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val wb     = org.apache.poi.xssf.usermodel.XSSFWorkbook()
            val hStyle = wb.createCellStyle().apply {
                fillForegroundColor = org.apache.poi.ss.usermodel.IndexedColors.GREEN.index
                fillPattern = org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND
                setFont(wb.createFont().apply { bold = true; color = org.apache.poi.ss.usermodel.IndexedColors.WHITE.index })
            }

            val ws1 = wb.createSheet("Ringkasan")
            ws1.createRow(0).createCell(0).apply { setCellValue("Laporan Keuangan FindFutsall"); cellStyle = hStyle }
            ws1.createRow(1).createCell(0).setCellValue("Periode: ${data.period?.from} s/d ${data.period?.to}")
            ws1.createRow(3).also { r ->
                r.createCell(0).apply { setCellValue("Keterangan"); cellStyle = hStyle }
                r.createCell(1).apply { setCellValue("Nilai"); cellStyle = hStyle }
            }
            val s = data.summary
            if (s != null) {
                listOf(
                    "Pendapatan Hari Ini"   to s.hari,
                    "Pendapatan Minggu Ini" to s.minggu,
                    "Pendapatan Bulan Ini"  to s.bulan,
                    "Pendapatan Tahun Ini"  to s.tahun,
                    "Total Transaksi"       to s.totalTransaksi.toDouble(),
                    "Rata-rata per Booking" to s.rataRata
                ).forEachIndexed { i, (k, v) ->
                    ws1.createRow(4 + i).also { r ->
                        r.createCell(0).setCellValue(k)
                        r.createCell(1).setCellValue(v)
                    }
                }
            }
            ws1.autoSizeColumn(0); ws1.autoSizeColumn(1)

            val ws2 = wb.createSheet("Riwayat Booking")
            ws2.createRow(0).also { r ->
                listOf("ID","Penyewa","Lapangan","Tanggal","Waktu","Status","Nominal").forEachIndexed { i, h ->
                    r.createCell(i).apply { setCellValue(h); cellStyle = hStyle }
                }
            }
            allTransaksi.forEachIndexed { idx, t ->
                ws2.createRow(idx + 1).also { r ->
                    r.createCell(0).setCellValue(t.id.toDouble())
                    r.createCell(1).setCellValue(t.customerName)
                    r.createCell(2).setCellValue(t.fieldName)
                    r.createCell(3).setCellValue(t.playDate)
                    r.createCell(4).setCellValue("${t.startTime}-${t.endTime}")
                    r.createCell(5).setCellValue(t.bookingStatus)
                    r.createCell(6).setCellValue(t.totalPrice)
                }
            }
            for (i in 0..6) ws2.autoSizeColumn(i)

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

            val dir  = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val name = "laporan_keuangan_${System.currentTimeMillis()}.xlsx"
            File(dir, name).outputStream().use { wb.write(it) }
            wb.close()
            Toast.makeText(requireContext(), "Excel tersimpan: $name", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gagal export Excel: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun formatRp(amount: Double): String {
        val fmt = NumberFormat.getNumberInstance(Locale("id", "ID"))
        return "Rp ${fmt.format(amount.toLong())}"
    }
}