package id.digikriya.moneflo.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import java.io.File
import java.io.FileOutputStream

/**
 * Helper foto profil: simpan file foto lokal di internal storage app (tidak upload
 * kemana-mana) dan tampilkan sebagai avatar bulat tanpa perlu library image-loading.
 */
object PhotoHelper {

    private const val MAX_DIMENSION = 512

    /** Salin foto yang dipilih user dari [uri] ke internal storage, return path lokal atau null jika gagal. */
    fun saveFromUri(context: Context, uri: Uri, userId: Long): String? {
        return try {
            val input = context.contentResolver.openInputStream(uri) ?: return null
            val original = BitmapFactory.decodeStream(input)
            input.close()
            if (original == null) return null

            val scaled = scaleDown(original, MAX_DIMENSION)
            val file = File(context.filesDir, "profile_$userId.jpg")
            FileOutputStream(file).use { out ->
                scaled.compress(Bitmap.CompressFormat.JPEG, 88, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    private fun scaleDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val ratio = maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
        if (ratio >= 1f) return bitmap
        val width = (bitmap.width * ratio).toInt()
        val height = (bitmap.height * ratio).toInt()
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    /** Tampilkan foto di [imageView] sebagai avatar bulat. Return true jika berhasil (file ada & valid). */
    fun loadCircular(imageView: ImageView, path: String?): Boolean {
        if (path.isNullOrEmpty()) return false
        val file = File(path)
        if (!file.exists()) return false

        val bitmap = BitmapFactory.decodeFile(path) ?: return false
        val rounded = RoundedBitmapDrawableFactory.create(imageView.resources, bitmap).apply {
            isCircular = true
        }
        imageView.setImageDrawable(rounded)
        return true
    }
}
