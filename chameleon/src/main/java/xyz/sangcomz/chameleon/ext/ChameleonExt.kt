package xyz.sangcomz.chameleon.ext

import android.graphics.drawable.Drawable
import xyz.sangcomz.chameleon.Chameleon
import xyz.sangcomz.chameleon.model.TextSettingBundle

fun Chameleon.setError(
    drawable: Drawable? = null,
    titleTextSettingBundle: TextSettingBundle = TextSettingBundle(),
    subTextSettingBundle: TextSettingBundle = TextSettingBundle()
) = showState(
    Chameleon.STATE.ERROR,
    drawable,
    titleTextSettingBundle,
    subTextSettingBundle
)

fun Chameleon.setLoading() = showState(Chameleon.STATE.LOADING)

fun Chameleon.setEmpty(
    drawable: Drawable? = null,
    titleTextSettingBundle: TextSettingBundle = TextSettingBundle(),
    subTextSettingBundle: TextSettingBundle = TextSettingBundle()
) = showState(
    Chameleon.STATE.EMPTY,
    drawable,
    titleTextSettingBundle,
    subTextSettingBundle
)

fun Chameleon.setContent() = showState(Chameleon.STATE.CONTENT)

fun Chameleon?.setErrorIfEmpty() = this?.let { if (hasNoContent()) setError() }

fun Chameleon?.contentOrEmpty(hasContent: Boolean) = this?.let {
    if (hasContent) setContent() else setEmpty()
}

fun Chameleon?.loadingOrContent(isLoading: Boolean) = this?.let {
    if (isLoading) setLoading() else setContent()
}
