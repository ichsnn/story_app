package com.app.storyapp.customviews

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.Editable
import android.util.AttributeSet
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import com.app.storyapp.R

class StoryAppInputText(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    private var helper: String? = null
    private var label: String? = null
    private var hint: String? = null
    private var minLength: Int = 0
    private var inputType: Int = 1
    private var iconStart: Drawable? = null
    private var editTextBackgroundTint: ColorStateList? = null
    private var isRequired: Boolean = false

    val text get() = inputEditText.text

    private var labelTextView: TextView
    private var helperTextView: TextView
    private var inputEditText: StoryAppInputEditText

    private var onTextChangeAddition: OnTextChangeAddition? = null

    init {
        orientation = VERTICAL

        val view = LayoutInflater.from(context).inflate(R.layout.story_app_input_text, this, true)
        labelTextView = view.findViewById(R.id.tv_label)
        helperTextView = view.findViewById(R.id.tv_helper)
        inputEditText = view.findViewById(R.id.ed_input)

        for (i in 0 until childCount) {
            val v = getChildAt(i)
            removeViewAt(i)
            addView(v)
        }

        context.theme.obtainStyledAttributes(attrs, R.styleable.StoryAppInputText, 0, 0).apply {
            try {
                helper = getString(R.styleable.StoryAppInputText_helper)
                label = getString(R.styleable.StoryAppInputText_label)
                hint = getString(R.styleable.StoryAppInputText_hint)
                minLength = getInt(R.styleable.StoryAppInputText_minLength, 0)
                inputType = getInt(R.styleable.StoryAppInputText_android_inputType, 1)
                iconStart = getDrawable(R.styleable.StoryAppInputText_iconStart)
                isRequired = getBoolean(R.styleable.StoryAppInputText_required, false)
            } finally {
                recycle()
            }
        }

        setLabelText(label)
        setHelperText(helper)
        setHintText(hint)
        setInputType()
        setIconStart()
        setInputOnChange()

        editTextBackgroundTint = inputEditText.backgroundTintList
    }

    private fun setLabelText(label: String?) {
        if (label != null) {
            labelTextView.text = label
            labelTextView.visibility = View.VISIBLE
        } else {
            labelTextView.visibility = View.GONE
        }
    }

    private fun setIconStart() {
        if (iconStart != null) {
            inputEditText.setDrawableStart(iconStart)
        }
    }

    private fun setHelperText(helper: String?) {
        if (helper != null) {
            helperTextView.text = helper
            helperTextView.visibility = View.VISIBLE
        } else {
            helperTextView.visibility = View.GONE
        }
    }

    private fun setHintText(hint: String?) {
        inputEditText.hint = hint
    }

    private fun setInputType() {
        inputEditText.inputType = inputType
    }

    private fun setInputOnChange() {
        inputEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                inputEditText.doAfterTextChanged { s ->
                    if (s.toString().length < minLength) {
                        msgMinLength(hint, minLength)
                    } else
                        if (s.toString().isEmpty()) {
                            msgRequiredField()
                        } else {
                            msgHide()
                        }
                    if (onTextChangeAddition != null) {
                        onTextChangeAddition?.onTextChange(s)
                    }
                }
            }
        }
    }

    fun isFieldEmpty(): Boolean {
        if (text.toString().isEmpty()) {
            msgRequiredField()
            return true
        }
        msgHide()
        return false
    }

    fun isMinLengthNotValid(): Boolean {
        if (text.toString().length < minLength) {
            msgMinLength(hint, minLength)
            return true
        }
        msgHide()
        return false
    }

    fun isEmailValid(): Boolean {
        val patterns = Patterns.EMAIL_ADDRESS
        if (patterns.matcher(text.toString()).matches()) {
            msgHide()
            return true
        }
        msgEmailNotValid()
        return false
    }

    fun msgRequiredField() {
        val message = context.getString(R.string.field_cannot_be_empty)
        setHelperText(message)
        warningHelper()
    }

    fun msgPasswordNotMatch() {
        val message = context.getString(R.string.password_not_match)
        setHelperText(message)
        warningHelper()
    }

    private fun msgEmailNotValid() {
        val message = context.getString(R.string.email_not_valid)
        setHelperText(message)
        warningHelper()
    }

    private fun msgMinLength(hint: String?, minLength: Int) {
        val stringTemplate = context.getString(R.string.field_min_length, hint, minLength)
        setHelperText(stringTemplate)
        warningHelper()
    }

    fun msgHide() {
        setHelperText(null)
        normalHelper()
    }

    private fun warningHelper() {
        inputEditText.backgroundTintList = ColorStateList.valueOf(Color.RED)
        helperTextView.setTextColor(Color.RED)
    }

    private fun normalHelper() {
        inputEditText.backgroundTintList = editTextBackgroundTint
    }

    fun onTextChangeAddition(onTextChangeAddition: OnTextChangeAddition) {
        this.onTextChangeAddition = onTextChangeAddition
    }

    interface OnTextChangeAddition {
        fun onTextChange(s: Editable?)
    }
}