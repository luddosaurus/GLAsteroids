package com.luddosaurus.glasteroids

open class InputManager {
    var verticalFactor = 0.0f
    var horizontalFactor = 0.0f
    var pressingA = false
    var pressingB = false
    fun onStart() {}
    fun onStop() {}
    fun onPause() {}
    fun onResume() {}
    open fun display(display: Boolean) {}
}