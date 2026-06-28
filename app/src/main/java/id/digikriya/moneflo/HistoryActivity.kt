package id.digikriya.moneflo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import id.digikriya.moneflo.adapter.TransactionAdapter
import id.digikriya.moneflo.database.DatabaseHelper
import id.digikriya.moneflo.model.Cashflow
import java.util.Calendar

class HistoryActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var spinnerBulan: Spinner
    private lateinit var spinnerTahun: Spinner
    private lateinit var rvTransactions: RecyclerView
    private lateinit var llEmptyState: LinearLayout
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fabAdd: FloatingActionButton

    private lateinit var db: DatabaseHelper
    private lateinit var adapter: TransactionAdapter

    private var currentUserId: Long = -1L
    private var selectedMonth: Int? = null  // null = semua bulan
    private var selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR)

    private val bulanList = listOf(
        "Semua Bulan", "Januari", "Februari", "Maret", "April",
        "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        currentUserId = intent.getLongExtra("USER_ID", -1L)
        if (currentUserId == -1L) {
            finish(); return
        }

        db = DatabaseHelper(this)

        initViews()
        setupToolbar()
        setupSpinners()
        setupRecyclerView()
        setupBottomNav()
        setupFab()
    }

    override fun onResume() {
        super.onResume()
        loadTransactions()
    }

    private fun initViews() {
        toolbar        = findViewById(R.id.toolbar)
        spinnerBulan   = findViewById(R.id.spinner_bulan)
        spinnerTahun   = findViewById(R.id.spinner_tahun)
        rvTransactions = findViewById(R.id.rv_transactions)
        llEmptyState   = findViewById(R.id.ll_empty_state)
        bottomNav      = findViewById(R.id.bottom_nav)
        fabAdd         = findViewById(R.id.fab_add)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupSpinners() {
        // ---- Spinner Bulan ----
        val bulanAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bulanList)
        bulanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerBulan.adapter = bulanAdapter
        // Default: Semua (index 0)
        spinnerBulan.setSelection(0)

        spinnerBulan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                (view as? TextView)?.setTextColor(getColor(R.color.text_primary))
                selectedMonth = if (pos == 0) null else pos   // pos 1-12 = Jan-Des
                loadTransactions()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // ---- Spinner Tahun ----
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val tahunList   = (currentYear downTo currentYear - 5).map { it.toString() }
        val tahunAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tahunList)
        tahunAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTahun.adapter = tahunAdapter
        // Default: tahun berjalan
        spinnerTahun.setSelection(0)
        selectedYear = currentYear

        spinnerTahun.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                (view as? TextView)?.setTextColor(getColor(R.color.text_primary))
                selectedYear = tahunList[pos].toInt()
                loadTransactions()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(this, emptyList()) { cashflow ->
            showTransactionOptions(cashflow)
        }
        rvTransactions.layoutManager = LinearLayoutManager(this)
        rvTransactions.adapter = adapter
    }

    private fun setupBottomNav() {
        bottomNav.selectedItemId = R.id.nav_history
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    finish()
                    true
                }
                R.id.nav_history -> true
                else -> false
            }
        }
    }

    private fun setupFab() {
        fabAdd.setOnClickListener {
            val intent = Intent(this, TransactionFormActivity::class.java).apply {
                putExtra("PREV_PAGE", "history")
                putExtra("USER_ID", currentUserId)
            }
            startActivity(intent)
        }
    }

    private fun loadTransactions() {
        val list = db.getCashflows(
            userId = currentUserId,
            month  = selectedMonth,
            year   = selectedYear
        )
        adapter.updateData(list)

        if (list.isEmpty()) {
            rvTransactions.visibility = View.GONE
            llEmptyState.visibility   = View.VISIBLE
        } else {
            rvTransactions.visibility = View.VISIBLE
            llEmptyState.visibility   = View.GONE
        }
    }

    private fun showTransactionOptions(cashflow: Cashflow) {
        val options = arrayOf("Edit Transaksi", "Hapus Transaksi")
        AlertDialog.Builder(this)
            .setTitle(cashflow.namaTransaksi)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openEditTransaction(cashflow)
                    1 -> confirmDeleteTransaction(cashflow)
                }
            }
            .show()
    }

    private fun openEditTransaction(cashflow: Cashflow) {
        val intent = Intent(this, TransactionFormActivity::class.java).apply {
            putExtra("PREV_PAGE", "history")
            putExtra("USER_ID", currentUserId)
            putExtra("CASHFLOW_ID", cashflow.id)
            putExtra("IS_EDIT", true)
        }
        startActivity(intent)
    }

    private fun confirmDeleteTransaction(cashflow: Cashflow) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Transaksi")
            .setMessage("Yakin ingin menghapus \"${cashflow.namaTransaksi}\"?")
            .setPositiveButton("Hapus") { _, _ ->
                db.deleteCashflow(cashflow.id, currentUserId)
                loadTransactions()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}