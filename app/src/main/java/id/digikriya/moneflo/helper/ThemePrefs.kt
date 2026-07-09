package id.digikriya.moneflo.helper

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

/**
 * Pengelola preferensi tema (Light / Dark / Ikuti Sistem), tersimpan permanen
 * di SharedPreferences "moneflo_prefs" dan diterapkan lewat AppCompatDelegate
 * supaya berlaku di seluruh Activity tanpa perlu setContentView ulang manual.
 */
object ThemePrefs {

    const val MODE_LIGHT = "light"
    const val MODE_DARK = "dark"
    const val MODE_SYSTEM = "system"

    private const val PREFS_NAME = "moneflo_prefs"
    private const val KEY_THEME_MODE = "theme_mode"

    fun getMode(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_THEME_MODE, MODE_SYSTEM) ?: MODE_SYSTEM
    }

    /** Terapkan mode tema yang tersimpan. Panggil di awal Application.onCreate(). */
    fun applySavedTheme(context: Context) {
        applyMode(getMode(context))
    }

    /** Simpan pilihan mode tema baru lalu langsung terapkan. */
    fun setMode(context: Context, mode: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_THEME_MODE, mode).apply()
        applyMode(mode)
    }

    private fun applyMode(mode: String) {
        val nightMode = when (mode) {
            MODE_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            MODE_DARK  -> AppCompatDelegate.MODE_NIGHT_YES
            else       -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}
