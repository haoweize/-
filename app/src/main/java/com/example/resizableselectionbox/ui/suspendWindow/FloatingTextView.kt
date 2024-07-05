package com.example.resizableselectionbox.ui.suspendWindow

import android.content.Context
import android.view.Gravity
import android.view.MotionEvent

class FloatingTextView(context: Context,translation: String) : androidx.appcompat.widget.AppCompatTextView(context) {

    init {
        // 设置文本框的样式、位置等属性
        text = translation
        gravity = Gravity.CENTER
        textSize = 50f
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action){
            MotionEvent.ACTION_DOWN -> {
                this.visibility = GONE
            }
        }
        return super.onTouchEvent(event)
    }
}