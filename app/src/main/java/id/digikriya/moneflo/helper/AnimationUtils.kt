package id.digikriya.moneflo.helper

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator

fun Activity.playEntranceAnimation() {
    val root = findViewById<ViewGroup>(android.R.id.content).getChildAt(0) ?: return
    root.alpha = 0f
    root.translationY = 60f
    AnimatorSet().apply {
        playTogether(
            ObjectAnimator.ofFloat(root, "alpha", 0f, 1f),
            ObjectAnimator.ofFloat(root, "translationY", 60f, 0f)
        )
        duration = 400
        interpolator = DecelerateInterpolator()
        start()
    }
}
