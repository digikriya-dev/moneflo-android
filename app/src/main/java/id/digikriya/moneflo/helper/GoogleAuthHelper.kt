package id.digikriya.moneflo.helper

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import id.digikriya.moneflo.R
import id.digikriya.moneflo.database.DatabaseHelper

/**
 * Helper class untuk Google Sign-In + Firebase Auth.
 *
 * Setelah autentikasi Google berhasil, user otomatis disimpan
 * ke SQLite lokal (tabel users) jika belum ada.
 */
class GoogleAuthHelper(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: DatabaseHelper = DatabaseHelper(context)

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    /**
     * Minta intent sign-in yang "bersih" — sign-out dulu dari GoogleSignInClient
     * supaya akun terakhir yang dipakai tidak otomatis ke-cache, dan dialog pemilih
     * akun Google selalu muncul setiap kali tombol "Masuk dengan Google" ditekan.
     */
    fun getFreshSignInIntent(onReady: (Intent) -> Unit) {
        googleSignInClient.signOut().addOnCompleteListener {
            onReady(googleSignInClient.signInIntent)
        }
    }

    /**
     * Proses result dari Google Sign-In intent.
     * Dipanggil di onActivityResult / ActivityResultCallback.
     *
     * @param data  Intent data dari result
     * @param onSuccess callback dengan userId SQLite lokal
     * @param onError   callback dengan pesan error
     */
    fun handleSignInResult(
        data: Intent?,
        onSuccess: (userId: Long, isNewUser: Boolean) -> Unit,
        onError: (message: String) -> Unit
    ) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account, onSuccess, onError)
        } catch (e: ApiException) {
            onError("Google Sign-In dibatalkan atau gagal (${e.statusCode})")
        }
    }

    private fun firebaseAuthWithGoogle(
        account: GoogleSignInAccount,
        onSuccess: (userId: Long, isNewUser: Boolean) -> Unit,
        onError: (message: String) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val firebaseUser = result.user ?: run {
                    onError("Gagal mendapatkan data user dari Firebase")
                    return@addOnSuccessListener
                }
                val isNew = result.additionalUserInfo?.isNewUser ?: false
                saveOrGetLocalUser(firebaseUser, account, isNew, onSuccess, onError)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Autentikasi Firebase gagal")
            }
    }

    /**
     * Simpan user Google ke SQLite lokal jika belum ada.
     * Jika sudah ada (login ulang), langsung return userId yang ada.
     */
    private fun saveOrGetLocalUser(
        firebaseUser: FirebaseUser,
        account: GoogleSignInAccount,
        isNewFirebaseUser: Boolean,
        onSuccess: (userId: Long, isNewUser: Boolean) -> Unit,
        onError: (message: String) -> Unit
    ) {
        val email       = firebaseUser.email ?: account.email ?: ""
        val namaLengkap = firebaseUser.displayName ?: account.displayName ?: "Pengguna Google"

        // Buat username dari email (ambil bagian sebelum @, bersihkan karakter invalid)
        val usernameBase = email.substringBefore("@")
            .lowercase()
            .replace(Regex("[^a-z0-9._]"), "_")
        val username = generateUniqueUsername(usernameBase)

        // Cek apakah email sudah terdaftar di SQLite lokal
        val existingUser = db.getUserByEmail(email)

        if (existingUser != null) {
            // User sudah ada → langsung login
            onSuccess(existingUser.id, false)
        } else {
            // User baru → daftarkan ke SQLite
            // Password diisi placeholder acak (tidak diketahui & tidak dipakai user) —
            // isGoogleAccount = true supaya halaman Ubah Password tahu untuk melewati
            // verifikasi "password lama" sampai user menetapkan password pertamanya sendiri.
            val userId = db.registerUser(
                username        = username,
                email           = email,
                namaLengkap     = namaLengkap,
                password        = firebaseUser.uid,
                isGoogleAccount = true
            )
            if (userId != -1L) {
                onSuccess(userId, true)
            } else {
                onError("Gagal menyimpan akun Google ke database lokal")
            }
        }
    }

    /** Generate username unik jika sudah ada yang pakai */
    private fun generateUniqueUsername(base: String): String {
        var username = base
        var counter = 1
        while (db.isUsernameExist(username)) {
            username = "${base}${counter}"
            counter++
        }
        return username
    }

    /** Sign out dari Firebase & Google */
    fun signOut(onComplete: () -> Unit) {
        auth.signOut()
        googleSignInClient.signOut().addOnCompleteListener { onComplete() }
    }

    /** Cek apakah sudah ada sesi Firebase aktif */
    fun getCurrentFirebaseUser(): FirebaseUser? = auth.currentUser
}
