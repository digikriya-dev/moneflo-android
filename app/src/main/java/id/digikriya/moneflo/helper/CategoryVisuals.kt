package id.digikriya.moneflo.helper

import id.digikriya.moneflo.R

/**
 * Mapping ikon & warna per kategori. Kategori bawaan (DEFAULT_CATEGORIES di
 * DatabaseHelper) dapat ikon spesifik; kategori custom buatan user jatuh ke ikon default.
 * Warna diambil berputar dari palet tetap supaya konsisten di pie chart & badge.
 */
object CategoryVisuals {

    private val palette = listOf(
        "#3B4CCA", // indigo
        "#10B981", // emerald
        "#F59E0B", // amber
        "#EF4444", // red
        "#8B5CF6", // violet
        "#06B6D4", // cyan
        "#EC4899", // pink
        "#84CC16", // lime
        "#F97316", // orange
        "#0EA5E9", // sky
        "#A855F7", // purple
        "#14B8A6", // teal
    )

    fun iconFor(namaKategori: String): Int {
        return when (namaKategori.trim().lowercase()) {
            // Kebutuhan Pokok & Tetap
            "tempat tinggal", "kos" -> R.drawable.ic_cat_home
            "tagihan & utilitas" -> R.drawable.ic_cat_bill
            "belanja bulanan" -> R.drawable.ic_cat_groceries
            "transportasi" -> R.drawable.ic_cat_transport
            "kesehatan" -> R.drawable.ic_cat_health
            "pendidikan", "tugas" -> R.drawable.ic_cat_task
            "cicilan & hutang" -> R.drawable.ic_cat_debt
            // Gaya Hidup & Hiburan
            "makanan & minuman", "makan" -> R.drawable.ic_cat_food
            "hiburan & langganan", "hiburan" -> R.drawable.ic_cat_entertainment
            "belanja modis" -> R.drawable.ic_cat_shopping
            "liburan & hobi" -> R.drawable.ic_cat_travel
            "sosial & donasi" -> R.drawable.ic_cat_donation
            // Pemasukan
            "gaji" -> R.drawable.ic_cat_salary
            "sampingan" -> R.drawable.ic_cat_freelance
            "investasi" -> R.drawable.ic_cat_investment
            "bonus & hadiah" -> R.drawable.ic_cat_gift
            // Khusus
            "transfer antar rekening" -> R.drawable.ic_cat_transfer
            "tabungan" -> R.drawable.ic_cat_savings
            else -> R.drawable.ic_cat_default
        }
    }

    fun colorFor(namaKategori: String): Int {
        val index = (namaKategori.trim().lowercase().hashCode().and(Int.MAX_VALUE)) % palette.size
        return android.graphics.Color.parseColor(palette[index])
    }

    fun colorForIndex(index: Int): Int {
        return android.graphics.Color.parseColor(palette[index % palette.size])
    }
}
