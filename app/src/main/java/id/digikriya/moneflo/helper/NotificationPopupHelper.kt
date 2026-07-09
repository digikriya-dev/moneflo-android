package id.digikriya.moneflo.helper

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
import id.digikriya.moneflo.R

/**
 * Popup notifikasi sederhana, dianchor di bawah tombol bell. Karena app ini belum
 * punya sistem notifikasi nyata, popup selalu menampilkan empty state — tinggal
 * ganti isi [notifications] kalau fitur notifikasi sungguhan sudah ada.
 */
object NotificationPopupHelper {

    fun show(context: Context, anchor: View, notifications: List<String> = emptyList()) {
        val view = LayoutInflater.from(context).inflate(R.layout.popup_notification, null)
        val tvContent = view.findViewById<TextView>(R.id.tv_notification_content)

        tvContent.text = if (notifications.isEmpty()) {
            "Tidak ada notifikasi saat ini."
        } else {
            notifications.joinToString("\n\n")
        }

        val density = context.resources.displayMetrics.density
        val popupWidthPx = (280 * density).toInt()

        val popup = PopupWindow(
            view,
            popupWidthPx,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        popup.isOutsideTouchable = true
        popup.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        popup.elevation = 12f

        // Sejajarkan sisi kanan popup dengan sisi kanan anchor (bell), supaya
        // popup mengembang ke kiri-bawah dari tombol, bukan nempel di kiri layar.
        val xOffset = -(popupWidthPx - anchor.width)
        val yOffset = (8 * density).toInt()
        popup.showAsDropDown(anchor, xOffset, yOffset)
    }
}
