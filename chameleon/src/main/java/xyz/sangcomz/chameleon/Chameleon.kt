package xyz.sangcomz.chameleon

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.constraint.ConstraintSet.*
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatButton
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity.CENTER
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import xyz.sangcomz.chameleon.ext.DP
import xyz.sangcomz.chameleon.ext.getDrawable
import xyz.sangcomz.chameleon.model.ButtonSettingBundle
import xyz.sangcomz.chameleon.model.ChameleonAttr
import xyz.sangcomz.chameleon.model.TextSettingBundle

/**
 * Created by sangcomz on 12/02/2018.
 */
open class Chameleon(context: Context?, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {

    enum class STATE {
        LOADING,
        ERROR,
        EMPTY,
        NONE,
        CONTENT
    }

    private var stateContentView: View? = null
    private var stateImageView: AppCompatImageView? = null
    private var stateTitleTextView: AppCompatTextView? = null
    private var stateSubTextView: AppCompatTextView? = null
    private var stateProgressLayout: FrameLayout? = null
    private var stateButton: AppCompatButton? = null
    private var errorButtonListener: ((View) -> Unit)? = null
    private var emptyButtonListener: ((View) -> Unit)? = null
    private var noneButtonListener: ((View) -> Unit)? = null
    private var stateChangeListener: ((newState: STATE, oldState: STATE) -> Unit)? = null

    private var chameleonAttr: ChameleonAttr? = null
    private var currentState: STATE = STATE.EMPTY

    init {
        attrs?.let {
            val a = context?.theme?.obtainStyledAttributes(
                attrs,
                R.styleable.Chameleon,
                0, 0
            )

            a?.let {
                chameleonAttr =
                    ChameleonAttr(
                        buttonBackgroundRes = it.getResourceId(R.styleable.Chameleon_buttonBackground, -1),
                        emptyText = it.getString(R.styleable.Chameleon_emptyText) ?: "empty",
                        emptyTextColor = it.getColor(
                            R.styleable.Chameleon_emptyTextColor,
                            ContextCompat.getColor(context, R.color.colorTitleText)
                        ),
                        emptyTextSize = it.getDimension(
                            R.styleable.Chameleon_emptyTextSize,
                            context.resources.getDimension(R.dimen.title_text_size)
                        ),
                        emptyTextStyle = it.getInt(R.styleable.Chameleon_emptyTextStyle, 0),
                        emptyTextGravity = it.getInt(R.styleable.Chameleon_emptyTextGravity, 0),
                        emptySubText = it.getString(R.styleable.Chameleon_emptySubText)
                            ?: "empty content",
                        emptySubTextColor = it.getColor(
                            R.styleable.Chameleon_emptySubTextColor,
                            ContextCompat.getColor(context, R.color.colorSubText)
                        ),
                        emptySubTextSize = it.getDimension(
                            R.styleable.Chameleon_emptySubTextSize,
                            context.resources.getDimension(R.dimen.sub_text_size)
                        ),
                        emptySubTextGravity = it.getInt(R.styleable.Chameleon_emptySubTextGravity, 0),
                        emptyDrawable = it.getResourceId(
                            R.styleable.Chameleon_emptyDrawable,
                            R.drawable.ic_chameleon_empty
                        ).getDrawable(context),
                        emptyButtonText = it.getString(R.styleable.Chameleon_emptyButtonText)
                            ?: "retry",
                        emptyButtonTextColor = it.getColor(
                            R.styleable.Chameleon_emptyButtonTextColor,
                            ContextCompat.getColor(context, R.color.colorTitleText)
                        ),
                        emptyButtonTextSize = it.getDimension(
                            R.styleable.Chameleon_emptyButtonTextSize,
                            context.resources.getDimension(R.dimen.title_text_size)
                        ),
                        emptyButtonBackgroundColor = it.getColor(
                            R.styleable.Chameleon_emptyButtonBackgroundColor,
                            ContextCompat.getColor(context, R.color.colorSubText)
                        ),
                        useEmptyButton = it.getBoolean(R.styleable.Chameleon_useEmptyButton, false),
                        displayNoneState = it.getBoolean(R.styleable.Chameleon_displayNoneState, false),
                        noneText = it.getString(R.styleable.Chameleon_noneText) ?: "",
                        noneTextColor = it.getColor(
                            R.styleable.Chameleon_noneTextColor,
                            ContextCompat.getColor(context, R.color.colorTitleText)
                        ),
                        noneTextSize = it.getDimension(
                            R.styleable.Chameleon_noneTextSize,
                            context.resources.getDimension(R.dimen.title_text_size)
                        ),
                        noneTextStyle = it.getInt(R.styleable.Chameleon_noneTextStyle, 0),
                        noneTextGravity = it.getInt(R.styleable.Chameleon_noneTextGravity, 0),
                        noneSubText = it.getString(R.styleable.Chameleon_noneSubText) ?: "",
                        noneSubTextColor = it.getColor(
                            R.styleable.Chameleon_noneSubTextColor,
                            ContextCompat.getColor(context, R.color.colorSubText)
                        ),
                        noneSubTextSize = it.getDimension(
                            R.styleable.Chameleon_noneSubTextSize,
                            context.resources.getDimension(R.dimen.sub_text_size)
                        ),
                        noneSubTextGravity = it.getInt(R.styleable.Chameleon_noneSubTextGravity, 0),
                        noneDrawable = it.getResourceId(
                            R.styleable.Chameleon_noneDrawable,
                            R.drawable.ic_chameleon_empty
                        ).getDrawable(context),
                        noneButtonText = it.getString(R.styleable.Chameleon_noneButtonText)
                            ?: "retry",
                        noneButtonTextColor = it.getColor(
                            R.styleable.Chameleon_noneButtonTextColor,
                            ContextCompat.getColor(context, R.color.colorTitleText)
                        ),
                        noneButtonTextSize = it.getDimension(
                            R.styleable.Chameleon_noneButtonTextSize,
                            context.resources.getDimension(R.dimen.title_text_size)
                        ),
                        noneButtonBackgroundColor = it.getColor(
                            R.styleable.Chameleon_noneButtonBackgroundColor,
                            ContextCompat.getColor(context, R.color.colorSubText)
                        ),
                        useNoneButton = it.getBoolean(R.styleable.Chameleon_useNoneButton, false),
                        errorText = it.getString(R.styleable.Chameleon_errorText) ?: "error",
                        errorTextColor = it.getColor(
                            R.styleable.Chameleon_errorTextColor,
                            ContextCompat.getColor(context, R.color.colorTitleText)
                        ),
                        errorTextSize = it.getDimension(
                            R.styleable.Chameleon_errorTextSize,
                            context.resources.getDimension(R.dimen.title_text_size)
                        ),
                        errorTextStyle = it.getInt(R.styleable.Chameleon_errorTextStyle, 0),
                        errorTextGravity = it.getInt(R.styleable.Chameleon_errorTextGravity, 0),
                        errorSubText = it.getString(R.styleable.Chameleon_errorSubText)
                            ?: "error content",
                        errorSubTextColor = it.getColor(
                            R.styleable.Chameleon_errorSubTextColor,
                            ContextCompat.getColor(context, R.color.colorSubText)
                        ),
                        errorSubTextSize = it.getDimension(
                            R.styleable.Chameleon_errorSubTextSize,
                            context.resources.getDimension(R.dimen.sub_text_size)
                        ),
                        errorSubTextGravity = it.getInt(R.styleable.Chameleon_errorSubTextGravity, 0),
                        errorDrawable = it.getResourceId(
                            R.styleable.Chameleon_errorDrawable,
                            R.drawable.ic_chameleon_error
                        ).getDrawable(context),
                        errorButtonText = it.getString(R.styleable.Chameleon_errorButtonText)
                            ?: "retry",
                        errorButtonTextColor = it.getColor(
                            R.styleable.Chameleon_errorButtonTextColor,
                            ContextCompat.getColor(context, R.color.colorTitleText)
                        ),
                        errorButtonTextSize = it.getDimension(
                            R.styleable.Chameleon_errorButtonTextSize,
                            context.resources.getDimension(R.dimen.title_text_size)
                        ),
                        errorButtonBackgroundColor = it.getColor(
                            R.styleable.Chameleon_errorButtonBackgroundColor,
                            ContextCompat.getColor(context, R.color.colorTitleText)
                        ),
                        useErrorButton = it.getBoolean(R.styleable.Chameleon_useErrorButton, false),
                        progressDrawable = it.getDrawable(R.styleable.Chameleon_progressDrawable),
                        useProgressBackground = it.getBoolean(R.styleable.Chameleon_useProgressBackground, false),
                        progressBackgroundColor = it.getColor(
                            R.styleable.Chameleon_progressBackgroundColor,
                            ContextCompat.getColor(context, R.color.colorLoadingBackground)
                        ),
                        isShowProgressWhenContentState = it.getBoolean(
                            R.styleable.Chameleon_isShowContentWhenLoadingState,
                            false
                        ),
                        isLargeProgress = it.getBoolean(R.styleable.Chameleon_isLargeProgress, false),
                        defaultChameleonState = stateFromInt(it.getInt(R.styleable.Chameleon_defaultChameleonState, -1))
                    )
            }

        }
    }

    /**
     * Get the matching state from the view attribute value
     * If the `defaultState` is not set by the developer,
     * then by default `STATE.CONTENT` will be set
     *
     * @param int - Value from the attribute
     * @return State to set
     */
    private fun stateFromInt(int: Int): STATE = when (int) {
        1 -> STATE.LOADING
        2 -> STATE.ERROR
        3 -> STATE.EMPTY
        4 -> STATE.NONE
        else -> STATE.CONTENT
    }

    override fun addView(child: View?) {
        checkValid(child)
        super.addView(child)
    }

    override fun addView(child: View?, index: Int) {
        checkValid(child)
        super.addView(child, index)
    }

    override fun addView(child: View?, width: Int, height: Int) {
        checkValid(child)
        super.addView(child, width, height)
    }

    override fun addView(child: View?, params: ViewGroup.LayoutParams?) {
        checkValid(child)
        super.addView(child, params)
    }

    private fun addStateView() {
        chameleonAttr?.let {
            initStateImageViewView()
            initStateTitleTextView()
            initStateSubTextView()
            initStateButton(it)
            initStateProgressBar(it)

            showState(it.defaultChameleonState)
        }

        ConstraintSet().apply {
            clone(this@Chameleon)
            connect(R.id.iv_state, TOP, PARENT_ID, TOP)
            connect(R.id.iv_state, START, PARENT_ID, START)
            connect(R.id.iv_state, BOTTOM, R.id.tv_title_state, TOP, 4.DP(context).toInt())
            connect(R.id.iv_state, END, PARENT_ID, END)
            setVerticalBias(R.id.iv_state, 1f)

            connect(R.id.tv_title_state, TOP, PARENT_ID, TOP)
            connect(R.id.tv_title_state, START, PARENT_ID, START)
            connect(R.id.tv_title_state, BOTTOM, PARENT_ID, BOTTOM)
            connect(R.id.tv_title_state, END, PARENT_ID, END)

            connect(R.id.tv_sub_state, TOP, R.id.tv_title_state, BOTTOM, 4.DP(context).toInt())
            connect(R.id.tv_sub_state, START, PARENT_ID, START)
            connect(R.id.tv_sub_state, END, PARENT_ID, END)

            connect(R.id.bt_state, TOP, R.id.tv_sub_state, BOTTOM, 16.DP(context).toInt())
            connect(R.id.bt_state, START, PARENT_ID, START)
            connect(R.id.bt_state, BOTTOM, PARENT_ID, BOTTOM)
            connect(R.id.bt_state, END, PARENT_ID, END)
            setVerticalBias(R.id.bt_state, 0f)
            applyTo(this@Chameleon)
        }
    }

    private fun checkValid(child: View?) {
        if (childCount > 0) {
            throw  IllegalStateException("Chameleon can host only one direct child")
        }
        child?.let {
            stateContentView = it
        }

        addStateView()
    }

    private fun initStateImageViewView() {
        stateImageView = AppCompatImageView(context).apply {
            id = R.id.iv_state
            visibility = View.GONE
        }
        val background = stateImageView?.background
        if (background is AnimationDrawable) {
            background.start()
        }
        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            startToStart = LayoutParams.PARENT_ID
            endToEnd = LayoutParams.PARENT_ID
            verticalChainStyle = LayoutParams.CHAIN_PACKED
        }
        super.addView(stateImageView, layoutParams)
    }

    private fun initStateTitleTextView() {
        val padding = 32.DP(context).toInt()
        stateTitleTextView = AppCompatTextView(context)
        stateTitleTextView?.apply {
            id = R.id.tv_title_state
            setPadding(padding, 0, padding, 0)
            ellipsize = TextUtils.TruncateAt.END
            visibility = View.GONE
        }
        val layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        layoutParams.startToStart = LayoutParams.PARENT_ID
        layoutParams.endToEnd = LayoutParams.PARENT_ID
        super.addView(stateTitleTextView, layoutParams)
    }

    private fun initStateSubTextView() {
        val padding = 32.DP(context).toInt()
        stateSubTextView = AppCompatTextView(context)
        stateSubTextView?.apply {
            id = R.id.tv_sub_state
            setPadding(padding, 0, padding, 0)
            ellipsize = TextUtils.TruncateAt.END
            visibility = View.GONE
        }
        val layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        layoutParams.startToStart = LayoutParams.PARENT_ID
        layoutParams.endToEnd = LayoutParams.PARENT_ID
        super.addView(stateSubTextView, layoutParams)
    }

    private fun initStateProgressBar(attr: ChameleonAttr) {
        stateProgressLayout = FrameLayout(context)
        stateProgressLayout?.apply {
            id = R.id.pb_state
            if (attr.useProgressBackground) setBackgroundColor(attr.progressBackgroundColor)
            visibility = View.GONE
        }
        val stateProgressBar =
            if (attr.isLargeProgress)
                ProgressBar(context, null, android.R.attr.progressBarStyleLarge)
            else ProgressBar(context)

        chameleonAttr?.progressDrawable?.let { stateProgressBar.indeterminateDrawable = it }

        val progressBarLayoutParams = FrameLayout.LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        progressBarLayoutParams.gravity = CENTER

        stateProgressLayout?.addView(stateProgressBar, progressBarLayoutParams)
        val layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
        super.addView(stateProgressLayout, layoutParams)
    }

    private fun initStateButton(attr: ChameleonAttr) {
        stateButton = AppCompatButton(context)

        stateButton?.apply {
            id = R.id.bt_state
            text = attr.errorButtonText
            setTextColor(attr.errorButtonTextColor)
            textSize = attr.errorButtonTextSize
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            visibility = View.GONE
        }
        val layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        layoutParams.startToStart = LayoutParams.PARENT_ID
        layoutParams.endToEnd = LayoutParams.PARENT_ID
        super.addView(stateButton, layoutParams)
    }

    fun showState(
        state: STATE,
        customDrawable: Drawable? = null,
        titleTextSettingBundle: TextSettingBundle = TextSettingBundle(),
        subTextSettingBundle: TextSettingBundle = TextSettingBundle(),
        buttonSettingBundle: ButtonSettingBundle = ButtonSettingBundle()
    ) {
        when (state) {
            STATE.CONTENT -> {
                setViewVisibility(View.VISIBLE)
            }
            STATE.ERROR -> {
                chameleonAttr?.let {
                    val errorDrawable = customDrawable ?: it.errorDrawable
                    setStateImageView(
                        errorDrawable
                            ?: R.drawable.ic_chameleon_error.getDrawable(context)
                    )

                    setStateTitleTextView(
                        titleTextSettingBundle.text ?: it.errorText,
                        titleTextSettingBundle.textSize ?: it.errorTextSize,
                        titleTextSettingBundle.textColor ?: it.errorTextColor,
                        titleTextSettingBundle.textGravity ?: it.errorTextGravity,
                        titleTextSettingBundle.textStyle ?: it.errorTextStyle
                    )

                    setStateSubTextView(
                        subTextSettingBundle.text ?: it.errorSubText,
                        subTextSettingBundle.textSize ?: it.errorSubTextSize,
                        subTextSettingBundle.textColor ?: it.errorSubTextColor,
                        subTextSettingBundle.textGravity ?: it.errorSubTextGravity
                    )

                    if (it.useErrorButton)
                        setStateButton(
                            buttonSettingBundle.text ?: it.errorButtonText,
                            buttonSettingBundle.textSize ?: it.errorButtonTextSize,
                            buttonSettingBundle.textColor ?: it.errorButtonTextColor,
                            buttonSettingBundle.backgroundColor
                                ?: it.errorButtonBackgroundColor,
                            buttonSettingBundle.backgroundRes ?: it.buttonBackgroundRes,
                            buttonSettingBundle.listener ?: errorButtonListener
                        )

                    setViewVisibility(
                        imageViewVisible = View.VISIBLE,
                        titleViewVisible = View.VISIBLE,
                        subViewVisible = View.VISIBLE,
                        retryViewVisible = if (it.useEmptyButton) View.VISIBLE else View.GONE
                    )
                }
            }
            STATE.LOADING -> {
                chameleonAttr?.let {
                    setViewVisibility(
                        progressViewVisible = View.VISIBLE,
                        contentViewVisible =
                        if (it.isShowProgressWhenContentState && (currentState == STATE.CONTENT))
                            View.VISIBLE
                        else
                            View.GONE
                    )
                }
            }
            STATE.EMPTY -> {
                chameleonAttr?.let {
                    val emptyDrawable = customDrawable ?: it.emptyDrawable
                    setStateImageView(
                        emptyDrawable
                            ?: R.drawable.ic_chameleon_empty.getDrawable(context)
                    )

                    setStateTitleTextView(
                        titleTextSettingBundle.text ?: it.emptyText,
                        titleTextSettingBundle.textSize ?: it.emptyTextSize,
                        titleTextSettingBundle.textColor ?: it.emptyTextColor,
                        titleTextSettingBundle.textGravity ?: it.emptyTextGravity,
                        titleTextSettingBundle.textStyle ?: it.emptyTextStyle
                    )

                    setStateSubTextView(
                        subTextSettingBundle.text ?: it.emptySubText,
                        subTextSettingBundle.textSize ?: it.emptySubTextSize,
                        subTextSettingBundle.textColor ?: it.emptySubTextColor,
                        subTextSettingBundle.textGravity ?: it.emptySubTextGravity
                    )

                    if (it.useEmptyButton)
                        setStateButton(
                            buttonSettingBundle.text ?: it.emptyButtonText,
                            buttonSettingBundle.textSize ?: it.emptyButtonTextSize,
                            buttonSettingBundle.textColor ?: it.emptyButtonTextColor,
                            buttonSettingBundle.backgroundColor
                                ?: it.emptyButtonBackgroundColor,
                            buttonSettingBundle.backgroundRes ?: it.buttonBackgroundRes,
                            buttonSettingBundle.listener ?: emptyButtonListener
                        )

                    setViewVisibility(
                        imageViewVisible = View.VISIBLE,
                        titleViewVisible = View.VISIBLE,
                        subViewVisible = View.VISIBLE,
                        retryViewVisible = if (it.useEmptyButton) View.VISIBLE else View.GONE
                    )
                }
            }
            STATE.NONE -> if (chameleonAttr?.displayNoneState == true) {
                val attr = chameleonAttr!!
                val noneDrawable = customDrawable ?: attr.noneDrawable
                setStateImageView(
                    noneDrawable
                        ?: R.drawable.ic_chameleon_empty.getDrawable(context)
                )

                setStateTitleTextView(
                    titleTextSettingBundle.text ?: attr.noneText,
                    titleTextSettingBundle.textSize ?: attr.noneTextSize,
                    titleTextSettingBundle.textColor ?: attr.noneTextColor,
                    titleTextSettingBundle.textGravity ?: attr.noneTextGravity,
                    titleTextSettingBundle.textStyle ?: attr.noneTextStyle
                )

                setStateSubTextView(
                    subTextSettingBundle.text ?: attr.noneSubText,
                    subTextSettingBundle.textSize ?: attr.noneSubTextSize,
                    subTextSettingBundle.textColor ?: attr.noneSubTextColor,
                    subTextSettingBundle.textGravity ?: attr.noneSubTextGravity
                )

                if (attr.useNoneButton) {
                    setStateButton(
                        buttonSettingBundle.text ?: attr.noneButtonText,
                        buttonSettingBundle.textSize ?: attr.noneButtonTextSize,
                        buttonSettingBundle.textColor ?: attr.noneButtonTextColor,
                        buttonSettingBundle.backgroundColor ?: attr.noneButtonBackgroundColor,
                        buttonSettingBundle.backgroundRes ?: attr.buttonBackgroundRes,
                        buttonSettingBundle.listener ?: noneButtonListener
                    )
                }

                setViewVisibility(
                    imageViewVisible = View.VISIBLE,
                    titleViewVisible = View.VISIBLE,
                    subViewVisible = View.VISIBLE,
                    retryViewVisible = if (attr.useNoneButton) View.VISIBLE else View.GONE
                )
            } else {
                setViewVisibility()
            }
        }

        if (state != currentState) {
            stateChangeListener?.invoke(state, currentState)
        }
        currentState = state
    }

    fun getState(): STATE = currentState

    fun hasNoContent(): Boolean {
        return (stateContentView as? RecyclerView)?.adapter?.itemCount == 0
    }

    private fun setViewVisibility(
        contentViewVisible: Int = View.GONE,
        imageViewVisible: Int = View.GONE,
        titleViewVisible: Int = View.GONE,
        subViewVisible: Int = View.GONE,
        progressViewVisible: Int = View.GONE,
        retryViewVisible: Int = View.GONE
    ) {
        stateContentView?.visibility = contentViewVisible
        stateImageView?.visibility = imageViewVisible
        stateTitleTextView?.visibility = titleViewVisible
        stateSubTextView?.visibility = subViewVisible

        stateProgressLayout?.visibility = progressViewVisible
        if (progressViewVisible == View.VISIBLE) stateProgressLayout?.bringToFront()

        stateButton?.visibility = retryViewVisible
    }

    private fun setStateImageView(drawable: Drawable?) {
        stateImageView?.setImageDrawable(drawable)
    }

    private fun setStateTitleTextView(
        content: String,
        size: Float,
        color: Int,
        gravity: Int,
        style: Int
    ) {
        stateTitleTextView?.apply {
            this.gravity = gravity
            text = content
            setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
            setTextColor(color)
            setTypeface(typeface, style)
        }
    }

    private fun setStateSubTextView(content: String, size: Float, color: Int, gravity: Int) {
        stateSubTextView?.apply {
            this.gravity = gravity
            text = content
            setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
            setTextColor(color)
        }
    }

    fun modifyStateButton(block: AppCompatButton.() -> Unit) {
        stateButton?.apply(block)
    }

    private fun setStateButton(
        content: String,
        size: Float,
        textColor: Int,
        backgroundColor: Int,
        backgroundRes: Int,
        listener: ((View) -> Unit)?
    ) {
        stateButton?.apply {
            text = content
            setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
            setTextColor(textColor)
            setBackgroundColor(backgroundColor)
            if (backgroundRes > -1) {
                setBackgroundResource(backgroundRes)
            }
            setOnClickListener(listener)
        }
    }

    fun setErrorButtonClickListener(clickListener: (View) -> Unit) {
        errorButtonListener = clickListener
    }

    fun setEmptyButtonClickListener(clickListener: (View) -> Unit) {
        emptyButtonListener = clickListener
    }

    fun setStateChangeListener(listener: ((newState: STATE, oldState: STATE) -> Unit)) {
        stateChangeListener = listener
    }
}