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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import id.digikriya.moneflo.helper.GoogleAuthHelper
import id.digikriya.moneflo.helper.playEntranceAnimation

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

    private lateinit var btnGoogle: View
    private lateinit var googleAuth: GoogleAuthHelper

    // Regex: hanya huruf kecil, angka, titik, underscore — tanpa spasi
    private val usernameRegex = Regex("^[a-z0-9._]+$")

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        googleAuth.handleSignInResult(
            data      = result.data,
            onSuccess = { userId, isNewUser ->
                db.setLoginSession(userId)
                Toast.makeText(this, "Akun Google berhasil didaftarkan!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, DashboardActivity::class.java) // TODO: ganti DashboardActivity
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            },
            onError = { message ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        db = DatabaseHelper(this)
        googleAuth = GoogleAuthHelper(this)

        initViews()
        setupListeners()
        playEntranceAnimation()
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
        btnGoogle = findViewById(R.id.btn_google)

    }

    private fun setupListeners() {
        btnDaftar.setOnClickListener {
            if (validateInputs()) performRegister()
        }

        tvLogin.setOnClickListener {
            // Navigasi eksplisit (bukan cuma finish()) — RegisterActivity bisa diakses
            // dari Onboarding, yang sudah finish() dirinya sendiri lebih dulu, jadi
            // back stack bisa kosong dan finish() saja akan menutup seluruh app.
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        btnGoogle.setOnClickListener {
            googleAuth.getFreshSignInIntent { intent ->
                googleSignInLauncher.launch(intent)
            }
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
            // TAMBAHKAN: set session dulu sebelum pindah
            db.setLoginSession(userId)
            val intent = Intent(this, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        } else {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

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
