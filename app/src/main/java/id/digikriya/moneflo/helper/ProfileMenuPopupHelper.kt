package id.digikriya.moneflo.helper

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
import id.digikriya.moneflo.R

/**
 * Popup menu profil singkat (Edit Profil / Ubah Password / Logout), dianchor
 * di bawah avatar pojok kiri atas — jalan pintas selain tab Profil.
 */
object ProfileMenuPopupHelper {

    fun show(
        context: Context,
        anchor: View,
        onEditProfil: () -> Unit,
        onUbahPassword: () -> Unit,
        onLogout: () -> Unit
    ) {
        val view = LayoutInflater.from(context).inflate(R.layout.popup_profile_menu, null)

        val popup = PopupWindow(
            view,
            (200 * context.resources.displayMetrics.density).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        popup.isOutsideTouchable = true
        popup.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        popup.elevation = 12f

        view.findViewById<TextView>(R.id.menu_edit_profil).setOnClickListener {
            popup.dismiss()
            onEditProfil()
        }
        view.findViewById<TextView>(R.id.menu_ubah_password).setOnClickListener {
            popup.dismiss()
            onUbahPassword()
        }
        view.findViewById<TextView>(R.id.menu_logout).setOnClickListener {
            popup.dismiss()
            onLogout()
        }

        val yOffset = (8 * context.resources.displayMetrics.density).toInt()
        popup.showAsDropDown(anchor, 0, yOffset)
    }
}
