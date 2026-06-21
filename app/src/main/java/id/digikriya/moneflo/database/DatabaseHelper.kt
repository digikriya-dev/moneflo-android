package id.digikriya.moneflo.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import id.digikriya.moneflo.model.Cashflow
import id.digikriya.moneflo.model.Category
import id.digikriya.moneflo.model.Session
import id.digikriya.moneflo.model.Setting
import id.digikriya.moneflo.model.User
import java.security.MessageDigest

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // =========================================================
    // CONSTANTS
    // =========================================================
    companion object {
        const val DATABASE_NAME = "moneflo.db"
        const val DATABASE_VERSION = 1

        // Table names
        const val TABLE_USERS       = "users"
        const val TABLE_SESSIONS    = "sessions"
        const val TABLE_CATEGORIES  = "categories"
        const val TABLE_SETTINGS    = "settings"
        const val TABLE_CASHFLOWS   = "cashflows"
        const val TABLE_TAGS        = "tags"
        const val TABLE_CASHFLOW_TAGS = "cashflow_tags"

        // Shared columns
        const val COL_ID            = "id"
        const val COL_USER_ID       = "user_id"
        const val COL_TIMESTAMP     = "timestamp"

        // Users columns
        const val COL_USERNAME      = "username"
        const val COL_EMAIL         = "email"
        const val COL_PASSWORD      = "password"
        const val COL_NAMA_LENGKAP  = "nama_lengkap"

        // Sessions columns
        const val COL_IS_LOGGED_IN  = "is_logged_in"

        // Categories columns
        const val COL_NAMA_KATEGORI = "nama_kategori"
        const val COL_IS_ACTIVE     = "is_active"

        // Settings columns
        const val COL_LIMIT_PENGELUARAN = "limit_pengeluaran"
        const val COL_PERINGATAN_AKTIF  = "peringatan_aktif"

        // Cashflows columns
        const val COL_CATEGORY_ID       = "category_id"
        const val COL_NAMA_TRANSAKSI    = "nama_transaksi"
        const val COL_JENIS_TRANSAKSI   = "jenis_transaksi"
        const val COL_NOMINAL           = "nominal"
        const val COL_CATATAN           = "catatan"

        // Tags columns
        const val COL_NAMA_TAG  = "nama_tag"

        // Cashflow Tags columns
        const val COL_CASHFLOW_ID   = "cashflow_id"
        const val COL_TAG_ID        = "tag_id"

        // Jenis transaksi values
        const val JENIS_INCOME  = "income"
        const val JENIS_EXPENSE = "expense"

        // Default categories
        val DEFAULT_CATEGORIES = listOf(
            "Makan", "Transportasi", "Kos", "Tugas", "Hiburan", "Lainnya"
        )
    }

    // =========================================================
    // CREATE TABLES
    // =========================================================
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("PRAGMA foreign_keys = ON;")
        createTables(db)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        if (!db.isReadOnly) {
            db.execSQL("PRAGMA foreign_keys = ON;")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CASHFLOW_TAGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TAGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CASHFLOWS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SETTINGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SESSIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    private fun createTables(db: SQLiteDatabase) {
        // USERS
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_USERS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USERNAME TEXT NOT NULL UNIQUE,
                $COL_EMAIL TEXT NOT NULL UNIQUE,
                $COL_PASSWORD TEXT NOT NULL,
                $COL_NAMA_LENGKAP TEXT NOT NULL,
                $COL_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent())

        // SESSIONS
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_SESSIONS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_ID INTEGER NOT NULL,
                $COL_IS_LOGGED_IN INTEGER NOT NULL DEFAULT 0,
                $COL_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY ($COL_USER_ID) REFERENCES $TABLE_USERS($COL_ID) ON DELETE CASCADE
            )
        """.trimIndent())

        // CATEGORIES
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_CATEGORIES (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_ID INTEGER NOT NULL,
                $COL_NAMA_KATEGORI TEXT NOT NULL,
                $COL_IS_ACTIVE INTEGER NOT NULL DEFAULT 1,
                $COL_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY ($COL_USER_ID) REFERENCES $TABLE_USERS($COL_ID) ON DELETE CASCADE,
                UNIQUE($COL_USER_ID, $COL_NAMA_KATEGORI)
            )
        """.trimIndent())

        // SETTINGS
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_SETTINGS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_ID INTEGER NOT NULL UNIQUE,
                $COL_LIMIT_PENGELUARAN REAL DEFAULT 0,
                $COL_PERINGATAN_AKTIF INTEGER DEFAULT 0,
                $COL_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY ($COL_USER_ID) REFERENCES $TABLE_USERS($COL_ID) ON DELETE CASCADE
            )
        """.trimIndent())

        // CASHFLOWS
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_CASHFLOWS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CATEGORY_ID INTEGER NOT NULL,
                $COL_USER_ID INTEGER NOT NULL,
                $COL_NAMA_TRANSAKSI TEXT NOT NULL,
                $COL_JENIS_TRANSAKSI TEXT NOT NULL,
                $COL_NOMINAL REAL NOT NULL,
                $COL_CATATAN TEXT,
                $COL_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY ($COL_CATEGORY_ID) REFERENCES $TABLE_CATEGORIES($COL_ID) ON DELETE RESTRICT,
                FOREIGN KEY ($COL_USER_ID) REFERENCES $TABLE_USERS($COL_ID) ON DELETE CASCADE,
                CHECK ($COL_JENIS_TRANSAKSI IN ('income', 'expense')),
                CHECK ($COL_NOMINAL > 0)
            )
        """.trimIndent())

        // TAGS
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_TAGS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_ID INTEGER NOT NULL,
                $COL_NAMA_TAG TEXT NOT NULL,
                $COL_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY ($COL_USER_ID) REFERENCES $TABLE_USERS($COL_ID) ON DELETE CASCADE,
                UNIQUE($COL_USER_ID, $COL_NAMA_TAG)
            )
        """.trimIndent())

        // CASHFLOW TAGS
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_CASHFLOW_TAGS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CASHFLOW_ID INTEGER NOT NULL,
                $COL_TAG_ID INTEGER NOT NULL,
                $COL_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY ($COL_CASHFLOW_ID) REFERENCES $TABLE_CASHFLOWS($COL_ID) ON DELETE CASCADE,
                FOREIGN KEY ($COL_TAG_ID) REFERENCES $TABLE_TAGS($COL_ID) ON DELETE CASCADE,
                UNIQUE($COL_CASHFLOW_ID, $COL_TAG_ID)
            )
        """.trimIndent())
    }

    // =========================================================
    // HELPER: Password Hashing (SHA-256)
    // =========================================================
    fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun verifyPassword(input: String, hash: String): Boolean {
        return hashPassword(input) == hash
    }

    // =========================================================
    // INIT: Seed default categories for a new user
    // =========================================================
    fun seedDefaultCategories(userId: Long) {
        val db = writableDatabase
        DEFAULT_CATEGORIES.forEach { namaKategori ->
            val cv = ContentValues().apply {
                put(COL_USER_ID, userId)
                put(COL_NAMA_KATEGORI, namaKategori)
                put(COL_IS_ACTIVE, 1)
            }
            db.insertWithOnConflict(TABLE_CATEGORIES, null, cv, SQLiteDatabase.CONFLICT_IGNORE)
        }
    }

    // =========================================================
    // INIT: Default settings for a new user
    // =========================================================
    private fun createDefaultSettings(userId: Long) {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_USER_ID, userId)
            put(COL_LIMIT_PENGELUARAN, 0.0)
            put(COL_PERINGATAN_AKTIF, 0)
        }
        db.insertWithOnConflict(TABLE_SETTINGS, null, cv, SQLiteDatabase.CONFLICT_IGNORE)
    }

    // =========================================================
    // USER
    // =========================================================

    /**
     * Register user baru.
     * Return: user ID jika berhasil, -1 jika username/email sudah ada.
     */
    fun registerUser(username: String, email: String, namaLengkap: String, password: String): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_USERNAME, username.lowercase())
            put(COL_EMAIL, email.lowercase())
            put(COL_NAMA_LENGKAP, namaLengkap)
            put(COL_PASSWORD, hashPassword(password))
        }
        val userId = db.insertWithOnConflict(TABLE_USERS, null, cv, SQLiteDatabase.CONFLICT_IGNORE)
        if (userId != -1L) {
            seedDefaultCategories(userId)
            createDefaultSettings(userId)
        }
        return userId
    }

    /** Cek apakah username sudah dipakai */
    fun isUsernameExist(username: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_ID FROM $TABLE_USERS WHERE $COL_USERNAME = ? COLLATE NOCASE",
            arrayOf(username)
        )
        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }

    /** Cek apakah email sudah dipakai */
    fun isEmailExist(email: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_ID FROM $TABLE_USERS WHERE $COL_EMAIL = ? COLLATE NOCASE",
            arrayOf(email)
        )
        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }

    /** Cek apakah email sudah dipakai, kecuali oleh user dengan ID tertentu (untuk edit profil) */
    fun isEmailExistExcept(email: String, excludeUserId: Long): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_ID FROM $TABLE_USERS WHERE $COL_EMAIL = ? COLLATE NOCASE AND $COL_ID != ?",
            arrayOf(email, excludeUserId.toString())
        )
        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }

    /**
     * Login dengan username & password.
     * Return: User object jika cocok, null jika tidak.
     */
    fun loginUser(username: String, password: String): User? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USERS WHERE $COL_USERNAME = ? COLLATE NOCASE",
            arrayOf(username)
        )
        var user: User? = null
        if (cursor.moveToFirst()) {
            val storedHash = cursor.getString(cursor.getColumnIndexOrThrow(COL_PASSWORD))
            if (verifyPassword(password, storedHash)) {
                user = cursorToUser(cursor)
            }
        }
        cursor.close()
        return user
    }

    /** Ambil user berdasarkan ID */
    fun getUserById(userId: Long): User? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USERS WHERE $COL_ID = ?",
            arrayOf(userId.toString())
        )
        var user: User? = null
        if (cursor.moveToFirst()) user = cursorToUser(cursor)
        cursor.close()
        return user
    }

    /** Ambil user berdasarkan username (untuk lupa password) */
    fun getUserByUsername(username: String): User? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USERS WHERE $COL_USERNAME = ? COLLATE NOCASE",
            arrayOf(username)
        )
        var user: User? = null
        if (cursor.moveToFirst()) user = cursorToUser(cursor)
        cursor.close()
        return user
    }

    /** Ambil user berdasarkan email (untuk Google Sign-In) */
    fun getUserByEmail(email: String): User? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USERS WHERE $COL_EMAIL = ? COLLATE NOCASE",
            arrayOf(email)
        )
        var user: User? = null
        if (cursor.moveToFirst()) user = cursorToUser(cursor)
        cursor.close()
        return user
    }

    /** Update password user (untuk reset password & ubah password) */
    fun updatePassword(userId: Long, newPassword: String): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_PASSWORD, hashPassword(newPassword))
        }
        val rows = db.update(TABLE_USERS, cv, "$COL_ID = ?", arrayOf(userId.toString()))
        return rows > 0
    }

    /** Verifikasi password lama (untuk ubah password di profil) */
    fun verifyOldPassword(userId: Long, oldPassword: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_PASSWORD FROM $TABLE_USERS WHERE $COL_ID = ?",
            arrayOf(userId.toString())
        )
        var match = false
        if (cursor.moveToFirst()) {
            val storedHash = cursor.getString(0)
            match = verifyPassword(oldPassword, storedHash)
        }
        cursor.close()
        return match
    }

    /** Update profil user (nama lengkap & email) */
    fun updateProfile(userId: Long, namaLengkap: String, email: String): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_NAMA_LENGKAP, namaLengkap)
            put(COL_EMAIL, email.lowercase())
        }
        val rows = db.update(TABLE_USERS, cv, "$COL_ID = ?", arrayOf(userId.toString()))
        return rows > 0
    }

    private fun cursorToUser(cursor: android.database.Cursor): User {
        return User(
            id        = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
            username  = cursor.getString(cursor.getColumnIndexOrThrow(COL_USERNAME)),
            email     = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)),
            password  = cursor.getString(cursor.getColumnIndexOrThrow(COL_PASSWORD)),
            namaLengkap = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAMA_LENGKAP)),
            timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIMESTAMP))
        )
    }

    // =========================================================
    // SESSION
    // =========================================================

    /**
     * Set session login (untuk "Tetap Login").
     * Set semua is_logged_in = 0 dulu, lalu insert baru.
     */
    fun setLoginSession(userId: Long) {
        val db = writableDatabase
        db.execSQL("UPDATE $TABLE_SESSIONS SET $COL_IS_LOGGED_IN = 0")
        val cv = ContentValues().apply {
            put(COL_USER_ID, userId)
            put(COL_IS_LOGGED_IN, 1)
        }
        db.insert(TABLE_SESSIONS, null, cv)
    }

    /** Logout: set semua is_logged_in = 0 */
    fun logout() {
        val db = writableDatabase
        db.execSQL("UPDATE $TABLE_SESSIONS SET $COL_IS_LOGGED_IN = 0")
    }

    /**
     * Cek apakah ada session aktif.
     * Return: Session object jika ada, null jika tidak.
     */
    fun getActiveSession(): Session? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_SESSIONS WHERE $COL_IS_LOGGED_IN = 1 LIMIT 1",
            null
        )
        var session: Session? = null
        if (cursor.moveToFirst()) {
            session = Session(
                id         = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                userId     = cursor.getLong(cursor.getColumnIndexOrThrow(COL_USER_ID)),
                isLoggedIn = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_LOGGED_IN)) == 1,
                timestamp  = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIMESTAMP))
            )
        }
        cursor.close()
        return session
    }

    // =========================================================
    // CATEGORY
    // =========================================================

    /** Ambil semua kategori aktif milik user */
    fun getCategoriesByUser(userId: Long): List<Category> {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_CATEGORIES WHERE $COL_USER_ID = ? AND $COL_IS_ACTIVE = 1 ORDER BY $COL_TIMESTAMP ASC",
            arrayOf(userId.toString())
        )
        val list = mutableListOf<Category>()
        while (cursor.moveToNext()) {
            list.add(cursorToCategory(cursor))
        }
        cursor.close()
        return list
    }

    /**
     * Tambah kategori baru.
     * Return: ID baru jika berhasil, -1 jika nama kategori sudah ada untuk user ini.
     */
    fun addCategory(userId: Long, namaKategori: String): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_USER_ID, userId)
            put(COL_NAMA_KATEGORI, namaKategori.trim())
            put(COL_IS_ACTIVE, 1)
        }
        return db.insertWithOnConflict(TABLE_CATEGORIES, null, cv, SQLiteDatabase.CONFLICT_IGNORE)
    }

    /** Cek apakah nama kategori sudah ada untuk user ini */
    fun isCategoryExist(userId: Long, namaKategori: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_ID FROM $TABLE_CATEGORIES WHERE $COL_USER_ID = ? AND $COL_NAMA_KATEGORI = ? COLLATE NOCASE",
            arrayOf(userId.toString(), namaKategori)
        )
        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }

    private fun cursorToCategory(cursor: android.database.Cursor): Category {
        return Category(
            id            = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
            userId        = cursor.getLong(cursor.getColumnIndexOrThrow(COL_USER_ID)),
            namaKategori  = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAMA_KATEGORI)),
            isActive      = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_ACTIVE)) == 1,
            timestamp     = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIMESTAMP))
        )
    }

    // =========================================================
    // CASHFLOW
    // =========================================================

    /**
     * Insert transaksi baru.
     * Return: ID baru jika berhasil, -1 jika gagal.
     */
    fun insertCashflow(cashflow: Cashflow): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_CATEGORY_ID,    cashflow.categoryId)
            put(COL_USER_ID,        cashflow.userId)
            put(COL_NAMA_TRANSAKSI, cashflow.namaTransaksi)
            put(COL_JENIS_TRANSAKSI, cashflow.jenisTransaksi)
            put(COL_NOMINAL,        cashflow.nominal)
            put(COL_CATATAN,        cashflow.catatan)
            put(COL_TIMESTAMP,      cashflow.timestamp)
        }
        return db.insert(TABLE_CASHFLOWS, null, cv)
    }

    /**
     * Update transaksi.
     * Return: true jika berhasil.
     */
    fun updateCashflow(cashflow: Cashflow): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_CATEGORY_ID,    cashflow.categoryId)
            put(COL_NAMA_TRANSAKSI, cashflow.namaTransaksi)
            put(COL_JENIS_TRANSAKSI, cashflow.jenisTransaksi)
            put(COL_NOMINAL,        cashflow.nominal)
            put(COL_CATATAN,        cashflow.catatan)
            put(COL_TIMESTAMP,      cashflow.timestamp)
        }
        val rows = db.update(TABLE_CASHFLOWS, cv, "$COL_ID = ? AND $COL_USER_ID = ?",
            arrayOf(cashflow.id.toString(), cashflow.userId.toString()))
        return rows > 0
    }

    /**
     * Hapus transaksi.
     * Return: true jika berhasil.
     */
    fun deleteCashflow(cashflowId: Long, userId: Long): Boolean {
        val db = writableDatabase
        val rows = db.delete(TABLE_CASHFLOWS,
            "$COL_ID = ? AND $COL_USER_ID = ?",
            arrayOf(cashflowId.toString(), userId.toString()))
        return rows > 0
    }

    /** Ambil semua transaksi user, optional filter bulan & tahun */
    fun getCashflows(userId: Long, month: Int? = null, year: Int? = null): List<Cashflow> {
        val db = readableDatabase

        val query = buildString {
            append("""
                SELECT cf.*, c.$COL_NAMA_KATEGORI
                FROM $TABLE_CASHFLOWS cf
                JOIN $TABLE_CATEGORIES c ON cf.$COL_CATEGORY_ID = c.$COL_ID
                WHERE cf.$COL_USER_ID = ?
            """.trimIndent())
            if (year != null) append(" AND strftime('%Y', cf.$COL_TIMESTAMP) = '${year}'")
            if (month != null) append(" AND strftime('%m', cf.$COL_TIMESTAMP) = '${month.toString().padStart(2, '0')}'")
            append(" ORDER BY cf.$COL_TIMESTAMP DESC")
        }

        val cursor = db.rawQuery(query, arrayOf(userId.toString()))
        val list = mutableListOf<Cashflow>()
        while (cursor.moveToNext()) {
            list.add(cursorToCashflow(cursor))
        }
        cursor.close()
        return list
    }

    /** Ambil N transaksi terbaru (untuk Recent Activity di Dashboard) */
    fun getRecentCashflows(userId: Long, limit: Int = 5): List<Cashflow> {
        val db = readableDatabase
        val cursor = db.rawQuery("""
            SELECT cf.*, c.$COL_NAMA_KATEGORI
            FROM $TABLE_CASHFLOWS cf
            JOIN $TABLE_CATEGORIES c ON cf.$COL_CATEGORY_ID = c.$COL_ID
            WHERE cf.$COL_USER_ID = ?
            ORDER BY cf.$COL_TIMESTAMP DESC
            LIMIT ?
        """.trimIndent(), arrayOf(userId.toString(), limit.toString()))
        val list = mutableListOf<Cashflow>()
        while (cursor.moveToNext()) {
            list.add(cursorToCashflow(cursor))
        }
        cursor.close()
        return list
    }

    /** Ambil 1 transaksi berdasarkan ID */
    fun getCashflowById(cashflowId: Long): Cashflow? {
        val db = readableDatabase
        val cursor = db.rawQuery("""
            SELECT cf.*, c.$COL_NAMA_KATEGORI
            FROM $TABLE_CASHFLOWS cf
            JOIN $TABLE_CATEGORIES c ON cf.$COL_CATEGORY_ID = c.$COL_ID
            WHERE cf.$COL_ID = ?
        """.trimIndent(), arrayOf(cashflowId.toString()))
        var cashflow: Cashflow? = null
        if (cursor.moveToFirst()) cashflow = cursorToCashflow(cursor)
        cursor.close()
        return cashflow
    }

    /** Hitung total saldo user: total income - total expense */
    fun getSaldo(userId: Long): Double {
        val db = readableDatabase
        val cursor = db.rawQuery("""
            SELECT
                SUM(CASE WHEN $COL_JENIS_TRANSAKSI = 'income' THEN $COL_NOMINAL ELSE 0 END) as total_income,
                SUM(CASE WHEN $COL_JENIS_TRANSAKSI = 'expense' THEN $COL_NOMINAL ELSE 0 END) as total_expense
            FROM $TABLE_CASHFLOWS
            WHERE $COL_USER_ID = ?
        """.trimIndent(), arrayOf(userId.toString()))
        var saldo = 0.0
        if (cursor.moveToFirst()) {
            val income  = cursor.getDouble(0)
            val expense = cursor.getDouble(1)
            saldo = income - expense
        }
        cursor.close()
        return saldo
    }

    /**
     * Ambil ringkasan pengeluaran per kategori bulan ini (untuk pie chart dashboard).
     * Return: Map<namaKategori, totalNominal>
     */
    fun getExpenseSummaryByCategory(userId: Long, month: Int, year: Int): Map<String, Double> {
        val db = readableDatabase
        val cursor = db.rawQuery("""
            SELECT c.$COL_NAMA_KATEGORI, SUM(cf.$COL_NOMINAL) as total
            FROM $TABLE_CASHFLOWS cf
            JOIN $TABLE_CATEGORIES c ON cf.$COL_CATEGORY_ID = c.$COL_ID
            WHERE cf.$COL_USER_ID = ?
              AND cf.$COL_JENIS_TRANSAKSI = 'expense'
              AND strftime('%m', cf.$COL_TIMESTAMP) = '${month.toString().padStart(2, '0')}'
              AND strftime('%Y', cf.$COL_TIMESTAMP) = '$year'
            GROUP BY c.$COL_ID
            ORDER BY total DESC
        """.trimIndent(), arrayOf(userId.toString()))
        val map = linkedMapOf<String, Double>()
        while (cursor.moveToNext()) {
            val nama  = cursor.getString(0)
            val total = cursor.getDouble(1)
            map[nama] = total
        }
        cursor.close()
        return map
    }

    private fun cursorToCashflow(cursor: android.database.Cursor): Cashflow {
        // namaKategori di-join dari tabel categories
        val namaKategoriIndex = cursor.getColumnIndex(COL_NAMA_KATEGORI)
        return Cashflow(
            id              = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
            categoryId      = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CATEGORY_ID)),
            userId          = cursor.getLong(cursor.getColumnIndexOrThrow(COL_USER_ID)),
            namaTransaksi   = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAMA_TRANSAKSI)),
            jenisTransaksi  = cursor.getString(cursor.getColumnIndexOrThrow(COL_JENIS_TRANSAKSI)),
            nominal         = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_NOMINAL)),
            catatan         = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATATAN)),
            timestamp       = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIMESTAMP)),
            namaKategori    = if (namaKategoriIndex >= 0) cursor.getString(namaKategoriIndex) else ""
        )
    }

    // =========================================================
    // SETTING
    // =========================================================

    fun getSetting(userId: Long): Setting? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_SETTINGS WHERE $COL_USER_ID = ?",
            arrayOf(userId.toString())
        )
        var setting: Setting? = null
        if (cursor.moveToFirst()) {
            setting = Setting(
                id                = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                userId            = cursor.getLong(cursor.getColumnIndexOrThrow(COL_USER_ID)),
                limitPengeluaran  = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LIMIT_PENGELUARAN)),
                peringatanAktif   = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PERINGATAN_AKTIF)) == 1,
                timestamp         = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIMESTAMP))
            )
        }
        cursor.close()
        return setting
    }

    fun updateSetting(userId: Long, limitPengeluaran: Double, peringatanAktif: Boolean): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_LIMIT_PENGELUARAN, limitPengeluaran)
            put(COL_PERINGATAN_AKTIF, if (peringatanAktif) 1 else 0)
        }
        val rows = db.update(TABLE_SETTINGS, cv, "$COL_USER_ID = ?", arrayOf(userId.toString()))
        return rows > 0
    }
}