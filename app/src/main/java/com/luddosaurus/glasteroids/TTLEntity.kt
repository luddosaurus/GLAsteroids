package com.luddosaurus.glasteroids

abstract class TTLEntity : GLEntity () {
    var ttl = 1f

    abstract fun fireFrom(source: GLEntity)

    override fun isDead(): Boolean {
        return ttl <= 0
    }

    override fun update(dt: Float) {
        if (ttl > 0) {
            ttl -= dt
            super.update(dt)
        }
    }

    override fun render(viewportMatrix: FloatArray) {
        if (ttl > 0) {
            super.render(viewportMatrix)
        }
    }

}