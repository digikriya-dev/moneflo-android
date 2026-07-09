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
    )

    fun iconFor(namaKategori: String): Int {
        return when (namaKategori.trim().lowercase()) {
            "makan" -> R.drawable.ic_cat_food
            "transportasi" -> R.drawable.ic_cat_transport
            "kos" -> R.drawable.ic_cat_home
            "tugas" -> R.drawable.ic_cat_task
            "hiburan" -> R.drawable.ic_cat_entertainment
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
