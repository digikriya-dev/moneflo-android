package id.digikriya.moneflo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import id.digikriya.moneflo.database.DatabaseHelper
import id.digikriya.moneflo.helper.BottomNavHelper
import id.digikriya.moneflo.helper.CategoryVisuals
import id.digikriya.moneflo.helper.NotificationPopupHelper
import id.digikriya.moneflo.helper.PhotoHelper
import id.digikriya.moneflo.helper.ProfileMenuPopupHelper
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StatisticsActivity : AppCompatActivity() {

    private lateinit var btnAvatar: View
    private lateinit var tvAvatarInitial: TextView
    private lateinit var ivAvatarPhoto: ImageView
    private lateinit var tvGreeting: TextView
    private lateinit var btnBell: ImageView
    private lateinit var segWeek: TextView
    private lateinit var segMonth: TextView
    private lateinit var segYear: TextView
    private lateinit var pieChart: PieChart
    private lateinit var tvChartEmpty: TextView
    private lateinit var llLegend: LinearLayout
    private lateinit var tvHighestCategory: TextView
    private lateinit var tvHighestNote: TextView
    private lateinit var viewHighestIconBg: View
    private lateinit var ivHighestIcon: ImageView
    private lateinit var tvDailyAvg: TextView
    private lateinit var tvSmartTip: TextView
    private lateinit var llDetails: LinearLayout
    private lateinit var tvDetailsEmpty: TextView
    private lateinit var bottomNav: com.google.android.material.bottomnavigation.BottomNavigationView

    private lateinit var db: DatabaseHelper
    private var currentUserId: Long = -1L
    private var period: String = PERIOD_WEEK

    private val rupiahFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    companion object {
        private const val PERIOD_WEEK = "week"
        private const val PERIOD_MONTH = "month"
        private const val PERIOD_YEAR = "year"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        db = DatabaseHelper(this)

        currentUserId = intent.getLongExtra("USER_ID", -1L)
        if (currentUserId == -1L) {
            currentUserId = getSharedPreferences("moneflo_prefs", MODE_PRIVATE).getLong("current_user_id", -1L)
        }
        if (currentUserId == -1L) { finish(); return }

        initViews()
        setupHeader()
        setupSegmentControl()
        BottomNavHelper.setup(bottomNav, R.id.nav_stats, this, currentUserId)

        selectPeriod(PERIOD_WEEK)
    }

    override fun onResume() {
        super.onResume()
        loadStatistics()
    }

    private fun initViews() {
        btnAvatar = findViewById(R.id.btn_avatar)
        tvAvatarInitial = findViewById(R.id.tv_avatar_initial)
        ivAvatarPhoto = findViewById(R.id.iv_avatar_photo)
        tvGreeting = findViewById(R.id.tv_greeting)
        btnBell = findViewById(R.id.btn_bell)
        segWeek = findViewById(R.id.seg_week)
        segMonth = findViewById(R.id.seg_month)
        segYear = findViewById(R.id.seg_year)
        pieChart = findViewById(R.id.pie_chart)
        tvChartEmpty = findViewById(R.id.tv_chart_empty)
        llLegend = findViewById(R.id.ll_legend)
        tvHighestCategory = findViewById(R.id.tv_highest_category)
        tvHighestNote = findViewById(R.id.tv_highest_note)
        viewHighestIconBg = findViewById(R.id.view_highest_icon_bg)
        ivHighestIcon = findViewById(R.id.iv_highest_icon)
        tvDailyAvg = findViewById(R.id.tv_daily_avg)
        tvSmartTip = findViewById(R.id.tv_smart_tip)
        llDetails = findViewById(R.id.ll_details)
        tvDetailsEmpty = findViewById(R.id.tv_details_empty)
        bottomNav = findViewById(R.id.bottom_nav)
    }

    private fun setupHeader() {
        val user = db.getUserById(currentUserId)
        tvAvatarInitial.text = user?.namaLengkap?.firstOrNull()?.uppercase() ?: "?"
        if (PhotoHelper.loadCircular(ivAvatarPhoto, user?.fotoProfil)) {
            ivAvatarPhoto.visibility = View.VISIBLE
            tvAvatarInitial.visibility = View.GONE
        }
        val firstName = user?.namaLengkap?.substringBefore(" ")?.ifEmpty { null } ?: user?.username
        tvGreeting.text = "Hi, ${firstName ?: "Pengguna"}!"

        btnBell.setOnClickListener {
            NotificationPopupHelper.show(this, btnBell)
        }

        btnAvatar.setOnClickListener {
            ProfileMenuPopupHelper.show(
                context = this,
                anchor = btnAvatar,
                onEditProfil = { openEditProfil() },
                onUbahPassword = { openUbahPassword() },
                onLogout = { showLogoutConfirmation() }
            )
        }
    }

    private fun openEditProfil() {
        startActivity(Intent(this, EditProfileActivity::class.java).apply {
            putExtra("USER_ID", currentUserId)
        })
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun openUbahPassword() {
        startActivity(Intent(this, ChangePasswordActivity::class.java).apply {
            putExtra("USER_ID", currentUserId)
        })
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Yakin ingin keluar dari akun?")
            .setPositiveButton("Logout") { _, _ ->
                db.logout()
                getSharedPreferences("moneflo_prefs", MODE_PRIVATE)
                    .edit().remove("current_user_id").apply()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun setupSegmentControl() {
        segWeek.setOnClickListener { selectPeriod(PERIOD_WEEK) }
        segMonth.setOnClickListener { selectPeriod(PERIOD_MONTH) }
        segYear.setOnClickListener { selectPeriod(PERIOD_YEAR) }
    }

    private fun selectPeriod(newPeriod: String) {
        period = newPeriod
        val selectedBg = ContextCompat.getDrawable(this, R.drawable.bg_segment_selected)
        val selectedColor = ContextCompat.getColor(this, R.color.segment_selected_text)
        val unselectedColor = ContextCompat.getColor(this, R.color.text_secondary)

        listOf(segWeek to PERIOD_WEEK, segMonth to PERIOD_MONTH, segYear to PERIOD_YEAR).forEach { (tv, id) ->
            if (id == newPeriod) {
                tv.background = selectedBg
                tv.setTextColor(selectedColor)
            } else {
                tv.background = null
                tv.setTextColor(unselectedColor)
            }
        }
        loadStatistics()
    }

    /** Rentang tanggal [start, end] (yyyy-MM-dd) untuk periode terpilih. */
    private fun currentRange(): Pair<String, String> {
        val cal = Calendar.getInstance()
        val end = sdf.format(cal.time)
        return when (period) {
            PERIOD_WEEK -> {
                cal.add(Calendar.DAY_OF_YEAR, -6)
                sdf.format(cal.time) to end
            }
            PERIOD_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val start = sdf.format(cal.time)
                start to end
            }
            else -> { // PERIOD_YEAR
                cal.set(Calendar.DAY_OF_YEAR, 1)
                val start = sdf.format(cal.time)
                start to end
            }
        }
    }

    private fun periodDayCount(): Int {
        return when (period) {
            PERIOD_WEEK -> 7
            PERIOD_MONTH -> Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
            else -> Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_YEAR)
        }
    }

    private fun loadStatistics() {
        val (start, end) = currentRange()
        val summary = db.getExpenseSummaryByDateRange(currentUserId, start, end)
        val counts = db.getExpenseCountByCategory(currentUserId, start, end)

        renderPieChart(summary)
        renderHighestCategory(summary)
        renderDailyAvg(summary)
        renderSmartTip(summary)
        renderDetails(summary, counts)
    }

    private fun renderPieChart(summary: Map<String, Double>) {
        llLegend.removeAllViews()

        if (summary.isEmpty()) {
            pieChart.visibility = View.GONE
            tvChartEmpty.visibility = View.VISIBLE
            return
        }
        pieChart.visibility = View.VISIBLE
        tvChartEmpty.visibility = View.GONE

        val total = summary.values.sum()
        val entries = summary.map { (kategori, nominal) -> PieEntry(nominal.toFloat(), kategori) }
        val colors = summary.keys.mapIndexed { index, _ -> CategoryVisuals.colorForIndex(index) }

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            setDrawValues(false)
            sliceSpace = 2f
        }

        val onSurface = ContextCompat.getColor(this, R.color.text_primary)
        val onSurfaceVariant = ContextCompat.getColor(this, R.color.text_secondary)
        val surfaceContainer = ContextCompat.getColor(this, R.color.card_bg)

        pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 78f
            transparentCircleRadius = 80f
            setHoleColor(surfaceContainer)
            setUsePercentValues(false)
            setDrawEntryLabels(false)
            setTouchEnabled(false)
            setCenterTextColor(onSurface)
            setCenterTextSize(15f)
            centerText = "Total Pengeluaran\n${formatRupiah(total)}"
            animateY(600)
            invalidate()
        }

        summary.keys.forEachIndexed { index, kategori ->
            val dotColor = CategoryVisuals.colorForIndex(index)
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(0, 0, 20, 0)
            }
            val dot = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(10, 10).apply { marginEnd = 6 }
                background = ContextCompat.getDrawable(this@StatisticsActivity, R.drawable.bg_circle_solid)
                backgroundTintList = android.content.res.ColorStateList.valueOf(dotColor)
            }
            val label = TextView(this).apply {
                text = kategori
                setTextColor(onSurfaceVariant)
                textSize = 12f
            }
            row.addView(dot)
            row.addView(label)
            llLegend.addView(row)
        }
    }

    private fun renderHighestCategory(summary: Map<String, Double>) {
        val top = summary.entries.maxByOrNull { it.value }
        if (top == null) {
            tvHighestCategory.text = "-"
            tvHighestNote.text = "Belum ada data pengeluaran."
            ivHighestIcon.setImageResource(R.drawable.ic_cat_default)
            viewHighestIconBg.backgroundTintList = android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.outline_variant)
            )
            return
        }
        tvHighestCategory.text = top.key
        tvHighestNote.text = "${formatRupiah(top.value)} pada periode ini"
        ivHighestIcon.setImageResource(CategoryVisuals.iconFor(top.key))
        viewHighestIconBg.backgroundTintList = android.content.res.ColorStateList.valueOf(CategoryVisuals.colorFor(top.key))
    }

    private fun renderDailyAvg(summary: Map<String, Double>) {
        val total = summary.values.sum()
        val avg = total / periodDayCount()
        tvDailyAvg.text = formatRupiah(avg)
    }

    private fun renderSmartTip(summary: Map<String, Double>) {
        val top = summary.entries.maxByOrNull { it.value }
        tvSmartTip.text = if (top != null) {
            "Pengeluaran untuk \"${top.key}\" paling besar periode ini. Coba tetapkan batas anggaran untuk kategori ini agar lebih terkontrol."
        } else {
            "Belum ada pengeluaran tercatat pada periode ini. Yuk mulai catat transaksimu!"
        }
    }

    private fun renderDetails(summary: Map<String, Double>, counts: Map<String, Int>) {
        llDetails.removeAllViews()

        if (summary.isEmpty()) {
            tvDetailsEmpty.visibility = View.VISIBLE
            return
        }
        tvDetailsEmpty.visibility = View.GONE

        val maxValue = summary.values.maxOrNull() ?: 1.0
        val sorted = summary.entries.sortedByDescending { it.value }

        sorted.forEach { (kategori, total) ->
            val itemView = layoutInflater.inflate(R.layout.item_category_detail, llDetails, false)

            val ivIcon = itemView.findViewById<ImageView>(R.id.iv_icon)
            val viewIconBg = itemView.findViewById<View>(R.id.view_icon_bg)
            val tvNama = itemView.findViewById<TextView>(R.id.tv_nama_kategori)
            val tvJumlah = itemView.findViewById<TextView>(R.id.tv_jumlah_transaksi)
            val tvNominal = itemView.findViewById<TextView>(R.id.tv_nominal)
            val progress = itemView.findViewById<ProgressBar>(R.id.progress_kategori)

            ivIcon.setImageResource(CategoryVisuals.iconFor(kategori))
            viewIconBg.backgroundTintList = android.content.res.ColorStateList.valueOf(CategoryVisuals.colorFor(kategori))
            tvNama.text = kategori
            val jumlah = counts[kategori] ?: 0
            tvJumlah.text = "$jumlah transaksi"
            tvNominal.text = formatRupiah(total)
            progress.progress = ((total / maxValue) * 100).toInt()

            llDetails.addView(itemView)
        }
    }

    private fun formatRupiah(amount: Double): String {
        return "Rp${String.format("%,.0f", amount).replace(",", ".")}"
    }
}
