package id.digikriya.moneflo

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import id.digikriya.moneflo.database.DatabaseHelper
import id.digikriya.moneflo.model.Cashflow
import id.digikriya.moneflo.model.Category
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TransactionFormActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var tilNama: TextInputLayout
    private lateinit var etNama: TextInputEditText
    private lateinit var rgJenis: RadioGroup
    private lateinit var rbPemasukan: RadioButton
    private lateinit var rbPengeluaran: RadioButton
    private lateinit var tilNominal: TextInputLayout
    private lateinit var etNominal: TextInputEditText
    private lateinit var spinnerKategori: Spinner
    private lateinit var tvErrorKategori: TextView
    private lateinit var etTanggal: EditText
    private lateinit var etWaktu: EditText
    private lateinit var etCatatan: EditText
    private lateinit var btnSimpan: View

    private lateinit var db: DatabaseHelper

    private var currentUserId: Long   = -1L
    private var cashflowId: Long      = -1L
    private var isEdit: Boolean       = false
    private var prevPage: String      = "dashboard"

    private var kategorList: MutableList<Category> = mutableListOf()
    private var selectedKategoriId: Long = -1L

    // Untuk tambah kategori baru di spinner
    private val TAMBAH_KATEGORI_LABEL = "+ Tambah Kategori Baru..."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_form)

        currentUserId = intent.getLongExtra("USER_ID", -1L)
        cashflowId    = intent.getLongExtra("CASHFLOW_ID", -1L)
        isEdit        = intent.getBooleanExtra("IS_EDIT", false)
        prevPage      = intent.getStringExtra("PREV_PAGE") ?: "dashboard"

        if (currentUserId == -1L) { finish(); return }

        db = DatabaseHelper(this)

        initViews()
        setupToolbar()
        setupKategoriSpinner()
        setupDateTimePickers()
        setupSaveButton()

        if (isEdit && cashflowId != -1L) {
            loadExistingData()
        } else {
            // Default tanggal hari ini
            setDefaultDate()
        }
    }

    private fun initViews() {
        toolbar        = findViewById(R.id.toolbar)
        tilNama        = findViewById(R.id.til_nama_transaksi)
        etNama         = findViewById(R.id.et_nama_transaksi)
        rgJenis        = findViewById(R.id.rg_jenis_transaksi)
        rbPemasukan    = findViewById(R.id.rb_pemasukan)
        rbPengeluaran  = findViewById(R.id.rb_pengeluaran)
        tilNominal     = findViewById(R.id.til_nominal)
        etNominal      = findViewById(R.id.et_nominal)
        spinnerKategori = findViewById(R.id.spinner_kategori)
        tvErrorKategori = findViewById(R.id.tv_error_kategori)
        etTanggal      = findViewById(R.id.et_tanggal)
        etWaktu        = findViewById(R.id.et_waktu)
        etCatatan      = findViewById(R.id.et_catatan)
        btnSimpan      = findViewById(R.id.btn_simpan)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (isEdit) "Edit Transaksi" else "Tambah Transaksi"
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupKategoriSpinner() {
        loadKategoriSpinner()
    }

    private fun loadKategoriSpinner(selectId: Long = -1L) {
        kategorList = db.getCategoriesByUser(currentUserId).toMutableList()

        val displayList = kategorList.map { it.namaKategori }.toMutableList()
        displayList.add(TAMBAH_KATEGORI_LABEL)

        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            displayList
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerKategori.adapter = spinnerAdapter

        // Pilih kategori sesuai ID jika edit
        if (selectId != -1L) {
            val idx = kategorList.indexOfFirst { it.id == selectId }
            if (idx >= 0) {
                spinnerKategori.setSelection(idx)
                selectedKategoriId = selectId
            }
        } else if (kategorList.isNotEmpty()) {
            selectedKategoriId = kategorList[0].id
        }

        spinnerKategori.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                (view as? TextView)?.setTextColor(getColor(R.color.text_primary))
                if (pos == kategorList.size) {
                    // "Tambah Kategori Baru" dipilih
                    spinnerKategori.setSelection(
                        if (selectedKategoriId != -1L) kategorList.indexOfFirst { it.id == selectedKategoriId } else 0
                    )
                    showTambahKategoriDialog()
                } else {
                    selectedKategoriId = kategorList[pos].id
                    tvErrorKategori.visibility = View.GONE
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun showTambahKategoriDialog() {
        val etInput = EditText(this).apply {
            hint = "Nama Kategori"
            setTextColor(getColor(R.color.text_primary))
            setHintTextColor(getColor(R.color.text_hint))
            setPadding(48, 32, 48, 32)
        }

        AlertDialog.Builder(this)
            .setTitle("Tambah Kategori Baru")
            .setView(etInput)
            .setPositiveButton("Simpan") { _, _ ->
                val namaKategori = etInput.text.toString().trim()
                when {
                    namaKategori.isEmpty() -> {
                        Toast.makeText(this, "Nama kategori tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    }
                    db.isCategoryExist(currentUserId, namaKategori) -> {
                        Toast.makeText(this, "Kategori sudah ada", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        val newId = db.addCategory(currentUserId, namaKategori)
                        if (newId != -1L) {
                            Toast.makeText(this, "Kategori berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                            loadKategoriSpinner(newId)
                        }
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun setupDateTimePickers() {
        etTanggal.setOnClickListener { showDatePicker() }
        etWaktu.setOnClickListener { showTimePicker() }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                etTanggal.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        val cal = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hour, minute ->
                etWaktu.setText(String.format("%02d:%02d", hour, minute))
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun setDefaultDate() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        etTanggal.setText(sdf.format(Calendar.getInstance().time))
    }

    private fun loadExistingData() {
        val cashflow = db.getCashflowById(cashflowId) ?: run { finish(); return }

        etNama.setText(cashflow.namaTransaksi)
        etNominal.setText(cashflow.nominal.toLong().toString())
        etCatatan.setText(cashflow.catatan ?: "")

        if (cashflow.jenisTransaksi == "income") rbPemasukan.isChecked = true
        else rbPengeluaran.isChecked = true

        // Parse timestamp → tanggal & waktu
        try {
            val sdf  = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = sdf.parse(cashflow.timestamp)
            if (date != null) {
                etTanggal.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date))
                val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
                if (time != "00:00") etWaktu.setText(time)
            }
        } catch (e: Exception) {
            setDefaultDate()
        }

        loadKategoriSpinner(cashflow.categoryId)
    }

    private fun setupSaveButton() {
        btnSimpan.setOnClickListener {
            if (validateInputs()) performSave()
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        val nama    = etNama.text.toString().trim()
        val nominal = etNominal.text.toString().trim()

        // Nama transaksi
        if (nama.isEmpty()) {
            tilNama.error = "Nama transaksi wajib diisi"
            isValid = false
        } else {
            tilNama.error = null
        }

        // Jenis transaksi
        if (rgJenis.checkedRadioButtonId == -1) {
            Toast.makeText(this, "Pilih jenis transaksi", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        // Nominal
        when {
            nominal.isEmpty() -> {
                tilNominal.error = "Nominal wajib diisi"
                isValid = false
            }
            nominal.toDoubleOrNull() == null || nominal.toDouble() <= 0 -> {
                tilNominal.error = "Nominal harus lebih dari 0"
                isValid = false
            }
            else -> tilNominal.error = null
        }

        // Kategori
        if (selectedKategoriId == -1L) {
            tvErrorKategori.text = "Pilih kategori"
            tvErrorKategori.visibility = View.VISIBLE
            isValid = false
        } else {
            tvErrorKategori.visibility = View.GONE
        }

        return isValid
    }

    private fun performSave() {
        val nama    = etNama.text.toString().trim()
        val nominal = etNominal.text.toString().trim().toDouble()
        val jenis   = if (rbPemasukan.isChecked) "income" else "expense"
        val catatan = etCatatan.text.toString().trim().ifEmpty { null }
        val tanggal = etTanggal.text.toString().trim()
        val waktu   = etWaktu.text.toString().trim().ifEmpty { "00:00" }
        val timestamp = "$tanggal $waktu:00"

        if (isEdit) {
            val cashflow = Cashflow(
                id             = cashflowId,
                categoryId     = selectedKategoriId,
                userId         = currentUserId,
                namaTransaksi  = nama,
                jenisTransaksi = jenis,
                nominal        = nominal,
                catatan        = catatan,
                timestamp      = timestamp
            )
            val berhasil = db.updateCashflow(cashflow)
            if (berhasil) {
                Toast.makeText(this, "Transaksi berhasil diperbarui", Toast.LENGTH_SHORT).show()
                navigateBack()
            } else {
                Toast.makeText(this, "Gagal memperbarui transaksi", Toast.LENGTH_SHORT).show()
            }
        } else {
            val cashflow = Cashflow(
                categoryId     = selectedKategoriId,
                userId         = currentUserId,
                namaTransaksi  = nama,
                jenisTransaksi = jenis,
                nominal        = nominal,
                catatan        = catatan,
                timestamp      = timestamp
            )
            val newId = db.insertCashflow(cashflow)
            if (newId != -1L) {
                Toast.makeText(this, "Transaksi berhasil disimpan", Toast.LENGTH_SHORT).show()
                navigateBack()
            } else {
                Toast.makeText(this, "Gagal menyimpan transaksi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateBack() {
        if (prevPage == "dashboard") {
            val intent = Intent(this, DashboardActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("USER_ID", currentUserId)
            }
            startActivity(intent)
        }
        finish()
    }
}