package com.baset.switchview

import android.content.Context
import android.util.DisplayMetrics
import androidx.annotation.ColorRes
import androidx.annotation.Dimension
import androidx.core.content.ContextCompat

class Utils(private val context: Context) {
    fun convertDPtoPX(@Dimension(unit = Dimension.DP) value: Float): Int {
        return value.toInt() * context.resources
            .displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT
    }

    fun getColorResource(@ColorRes color: Int): Int {
        return ContextCompat.getColor(context, color)
    }

}