package com.luddosaurus.glasteroids

import android.R.attr.port
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout


class TouchController(view: View) : InputManager(),
    View.OnTouchListener {

    lateinit var layout : RelativeLayout
    init {
        layout = view.findViewById(R.id.game_pad)
        view.findViewById<Button>(R.id.keypad_left).setOnTouchListener(this)
        view.findViewById<Button>(R.id.keypad_right).setOnTouchListener(this)
        view.findViewById<Button>(R.id.keypad_a).setOnTouchListener(this)
        view.findViewById<Button>(R.id.keypad_b).setOnTouchListener(this)
    }

    override fun display(display: Boolean) {
        layout.visibility = if (display) View.VISIBLE else View.INVISIBLE
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val action = event.actionMasked
        val id: Int = v.getId()
        if (action == MotionEvent.ACTION_DOWN) {
            // User started pressing a key
            if (id == R.id.keypad_left) {
                horizontalFactor -= 1f
            } else if (id == R.id.keypad_right) {
                horizontalFactor += 1f
            }
            if (id == R.id.keypad_a) {
                pressingA = true
            }
            if (id == R.id.keypad_b) {
                pressingB = true
            }
        } else if (action == MotionEvent.ACTION_UP) {
            // User released a key
            if (id == R.id.keypad_left) {
                horizontalFactor += 1f
            } else if (id == R.id.keypad_right) {
                horizontalFactor -= 1f
            }
            if (id == R.id.keypad_a) {
                pressingA = false
            }
            if (id == R.id.keypad_b) {
                pressingB = false
            }
        }

        return false
    }
}