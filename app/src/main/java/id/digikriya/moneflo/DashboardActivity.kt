package id.digikriya.moneflo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import id.digikriya.moneflo.database.DatabaseHelper
import id.digikriya.moneflo.helper.BottomNavHelper
import id.digikriya.moneflo.model.Cashflow
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    // Views
    private lateinit var tvGreeting: TextView
    private lateinit var tvSaldo: TextView
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpense: TextView
    private lateinit var pieChart: PieChart
    private lateinit var tvChartEmpty: TextView
    private lateinit var llRecentTransactions: LinearLayout
    private lateinit var llEmptyState: LinearLayout
    private lateinit var tvLihatSemua: TextView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var btnAvatar: View
    private lateinit var tvAvatarInitial: TextView
    private lateinit var ivAvatarPhoto: android.widget.ImageView
    private lateinit var btnBell: View

    private lateinit var db: DatabaseHelper
    private var currentUserId: Long = -1L

    private val rupiahFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        db = DatabaseHelper(this)

        // Coba dari SharedPreferences dulu
        val prefs = getSharedPreferences("moneflo_prefs", MODE_PRIVATE)
        currentUserId = prefs.getLong("current_user_id", -1L)

        // Fallback ke session DB
        if (currentUserId == -1L) {
            val session = db.getActiveSession()
            if (session == null) {
                goToLogin()
                return
            }
            currentUserId = session.userId
        }

        // Backfill kategori default baru untuk user lama yang sudah terdaftar
        // sebelum daftar kategori ini diperluas (aman dipanggil berkali-kali).
        db.seedDefaultCategories(currentUserId)

        initViews()
        setupHeader()
        BottomNavHelper.setup(bottomNav, R.id.nav_dashboard, this, currentUserId)
        setupListeners()
        loadDashboardData()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data setiap kali kembali ke dashboard (setelah tambah/edit transaksi)
        if (currentUserId != -1L) loadDashboardData()
    }

    private fun initViews() {
        tvGreeting           = findViewById(R.id.tv_greeting)
        tvSaldo              = findViewById(R.id.tv_saldo)
        tvTotalIncome        = findViewById(R.id.tv_total_income)
        tvTotalExpense       = findViewById(R.id.tv_total_expense)
        pieChart             = findViewById(R.id.pie_chart)
        tvChartEmpty         = findViewById(R.id.tv_chart_empty)
        llRecentTransactions = findViewById(R.id.ll_recent_transactions)
        llEmptyState         = findViewById(R.id.ll_empty_state)
        tvLihatSemua         = findViewById(R.id.tv_lihat_semua)
        bottomNav            = findViewById(R.id.bottom_nav)
        fabAdd               = findViewById(R.id.fab_add)
        btnAvatar            = findViewById(R.id.btn_avatar)
        tvAvatarInitial      = findViewById(R.id.tv_avatar_initial)
        ivAvatarPhoto        = findViewById(R.id.iv_avatar_photo)
        btnBell              = findViewById(R.id.btn_bell)
    }

    private fun setupHeader() {
        val user = db.getUserById(currentUserId)
        tvAvatarInitial.text = user?.namaLengkap?.firstOrNull()?.uppercase() ?: "?"
        if (id.digikriya.moneflo.helper.PhotoHelper.loadCircular(ivAvatarPhoto, user?.fotoProfil)) {
            ivAvatarPhoto.visibility = View.VISIBLE
            tvAvatarInitial.visibility = View.GONE
        }
        val firstName = user?.namaLengkap?.substringBefore(" ")?.ifEmpty { null } ?: user?.username
        tvGreeting.text = "Hi, ${firstName ?: "Pengguna"}!"
    }

    private fun setupListeners() {
        // FAB tambah transaksi
        fabAdd.setOnClickListener {
            val intent = Intent(this, TransactionFormActivity::class.java)
            intent.putExtra("PREV_PAGE", "dashboard")
            intent.putExtra("USER_ID", currentUserId)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        // Lihat semua → ke Riwayat
        tvLihatSemua.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java).apply {
                putExtra("USER_ID", currentUserId)
            })
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        // Notifikasi
        btnBell.setOnClickListener {
            id.digikriya.moneflo.helper.NotificationPopupHelper.show(this, btnBell)
        }

        // Menu profil singkat (jalan pintas selain tab Profil)
        btnAvatar.setOnClickListener {
            id.digikriya.moneflo.helper.ProfileMenuPopupHelper.show(
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
                goToLogin()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // =========================================================
    // LOAD DATA
    // =========================================================

    private fun loadDashboardData() {
        loadSaldo()
        loadPieChart()
        loadRecentTransactions()
    }

    private fun loadSaldo() {
        val saldo = db.getSaldo(currentUserId)
        val cashflows = db.getCashflows(currentUserId)

        val totalIncome  = cashflows.filter { it.jenisTransaksi == "income" }.sumOf { it.nominal }
        val totalExpense = cashflows.filter { it.jenisTransaksi == "expense" }.sumOf { it.nominal }

        tvSaldo.text       = formatRupiah(saldo)
        tvSaldo.setTextColor(
            getColor(if (saldo >= 0) R.color.on_gradient_primary else R.color.expense_red)
        )
        tvTotalIncome.text  = formatRupiah(totalIncome)
        tvTotalExpense.text = formatRupiah(totalExpense)
    }

    private fun loadPieChart() {
        val cal   = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH) + 1
        val year  = cal.get(Calendar.YEAR)

        val summary = db.getExpenseSummaryByCategory(currentUserId, month, year)

        if (summary.isEmpty()) {
            pieChart.visibility   = View.GONE
            tvChartEmpty.visibility = View.VISIBLE
            return
        }

        pieChart.visibility     = View.VISIBLE
        tvChartEmpty.visibility = View.GONE

        val entries = summary.map { (kategori, total) ->
            PieEntry(total.toFloat(), kategori)
        }
        val chartColors = summary.keys.mapIndexed { index, _ ->
            id.digikriya.moneflo.helper.CategoryVisuals.colorForIndex(index)
        }

        val dataSet = PieDataSet(entries, "").apply {
            colors = chartColors.toMutableList()
            valueTextColor  = 0xFFFFFFFF.toInt()
            valueTextSize   = 11f
            valueFormatter  = PercentFormatter(pieChart)
            sliceSpace      = 2f
        }

        val holeColor = getColor(R.color.card_bg)
        val labelColor = getColor(R.color.text_secondary)

        pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled   = false
            isDrawHoleEnabled       = true
            holeRadius              = 48f
            transparentCircleRadius = 52f
            setHoleColor(holeColor)
            setTransparentCircleColor(holeColor)
            setUsePercentValues(true)
            legend.apply {
                isEnabled   = true
                textColor   = labelColor
                textSize    = 11f
                formSize    = 10f
            }
            setEntryLabelColor(0xFFFFFFFF.toInt())
            setEntryLabelTextSize(10f)
            setCenterText("Pengeluaran")
            setCenterTextColor(labelColor)
            setCenterTextSize(12f)
            animateY(800)
            invalidate()
        }
    }

    private fun loadRecentTransactions() {
        val recentList = db.getRecentCashflows(currentUserId, 5)
        llRecentTransactions.removeAllViews()

        if (recentList.isEmpty()) {
            llEmptyState.visibility         = View.VISIBLE
            llRecentTransactions.visibility = View.GONE
            tvLihatSemua.visibility         = View.GONE
            return
        }

        llEmptyState.visibility         = View.GONE
        llRecentTransactions.visibility = View.VISIBLE
        tvLihatSemua.visibility         = View.VISIBLE

        recentList.forEach { cashflow ->
            val itemView = layoutInflater.inflate(
                R.layout.item_recent_transaction,
                llRecentTransactions,
                false
            )
            bindTransactionItem(itemView, cashflow)
            itemView.setOnClickListener {
                openEditTransaction(cashflow)
            }
            llRecentTransactions.addView(itemView)
        }
    }

    private fun bindTransactionItem(view: View, cashflow: Cashflow) {
        val tvNama      = view.findViewById<TextView>(R.id.tv_nama_transaksi)
        val tvKategori  = view.findViewById<TextView>(R.id.tv_kategori)
        val tvNominal   = view.findViewById<TextView>(R.id.tv_nominal)
        val tvTanggal   = view.findViewById<TextView>(R.id.tv_tanggal)
        val ivIcon      = view.findViewById<android.widget.ImageView>(R.id.iv_kategori_icon)
        val viewIconBg  = view.findViewById<View>(R.id.view_icon_bg)

        tvNama.text     = cashflow.namaTransaksi
        tvKategori.text = cashflow.namaKategori
        ivIcon.setImageResource(id.digikriya.moneflo.helper.CategoryVisuals.iconFor(cashflow.namaKategori))
        viewIconBg.backgroundTintList = android.content.res.ColorStateList.valueOf(
            id.digikriya.moneflo.helper.CategoryVisuals.colorFor(cashflow.namaKategori)
        )

        val isIncome = cashflow.jenisTransaksi == "income"
        val nominalText = "${if (isIncome) "+" else "-"}${formatRupiah(cashflow.nominal)}"
        tvNominal.text      = nominalText
        tvNominal.setTextColor(getColor(if (isIncome) R.color.income_green else R.color.expense_red))

        // Format tanggal
        try {
            val sdf    = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date   = sdf.parse(cashflow.timestamp)
            val outSdf = SimpleDateFormat("dd MMM yyyy", Locale("id"))
            tvTanggal.text = if (date != null) outSdf.format(date) else cashflow.timestamp
        } catch (e: Exception) {
            tvTanggal.text = cashflow.timestamp
        }
    }

    private fun openEditTransaction(cashflow: Cashflow) {
        val intent = Intent(this, TransactionFormActivity::class.java).apply {
            putExtra("PREV_PAGE", "dashboard")
            putExtra("USER_ID", currentUserId)
            putExtra("CASHFLOW_ID", cashflow.id)
            putExtra("IS_EDIT", true)
        }
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    // =========================================================
    // HELPER
    // =========================================================

    private fun formatRupiah(amount: Double): String {
        return "Rp${String.format("%,.0f", amount).replace(",", ".")}"
    }
}
