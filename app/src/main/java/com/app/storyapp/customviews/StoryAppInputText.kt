package com.app.storyapp.customviews

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
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

    var labelTextView: TextView
    var helperTextView: TextView
    var inputEditText: StoryAppInputEditText

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

        Log.d("TAGSTORYAPP", "init $label")

        setLabelText(label)
        setHelperText(helper)
        setHintText(hint)
        setInputType()
        setIconStart()
        setInputOnChange()

        editTextBackgroundTint = inputEditText.backgroundTintList
    }

    fun setLabelText(label: String?) {
        if (label != null) {
            labelTextView.text = label
            labelTextView.visibility = View.VISIBLE
        } else {
            labelTextView.visibility = View.GONE
        }
    }

    fun setIconStart() {
        if (iconStart != null) {
            inputEditText.setDrawableStart(iconStart)
        }
    }

    fun setHelperText(helper: String?) {

        if (helper != null) {
            helperTextView.text = helper
            helperTextView.visibility = View.VISIBLE
        } else {
            helperTextView.visibility = View.GONE
        }
    }

    fun setHintText(hint: String?) {
        inputEditText.hint = hint
    }

    fun setInputType() {
        inputEditText.inputType = inputType
    }

    fun setInputOnChange() {
        inputEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                inputEditText.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {

                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                    }

                    override fun afterTextChanged(s: Editable?) {
                        if (s.toString().length < minLength) {
                            setHelperText("$hint must be at least $minLength characters long")
                            inputEditText.backgroundTintList = ColorStateList.valueOf(Color.RED)
                            helperTextView.setTextColor(Color.RED)
                        } else if (s.toString().length < minLength) {
                            setHelperText(null)
                            inputEditText.backgroundTintList = editTextBackgroundTint
                        } else
                            if (s.toString().isEmpty()) {
                                setHelperText("Field cannot be empty")
                                inputEditText.backgroundTintList = ColorStateList.valueOf(Color.RED)
                                helperTextView.setTextColor(Color.RED)
                            } else {
                                setHelperText(null)
                                inputEditText.backgroundTintList = editTextBackgroundTint
                            }
                    }
                })
            }
        }

    }
}