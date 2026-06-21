package id.digikriya.moneflo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import id.digikriya.moneflo.database.DatabaseHelper
import id.digikriya.moneflo.model.Cashflow
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    // Views
    private lateinit var toolbar: Toolbar
    private lateinit var tvGreeting: TextView
    private lateinit var tvUsername: TextView
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
    private lateinit var btnProfileMenu: View
    private lateinit var popupProfileMenu: View
    private lateinit var menuEditProfil: TextView
    private lateinit var menuUbahPassword: TextView
    private lateinit var menuLogout: TextView

    private lateinit var db: DatabaseHelper
    private var currentUserId: Long = -1L

    private val rupiahFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        db = DatabaseHelper(this)

        // Ambil user dari session aktif
        val session = db.getActiveSession()
        if (session == null) {
            goToLogin()
            return
        }
        currentUserId = session.userId

        initViews()
        setupToolbar()
        setupBottomNav()
        setupListeners()
        loadDashboardData()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data setiap kali kembali ke dashboard (setelah tambah/edit transaksi)
        if (currentUserId != -1L) loadDashboardData()
    }

    private fun initViews() {
        toolbar              = findViewById(R.id.toolbar)
        tvGreeting           = findViewById(R.id.tv_greeting)
        tvUsername           = findViewById(R.id.tv_username)
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
        btnProfileMenu       = findViewById(R.id.btn_profile_menu)
        popupProfileMenu     = findViewById(R.id.popup_profile_menu)
        menuEditProfil       = findViewById(R.id.menu_edit_profil)
        menuUbahPassword     = findViewById(R.id.menu_ubah_password)
        menuLogout           = findViewById(R.id.menu_logout)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val user = db.getUserById(currentUserId)
        tvUsername.text = user?.namaLengkap ?: user?.username ?: "-"

        // Greeting sesuai waktu
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        tvGreeting.text = when {
            hour < 12 -> "Selamat pagi,"
            hour < 15 -> "Selamat siang,"
            hour < 18 -> "Selamat sore,"
            else      -> "Selamat malam,"
        }
    }

    private fun setupBottomNav() {
        bottomNav.selectedItemId = R.id.nav_dashboard
        // Sembunyikan label item placeholder tengah
        bottomNav.menu.findItem(R.id.nav_placeholder)?.isEnabled = false
    }

    private fun setupListeners() {
        // FAB tambah transaksi
        fabAdd.setOnClickListener {
            val intent = Intent(this, TransactionFormActivity::class.java)
            intent.putExtra("PREV_PAGE", "dashboard")
            intent.putExtra("USER_ID", currentUserId)
            startActivity(intent)
        }

        // Lihat semua → ke Riwayat
        tvLihatSemua.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java).apply {
                putExtra("USER_ID", currentUserId)
            })
        }

        // Bottom nav
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> true
                R.id.nav_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java).apply {
                        putExtra("USER_ID", currentUserId)
                    })
                    true
                }
                else -> false
            }
        }

        // Popup menu profil
        btnProfileMenu.setOnClickListener {
            val isVisible = popupProfileMenu.visibility == View.VISIBLE
            popupProfileMenu.visibility = if (isVisible) View.GONE else View.VISIBLE
        }

        menuEditProfil.setOnClickListener {
            popupProfileMenu.visibility = View.GONE
            startActivity(Intent(this, ProfileActivity::class.java).apply {
                putExtra("USER_ID", currentUserId)
                putExtra("ACTION", "edit_profil")
            })
        }

        menuUbahPassword.setOnClickListener {
            popupProfileMenu.visibility = View.GONE
            startActivity(Intent(this, ProfileActivity::class.java).apply {
                putExtra("USER_ID", currentUserId)
                putExtra("ACTION", "ubah_password")
            })
        }

        menuLogout.setOnClickListener {
            popupProfileMenu.visibility = View.GONE
            showLogoutConfirmation()
        }

        // Tutup popup saat klik area lain
        findViewById<View>(R.id.root_scroll)?.setOnClickListener {
            popupProfileMenu.visibility = View.GONE
        }
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
            getColor(if (saldo >= 0) R.color.text_primary else R.color.expense_red)
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

        val chartColors = listOf(
            0xFF00D4E8.toInt(), // cyan
            0xFF1A6EFA.toInt(), // blue
            0xFF3DFF8A.toInt(), // green
            0xFFFF4D6A.toInt(), // red
            0xFFFFB84D.toInt(), // orange
            0xFFBB86FC.toInt(), // purple
            0xFFFF6BBA.toInt(), // pink
        )

        val dataSet = PieDataSet(entries, "").apply {
            colors = chartColors.take(entries.size).toMutableList()
            valueTextColor  = 0xFFFFFFFF.toInt()
            valueTextSize   = 11f
            valueFormatter  = PercentFormatter(pieChart)
            sliceSpace      = 2f
        }

        pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled   = false
            isDrawHoleEnabled       = true
            holeRadius              = 48f
            transparentCircleRadius = 52f
            setHoleColor(0xFF161B22.toInt())
            setTransparentCircleColor(0xFF161B22.toInt())
            setUsePercentValues(true)
            legend.apply {
                isEnabled   = true
                textColor   = 0xFF8B949E.toInt()
                textSize    = 11f
                formSize    = 10f
            }
            setEntryLabelColor(0xFFFFFFFF.toInt())
            setEntryLabelTextSize(10f)
            setCenterText("Pengeluaran")
            setCenterTextColor(0xFF8B949E.toInt())
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
        val tvInitial   = view.findViewById<TextView>(R.id.tv_kategori_initial)

        tvNama.text     = cashflow.namaTransaksi
        tvKategori.text = cashflow.namaKategori
        tvInitial.text  = cashflow.namaKategori.firstOrNull()?.uppercase() ?: "?"

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
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Yakin ingin keluar dari akun?")
            .setPositiveButton("Logout") { _, _ ->
                db.logout()
                goToLogin()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // =========================================================
    // HELPER
    // =========================================================

    private fun formatRupiah(amount: Double): String {
        return "Rp${String.format("%,.0f", amount).replace(",", ".")}"
    }
}
