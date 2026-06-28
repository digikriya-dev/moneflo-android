package id.digikriya.moneflo

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import id.digikriya.moneflo.database.DatabaseHelper

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var tilPasswordLama: TextInputLayout
    private lateinit var etPasswordLama: TextInputEditText
    private lateinit var tilPasswordBaru: TextInputLayout
    private lateinit var etPasswordBaru: TextInputEditText
    private lateinit var tilKonfirmasiPassword: TextInputLayout
    private lateinit var etKonfirmasiPassword: TextInputEditText
    private lateinit var btnSimpan: View

    private lateinit var db: DatabaseHelper
    private var currentUserId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        currentUserId = intent.getLongExtra("USER_ID", -1L)
        if (currentUserId == -1L) { finish(); return }

        db = DatabaseHelper(this)

        initViews()
        setupToolbar()
        setupSaveButton()
    }

    private fun initViews() {
        toolbar                = findViewById(R.id.toolbar)
        tilPasswordLama        = findViewById(R.id.til_password_lama)
        etPasswordLama         = findViewById(R.id.et_password_lama)
        tilPasswordBaru        = findViewById(R.id.til_password_baru)
        etPasswordBaru         = findViewById(R.id.et_password_baru)
        tilKonfirmasiPassword  = findViewById(R.id.til_konfirmasi_password)
        etKonfirmasiPassword   = findViewById(R.id.et_konfirmasi_password)
        btnSimpan              = findViewById(R.id.btn_simpan)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupSaveButton() {
        btnSimpan.setOnClickListener {
            if (validateInputs()) performSave()
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        val passwordLama   = etPasswordLama.text.toString()
        val passwordBaru   = etPasswordBaru.text.toString()
        val konfirmasi     = etKonfirmasiPassword.text.toString()

        // Cek password lama
        if (passwordLama.isEmpty()) {
            tilPasswordLama.error = "Password lama wajib diisi"
            isValid = false
        } else if (!db.verifyOldPassword(currentUserId, passwordLama)) {
            tilPasswordLama.error = "Password lama tidak sesuai"
            isValid = false
        } else {
            tilPasswordLama.error = null
        }

        // Cek password baru
        if (passwordBaru.length < 6) {
            tilPasswordBaru.error = "Password baru minimal 6 karakter"
            isValid = false
        } else {
            tilPasswordBaru.error = null
        }

        // Cek konfirmasi
        if (konfirmasi != passwordBaru) {
            tilKonfirmasiPassword.error = "Konfirmasi password tidak sama"
            isValid = false
        } else {
            tilKonfirmasiPassword.error = null
        }

        return isValid
    }

    private fun performSave() {
        val passwordBaru = etPasswordBaru.text.toString()
        val berhasil = db.updatePassword(currentUserId, passwordBaru)

        if (berhasil) {
            Toast.makeText(this, "Password berhasil diubah", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Gagal mengubah password", Toast.LENGTH_SHORT).show()
        }
    }
}