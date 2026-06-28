package id.digikriya.moneflo

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import id.digikriya.moneflo.database.DatabaseHelper
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var tvAvatarInitial: TextView
    private lateinit var tvNamaLengkap: TextView
    private lateinit var tvUsernameDisplay: TextView
    private lateinit var tvInfoUsername: TextView
    private lateinit var tvInfoEmail: TextView
    private lateinit var tvInfoTanggal: TextView
    private lateinit var btnEditProfil: LinearLayout
    private lateinit var btnUbahPassword: LinearLayout
    private lateinit var btnLogout: LinearLayout

    private lateinit var db: DatabaseHelper
    private var currentUserId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        currentUserId = intent.getLongExtra("USER_ID", -1L)
        if (currentUserId == -1L) { finish(); return }

        db = DatabaseHelper(this)

        initViews()
        setupToolbar()
        setupListeners()

        // Jika dibuka dari popup menu dashboard dengan action langsung
        when (intent.getStringExtra("ACTION")) {
            "edit_profil"    -> openEditProfil()
            "ubah_password"  -> openUbahPassword()
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }

    private fun initViews() {
        toolbar            = findViewById(R.id.toolbar)
        tvAvatarInitial    = findViewById(R.id.tv_avatar_initial)
        tvNamaLengkap      = findViewById(R.id.tv_nama_lengkap)
        tvUsernameDisplay  = findViewById(R.id.tv_username_display)
        tvInfoUsername     = findViewById(R.id.tv_info_username)
        tvInfoEmail        = findViewById(R.id.tv_info_email)
        tvInfoTanggal      = findViewById(R.id.tv_info_tanggal)
        btnEditProfil      = findViewById(R.id.btn_edit_profil)
        btnUbahPassword    = findViewById(R.id.btn_ubah_password)
        btnLogout          = findViewById(R.id.btn_logout)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupListeners() {
        btnEditProfil.setOnClickListener { openEditProfil() }
        btnUbahPassword.setOnClickListener { openUbahPassword() }
        btnLogout.setOnClickListener { showLogoutConfirmation() }
    }

    private fun loadUserData() {
        val user = db.getUserById(currentUserId) ?: return

        tvAvatarInitial.text   = user.namaLengkap.firstOrNull()?.uppercase() ?: "?"
        tvNamaLengkap.text     = user.namaLengkap
        tvUsernameDisplay.text = "@${user.username}"
        tvInfoUsername.text    = user.username
        tvInfoEmail.text       = user.email

        // Format tanggal registrasi
        tvInfoTanggal.text = try {
            val sdf    = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date   = sdf.parse(user.timestamp)
            val outSdf = SimpleDateFormat("dd MMMM yyyy", Locale("id"))
            if (date != null) outSdf.format(date) else user.timestamp
        } catch (e: Exception) {
            user.timestamp
        }
    }

    private fun openEditProfil() {
        startActivity(Intent(this, EditProfileActivity::class.java).apply {
            putExtra("USER_ID", currentUserId)
        })
    }

    private fun openUbahPassword() {
        startActivity(Intent(this, ChangePasswordActivity::class.java).apply {
            putExtra("USER_ID", currentUserId)
        })
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Yakin ingin keluar dari akun?")
            .setPositiveButton("Logout") { _, _ ->
                db.logout()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}