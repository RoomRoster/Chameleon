package xyz.sangcomz.chameleon.ext

import android.content.Context

/**
 * Created by seokwon.jeong on 16/11/2017.
 */
internal fun Int.DP(context: Context): Float = this * context.resources.displayMetrics.density

internal fun Int.SP(context: Context): Float = this * context.resources.displayMetrics.scaledDensity

