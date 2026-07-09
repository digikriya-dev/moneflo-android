package id.digikriya.moneflo

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import id.digikriya.moneflo.adapter.HistoryListItem
import id.digikriya.moneflo.adapter.TransactionAdapter
import id.digikriya.moneflo.database.DatabaseHelper
import id.digikriya.moneflo.helper.BottomNavHelper
import id.digikriya.moneflo.helper.DateGroupHelper
import id.digikriya.moneflo.model.Cashflow
import java.util.Calendar

class HistoryActivity : AppCompatActivity() {

    private lateinit var btnAvatar: View
    private lateinit var tvAvatarInitial: TextView
    private lateinit var ivAvatarPhoto: ImageView
    private lateinit var tvGreeting: TextView
    private lateinit var btnBell: ImageView
    private lateinit var etSearch: EditText
    private lateinit var spinnerBulan: Spinner
    private lateinit var spinnerTahun: Spinner
    private lateinit var chipGroupKategori: ChipGroup
    private lateinit var rvTransactions: RecyclerView
    private lateinit var llEmptyState: LinearLayout
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fabAdd: FloatingActionButton

    private lateinit var db: DatabaseHelper
    private lateinit var adapter: TransactionAdapter

    private var currentUserId: Long = -1L
    private var searchQuery: String = ""
    private var selectedKategori: String? = null // null = Semua
    private var selectedMonth: Int? = null  // null = semua bulan
    private var selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR)

    private val CHIP_ALL_ID = View.generateViewId()

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
        setupHeader()
        setupSearch()
        setupMonthYearFilter()
        setupCategoryChips()
        setupRecyclerView()
        BottomNavHelper.setup(bottomNav, R.id.nav_history, this, currentUserId)
        setupFab()
    }

    override fun onResume() {
        super.onResume()
        loadTransactions()
    }

    private fun initViews() {
        btnAvatar         = findViewById(R.id.btn_avatar)
        tvAvatarInitial   = findViewById(R.id.tv_avatar_initial)
        ivAvatarPhoto     = findViewById(R.id.iv_avatar_photo)
        tvGreeting        = findViewById(R.id.tv_greeting)
        btnBell           = findViewById(R.id.btn_bell)
        etSearch          = findViewById(R.id.et_search)
        spinnerBulan      = findViewById(R.id.spinner_bulan)
        spinnerTahun      = findViewById(R.id.spinner_tahun)
        chipGroupKategori = findViewById(R.id.chip_group_kategori)
        rvTransactions    = findViewById(R.id.rv_transactions)
        llEmptyState      = findViewById(R.id.ll_empty_state)
        bottomNav         = findViewById(R.id.bottom_nav)
        fabAdd            = findViewById(R.id.fab_add)
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

        btnBell.setOnClickListener {
            id.digikriya.moneflo.helper.NotificationPopupHelper.show(this, btnBell)
        }

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
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s?.toString()?.trim() ?: ""
                loadTransactions()
            }
        })
    }

    private fun setupMonthYearFilter() {
        // ---- Spinner Bulan ----
        val bulanAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bulanList)
        bulanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerBulan.adapter = bulanAdapter
        spinnerBulan.setSelection(0) // Default: Semua Bulan

        spinnerBulan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                (view as? TextView)?.setTextColor(getColor(R.color.text_primary))
                selectedMonth = if (pos == 0) null else pos // pos 1-12 = Jan-Des
                loadTransactions()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // ---- Spinner Tahun ----
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val tahunList = (currentYear downTo currentYear - 5).map { it.toString() }
        val tahunAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tahunList)
        tahunAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTahun.adapter = tahunAdapter
        spinnerTahun.setSelection(0) // Default: Tahun berjalan
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

    private fun setupCategoryChips() {
        val categories = db.getCategoriesByUser(currentUserId)

        val chipAll = Chip(this).apply {
            id = CHIP_ALL_ID
            text = "Semua"
            isCheckable = true
            isChecked = true
        }
        chipGroupKategori.addView(chipAll)

        categories.forEach { kategori ->
            val chip = Chip(this).apply {
                text = kategori.namaKategori
                isCheckable = true
                tag = kategori.namaKategori
            }
            chipGroupKategori.addView(chip)
        }

        chipGroupKategori.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull()
            selectedKategori = if (checkedId == null || checkedId == CHIP_ALL_ID) {
                null
            } else {
                group.findViewById<Chip>(checkedId)?.tag as? String
            }
            loadTransactions()
        }
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(this, emptyList()) { cashflow ->
            showTransactionOptions(cashflow)
        }
        rvTransactions.layoutManager = LinearLayoutManager(this)
        rvTransactions.adapter = adapter
    }

    private fun setupFab() {
        fabAdd.setOnClickListener {
            val intent = Intent(this, TransactionFormActivity::class.java).apply {
                putExtra("PREV_PAGE", "history")
                putExtra("USER_ID", currentUserId)
            }
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    private fun loadTransactions() {
        var list = db.getCashflows(userId = currentUserId, month = selectedMonth, year = selectedYear)

        if (searchQuery.isNotEmpty()) {
            list = list.filter { it.namaTransaksi.contains(searchQuery, ignoreCase = true) }
        }
        selectedKategori?.let { kategori ->
            list = list.filter { it.namaKategori == kategori }
        }

        if (list.isEmpty()) {
            rvTransactions.visibility = View.GONE
            llEmptyState.visibility   = View.VISIBLE
            adapter.updateData(emptyList())
            return
        }

        rvTransactions.visibility = View.VISIBLE
        llEmptyState.visibility   = View.GONE
        adapter.updateData(groupByDate(list))
    }

    /** List sudah terurut DESC by timestamp (dari getCashflows) — kelompokkan jadi header + item. */
    private fun groupByDate(list: List<Cashflow>): List<HistoryListItem> {
        val result = mutableListOf<HistoryListItem>()
        var lastLabel: String? = null
        list.forEach { cashflow ->
            val label = DateGroupHelper.label(cashflow.timestamp)
            if (label != lastLabel) {
                result.add(HistoryListItem.Header(label))
                lastLabel = label
            }
            result.add(HistoryListItem.Item(cashflow))
        }
        return result
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
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
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
