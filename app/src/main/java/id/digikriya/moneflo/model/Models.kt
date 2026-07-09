package id.digikriya.moneflo.model

data class User(
    val id: Long = 0,
    val username: String,
    val email: String,
    val password: String,
    val namaLengkap: String,
    val fotoProfil: String? = null,
    val isGoogleAccount: Boolean = false,
    val timestamp: String = ""
)

data class Session(
    val id: Long = 0,
    val userId: Long,
    val isLoggedIn: Boolean,
    val timestamp: String = ""
)

data class Category(
    val id: Long = 0,
    val userId: Long,
    val namaKategori: String,
    val isActive: Boolean = true,
    val timestamp: String = ""
)

data class Setting(
    val id: Long = 0,
    val userId: Long,
    val limitPengeluaran: Double = 0.0,
    val peringatanAktif: Boolean = false,
    val timestamp: String = ""
)

data class Cashflow(
    val id: Long = 0,
    val categoryId: Long,
    val userId: Long,
    val namaTransaksi: String,
    val jenisTransaksi: String,   // "income" | "expense"
    val nominal: Double,
    val catatan: String? = null,
    val timestamp: String = "",
    // Join field dari tabel categories (tidak disimpan di cashflows)
    val namaKategori: String = ""
)

data class Tag(
    val id: Long = 0,
    val userId: Long,
    val namaTag: String,
    val timestamp: String = ""
)