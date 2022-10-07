package com.luddosaurus.glasteroids

import kotlin.math.abs
import kotlin.math.sqrt

class Point3D {
    var x = 0.0f
    var y = 0.0f
    var z = 0.0f

    constructor() {}
    constructor(x: Float, y: Float, z: Float) {
        set(x, y, z)
    }

    constructor(p: FloatArray) {
        set(p)
    }

    operator fun set(x: Float, y: Float, z: Float) {
        this.x = x
        this.y = y
        this.z = z
    }

    fun set(p: FloatArray) {
        assert(p.size == 3)
        x = p[0]
        y = p[1]
        z = p[2]
    }

    fun distanceSquared(that: Point3D): Float {
        val dx = x - that.x
        val dy = y - that.y
        val dz = z - that.z
        return dx * dx + dy * dy + dz * dz
    }

    fun distance(that: Point3D): Float {
        val dx = x - that.x
        val dy = y - that.y
        val dz = z - that.z
        return sqrt((dx * dx + dy * dy + dz * dz))
    }

    fun distanceL1(that: Point3D): Float {
        return abs(x - that.x) + abs(y - that.y) + abs(
            z - that.z
        )
    }
}