package id.digikriya.moneflo.helper

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** Label section header relatif (Hari Ini / Kemarin / tanggal lengkap) untuk timestamp "yyyy-MM-dd HH:mm:ss". */
object DateGroupHelper {

    private val sdfFull = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val sdfDisplay = SimpleDateFormat("dd MMMM yyyy", Locale("id"))

    fun label(timestamp: String): String {
        val date = try { sdfFull.parse(timestamp) } catch (e: Exception) { null } ?: return timestamp

        val today = Calendar.getInstance()
        val target = Calendar.getInstance().apply { time = date }

        val diffDays = daysBetween(target, today)
        return when (diffDays) {
            0 -> "Hari Ini"
            1 -> "Kemarin"
            else -> sdfDisplay.format(date)
        }
    }

    private fun daysBetween(a: Calendar, b: Calendar): Int {
        val aClean = truncate(a)
        val bClean = truncate(b)
        val diffMillis = bClean.timeInMillis - aClean.timeInMillis
        return (diffMillis / (1000 * 60 * 60 * 24)).toInt()
    }

    private fun truncate(cal: Calendar): Calendar {
        return (cal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
}
