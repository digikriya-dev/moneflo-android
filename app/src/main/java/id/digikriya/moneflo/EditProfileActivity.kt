package id.digikriya.moneflo

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import id.digikriya.moneflo.database.DatabaseHelper
import id.digikriya.moneflo.helper.playEntranceAnimation

class EditProfileActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var tilNamaLengkap: TextInputLayout
    private lateinit var etNamaLengkap: TextInputEditText
    private lateinit var tilEmail: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var btnSimpan: View

    private lateinit var db: DatabaseHelper
    private var currentUserId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        currentUserId = intent.getLongExtra("USER_ID", -1L)
        if (currentUserId == -1L) { finish(); return }

        db = DatabaseHelper(this)
        initViews()
        setupToolbar()
        loadCurrentData()
        setupSaveButton()
        playEntranceAnimation()
    }

    private fun initViews() {
        toolbar        = findViewById(R.id.toolbar)
        tilNamaLengkap = findViewById(R.id.til_nama_lengkap)
        etNamaLengkap  = findViewById(R.id.et_nama_lengkap)
        tilEmail       = findViewById(R.id.til_email)
        etEmail        = findViewById(R.id.et_email)
        btnSimpan      = findViewById(R.id.btn_simpan)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    private fun loadCurrentData() {
        val user = db.getUserById(currentUserId) ?: return
        etNamaLengkap.setText(user.namaLengkap)
        etEmail.setText(user.email)
    }

    private fun setupSaveButton() {
        btnSimpan.setOnClickListener {
            if (validateInputs()) performSave()
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        val nama  = etNamaLengkap.text.toString().trim()
        val email = etEmail.text.toString().trim()

        if (nama.isEmpty()) {
            tilNamaLengkap.error = "Nama lengkap wajib diisi"
            isValid = false
        } else {
            tilNamaLengkap.error = null
        }

        when {
            email.isEmpty() -> {
                tilEmail.error = "Email wajib diisi"
                isValid = false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                tilEmail.error = "Format email tidak valid"
                isValid = false
            }
            db.isEmailExistExcept(email, currentUserId) -> {
                tilEmail.error = "Email sudah digunakan"
                isValid = false
            }
            else -> tilEmail.error = null
        }

        return isValid
    }

    private fun performSave() {
        val berhasil = db.updateProfile(
            currentUserId,
            etNamaLengkap.text.toString().trim(),
            etEmail.text.toString().trim()
        )
        if (berhasil) {
            Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        } else {
            Toast.makeText(this, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show()
        }
    }
}