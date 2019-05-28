package xyz.sangcomz.chameleon.model

import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.view.View

data class ButtonSettingBundle(
    val text: String? = null,
    val textSize: Float? = null,
    val textColor: Int? = null,
    @ColorInt val backgroundColor: Int? = null,
    @DrawableRes val backgroundRes: Int? = null,
    val listener: ((View) -> Unit)? = null
)