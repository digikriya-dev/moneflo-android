package id.digikriya.moneflo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import id.digikriya.moneflo.database.DatabaseHelper
import id.digikriya.moneflo.helper.BottomNavHelper
import id.digikriya.moneflo.helper.PhotoHelper
import id.digikriya.moneflo.helper.ThemePrefs
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var btnChangePhoto: View
    private lateinit var tvAvatarInitial: TextView
    private lateinit var ivAvatarPhoto: ImageView
    private lateinit var tvNamaLengkap: TextView
    private lateinit var tvUsernameDisplay: TextView
    private lateinit var tvInfoUsername: TextView
    private lateinit var tvInfoEmail: TextView
    private lateinit var tvInfoTanggal: TextView
    private lateinit var btnEditProfil: LinearLayout
    private lateinit var btnUbahPassword: LinearLayout
    private lateinit var btnLogout: LinearLayout
    private lateinit var optThemeLight: TextView
    private lateinit var optThemeDark: TextView
    private lateinit var optThemeSystem: TextView
    private lateinit var bottomNav: BottomNavigationView

    private lateinit var db: DatabaseHelper
    private var currentUserId: Long = -1L

    private val pickPhotoLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) savePickedPhoto(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        currentUserId = intent.getLongExtra("USER_ID", -1L)
        if (currentUserId == -1L) { finish(); return }

        db = DatabaseHelper(this)
        initViews()
        setupToolbar()
        setupListeners()
        setupThemeSelector()
        BottomNavHelper.setup(bottomNav, R.id.nav_profile, this, currentUserId)
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }

    private fun initViews() {
        toolbar           = findViewById(R.id.toolbar)
        btnChangePhoto    = findViewById(R.id.btn_change_photo)
        tvAvatarInitial   = findViewById(R.id.tv_avatar_initial)
        ivAvatarPhoto     = findViewById(R.id.iv_avatar_photo)
        tvNamaLengkap     = findViewById(R.id.tv_nama_lengkap)
        tvUsernameDisplay = findViewById(R.id.tv_username_display)
        tvInfoUsername    = findViewById(R.id.tv_info_username)
        tvInfoEmail       = findViewById(R.id.tv_info_email)
        tvInfoTanggal     = findViewById(R.id.tv_info_tanggal)
        btnEditProfil     = findViewById(R.id.btn_edit_profil)
        btnUbahPassword   = findViewById(R.id.btn_ubah_password)
        btnLogout         = findViewById(R.id.btn_logout)
        optThemeLight     = findViewById(R.id.opt_theme_light)
        optThemeDark      = findViewById(R.id.opt_theme_dark)
        optThemeSystem    = findViewById(R.id.opt_theme_system)
        bottomNav         = findViewById(R.id.bottom_nav)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
    }

    private fun setupListeners() {
        btnEditProfil.setOnClickListener { openEditProfil() }
        btnUbahPassword.setOnClickListener { openUbahPassword() }
        btnLogout.setOnClickListener { showLogoutConfirmation() }
        btnChangePhoto.setOnClickListener { pickPhotoLauncher.launch("image/*") }
    }

    private fun savePickedPhoto(uri: Uri) {
        val path = PhotoHelper.saveFromUri(this, uri, currentUserId)
        if (path != null && db.updatePhotoPath(currentUserId, path)) {
            loadUserData()
        } else {
            Toast.makeText(this, "Gagal menyimpan foto profil", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupThemeSelector() {
        optThemeLight.setOnClickListener { applyTheme(ThemePrefs.MODE_LIGHT) }
        optThemeDark.setOnClickListener { applyTheme(ThemePrefs.MODE_DARK) }
        optThemeSystem.setOnClickListener { applyTheme(ThemePrefs.MODE_SYSTEM) }
        renderThemeSelector()
    }

    private fun applyTheme(mode: String) {
        if (mode == ThemePrefs.getMode(this)) return
        ThemePrefs.setMode(this, mode)
        recreate()
    }

    private fun renderThemeSelector() {
        val selectedBg = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.bg_segment_selected)
        val selectedColor = getColor(R.color.segment_selected_text)
        val unselectedColor = getColor(R.color.text_secondary)
        val currentMode = ThemePrefs.getMode(this)

        listOf(
            optThemeLight to ThemePrefs.MODE_LIGHT,
            optThemeDark to ThemePrefs.MODE_DARK,
            optThemeSystem to ThemePrefs.MODE_SYSTEM
        ).forEach { (tv, mode) ->
            if (mode == currentMode) {
                tv.background = selectedBg
                tv.setTextColor(selectedColor)
            } else {
                tv.background = null
                tv.setTextColor(unselectedColor)
            }
        }
    }

    private fun loadUserData() {
        val user = db.getUserById(currentUserId) ?: return
        tvAvatarInitial.text   = user.namaLengkap.firstOrNull()?.uppercase() ?: "?"
        if (PhotoHelper.loadCircular(ivAvatarPhoto, user.fotoProfil)) {
            ivAvatarPhoto.visibility = View.VISIBLE
            tvAvatarInitial.visibility = View.GONE
        } else {
            ivAvatarPhoto.visibility = View.GONE
            tvAvatarInitial.visibility = View.VISIBLE
        }
        tvNamaLengkap.text     = user.namaLengkap
        tvUsernameDisplay.text = "@${user.username}"
        tvInfoUsername.text    = user.username
        tvInfoEmail.text       = user.email
        tvInfoTanggal.text     = try {
            val sdf  = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = sdf.parse(user.timestamp)
            val out  = SimpleDateFormat("dd MMMM yyyy", Locale("id"))
            if (date != null) out.format(date) else user.timestamp
        } catch (e: Exception) { user.timestamp }
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
                // Hapus SharedPreferences
                getSharedPreferences("moneflo_prefs", MODE_PRIVATE)
                    .edit().remove("current_user_id").apply()
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
}