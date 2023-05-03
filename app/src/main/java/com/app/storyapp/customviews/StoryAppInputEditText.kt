package com.app.storyapp.customviews

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.InputType
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.app.storyapp.R

class StoryAppInputEditText : AppCompatEditText, View.OnTouchListener {
    private lateinit var visibilityIcon: Drawable
    private lateinit var visibilityOffIcon: Drawable
    private var drawableEnd: Drawable? = null
    private var drawableStart: Drawable? = null
    private var isPasswordVisible = false

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        init()
    }

    override fun setInputType(type: Int) {
        super.setInputType(type)
        showTogglePassword()
    }

    private fun init() {
        showTogglePassword()
        setOnTouchListener(this)
    }

    private fun showTogglePassword() {
        if (inputType == InputType.TYPE_TEXT_VARIATION_PASSWORD + 1 || inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD + 1) {
            visibilityIcon =
                ContextCompat.getDrawable(context, R.drawable.ic_visibility) as Drawable
            visibilityOffIcon =
                ContextCompat.getDrawable(context, R.drawable.ic_visibility_off) as Drawable
        }
        when (inputType) {
            InputType.TYPE_TEXT_VARIATION_PASSWORD + 1 -> drawableEnd = visibilityIcon
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD + 1 -> drawableEnd = visibilityOffIcon
        }
        setButtonDrawable()
    }

    fun setDrawableStart(drawable: Drawable?) {
        drawableStart = drawable
        setButtonDrawable()
    }

    private fun setButtonDrawable(
    ) {
        setCompoundDrawablesWithIntrinsicBounds(
            drawableStart, null, drawableEnd, null
        )
    }

    private fun setPasswordVisibility(value: Boolean) {
        isPasswordVisible = value
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        if (compoundDrawables[2] != null) {
            if (inputType == InputType.TYPE_TEXT_VARIATION_PASSWORD + 1 || inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD + 1) {
                val passwordVisibilityButtonStart: Float
                val passwordVisibilityButtonEnd: Float
                var isToggleClicked = false
                if (layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                    passwordVisibilityButtonEnd =
                        if (inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD + 1) (visibilityOffIcon.intrinsicWidth + paddingStart).toFloat()
                        else (visibilityIcon.intrinsicWidth + paddingStart).toFloat()
                    when {
                        event.x < passwordVisibilityButtonEnd -> isToggleClicked = true
                    }
                } else {
                    passwordVisibilityButtonStart =
                        if (inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD + 1) (width - paddingEnd - visibilityOffIcon.intrinsicWidth).toFloat()
                        else (width - paddingEnd - visibilityOffIcon.intrinsicWidth).toFloat()
                    when {
                        event.x > passwordVisibilityButtonStart -> isToggleClicked = true
                    }
                }
                if (isToggleClicked) {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            setPasswordVisibility(!isPasswordVisible)
                            inputType =
                                if (inputType == InputType.TYPE_TEXT_VARIATION_PASSWORD + 1) InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD + 1 else InputType.TYPE_TEXT_VARIATION_PASSWORD + 1
                            showTogglePassword()
                            return true
                        }
                    }
                }
            }
        }
        return false
    }
}