package id.digikriya.moneflo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import id.digikriya.moneflo.database.DatabaseHelper

class RegisterActivity : AppCompatActivity() {

    private lateinit var etUsername: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etNamaLengkap: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etKonfirmasiPassword: TextInputEditText

    private lateinit var tilUsername: TextInputLayout
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilNamaLengkap: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilKonfirmasiPassword: TextInputLayout

    private lateinit var cbLangsungLogin: CheckBox
    private lateinit var btnDaftar: View
    private lateinit var progressBar: ProgressBar
    private lateinit var tvLogin: TextView

    private lateinit var db: DatabaseHelper

    // Regex: hanya huruf kecil, angka, titik, underscore — tanpa spasi
    private val usernameRegex = Regex("^[a-z0-9._]+$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        db = DatabaseHelper(this)
        initViews()
        setupListeners()
    }

    private fun initViews() {
        etUsername           = findViewById(R.id.et_username)
        etEmail              = findViewById(R.id.et_email)
        etNamaLengkap        = findViewById(R.id.et_nama_lengkap)
        etPassword           = findViewById(R.id.et_password)
        etKonfirmasiPassword = findViewById(R.id.et_konfirmasi_password)

        tilUsername           = findViewById(R.id.til_username)
        tilEmail              = findViewById(R.id.til_email)
        tilNamaLengkap        = findViewById(R.id.til_nama_lengkap)
        tilPassword           = findViewById(R.id.til_password)
        tilKonfirmasiPassword = findViewById(R.id.til_konfirmasi_password)

        cbLangsungLogin = findViewById(R.id.cb_langsung_login)
        btnDaftar       = findViewById(R.id.btn_daftar)
        progressBar     = findViewById(R.id.progress_bar)
        tvLogin         = findViewById(R.id.tv_login)
    }

    private fun setupListeners() {
        btnDaftar.setOnClickListener {
            if (validateInputs()) performRegister()
        }

        tvLogin.setOnClickListener {
            finish() // kembali ke LoginActivity
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        val username  = etUsername.text.toString().trim()
        val email     = etEmail.text.toString().trim()
        val nama      = etNamaLengkap.text.toString().trim()
        val password  = etPassword.text.toString()
        val konfirmasi = etKonfirmasiPassword.text.toString()

        // Username
        when {
            username.isEmpty() -> {
                tilUsername.error = "Username wajib diisi"
                isValid = false
            }
            !usernameRegex.matches(username) -> {
                tilUsername.error = "Hanya huruf kecil, angka, titik, dan underscore"
                isValid = false
            }
            db.isUsernameExist(username) -> {
                tilUsername.error = "Username sudah digunakan"
                isValid = false
            }
            else -> tilUsername.error = null
        }

        // Email
        when {
            email.isEmpty() -> {
                tilEmail.error = "Email wajib diisi"
                isValid = false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                tilEmail.error = "Format email tidak valid"
                isValid = false
            }
            db.isEmailExist(email) -> {
                tilEmail.error = "Email sudah digunakan"
                isValid = false
            }
            else -> tilEmail.error = null
        }

        // Nama Lengkap
        if (nama.isEmpty()) {
            tilNamaLengkap.error = "Nama lengkap wajib diisi"
            isValid = false
        } else {
            tilNamaLengkap.error = null
        }

        // Password
        if (password.length < 6) {
            tilPassword.error = "Password minimal 6 karakter"
            isValid = false
        } else {
            tilPassword.error = null
        }

        // Konfirmasi Password
        if (konfirmasi != password) {
            tilKonfirmasiPassword.error = "Konfirmasi password tidak sama"
            isValid = false
        } else {
            tilKonfirmasiPassword.error = null
        }

        return isValid
    }

    private fun performRegister() {
        setLoadingState(true)

        val username  = etUsername.text.toString().trim()
        val email     = etEmail.text.toString().trim()
        val nama      = etNamaLengkap.text.toString().trim()
        val password  = etPassword.text.toString()

        val userId = db.registerUser(username, email, nama, password)

        if (userId == -1L) {
            // Seharusnya tidak terjadi karena sudah divalidasi di atas
            setLoadingState(false)
            tilUsername.error = "Gagal mendaftar, coba lagi"
            return
        }

        setLoadingState(false)

        if (cbLangsungLogin.isChecked) {
            // Langsung login tanpa set session permanen
            // TODO: ganti MainActivity dengan DashboardActivity saat sudah dibuat
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        } else {
            // Kembali ke Login
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        finish()
    }

    private fun setLoadingState(isLoading: Boolean) {
        btnDaftar.isEnabled = !isLoading
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        etUsername.isEnabled = !isLoading
        etEmail.isEnabled = !isLoading
        etNamaLengkap.isEnabled = !isLoading
        etPassword.isEnabled = !isLoading
        etKonfirmasiPassword.isEnabled = !isLoading
    }
}
