package com.luddosaurus.glasteroids

import android.graphics.Color
import android.graphics.PointF
import android.opengl.GLES20
import android.opengl.Matrix
import androidx.core.graphics.alpha
import com.luddosaurus.glasteroids.GLManager.draw
import kotlin.math.abs

//re-usable singleton TriangleMesh
object Triangle {
    val mesh = Mesh(floatArrayOf(     // in counterclockwise order:
        0.0f, 0.622008459f, 0.0f,      // top
        -0.5f, -0.311004243f, 0.0f,    // bottom left
        0.5f, -0.311004243f, 0.0f      // bottom right
    ), GLES20.GL_TRIANGLES)
}

//re-usable matrices
val modelMatrix = FloatArray(4 * 4)
val viewportModelMatrix = FloatArray(4 * 4)
val rotationViewportModelMatrix = FloatArray(4 * 4)


open class GLEntity {
    // Move
    lateinit var mesh: Mesh
    var color = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f) //default white
    var depth = 0f //we'll use depth for z-axis if we need to entities
    var scale = 1f
    var rotation = 0f //angle in degrees
    var x = 0.0f
    var y = 0.0f
    var velX = 0f
    var velY = 0f
    var width = 0.0f
    var height = 0.0f
    var isAlive = true


    open fun update(dt: Float) {
        x += velX * dt
        y += velY * dt

        if (left() > engine.camera.worldWidth) {
            setRight(0f)
        } else if (right() < 0f) {
            setLeft(engine.camera.worldWidth)
        }

        if (top() > engine.camera.worldHeight) {
            setBottom(0f)
        } else if (bottom() < 0f) {
            setTop(engine.camera.worldHeight)
        }

        if (y > engine.camera.worldHeight / 2f) {
            setColors(1f, 0f, 0f, 1f)
        } else {
            setColors(1f, 1f, 1f, 1f)
        }
    }

    fun left() = x + mesh.left()
    fun right() = x + mesh.right()
    fun setLeft(leftEdgePosition: Float) {
        x = leftEdgePosition - mesh.left()
    }

    fun setRight(rightEdgePosition: Float) {
        x = rightEdgePosition - mesh.right()
    }

    fun top() = y + mesh.top()
    fun bottom() = y + mesh.bottom()
    fun setTop(topEdgePosition: Float) {
        y = topEdgePosition - mesh.top()
    }

    fun setBottom(bottomEdgePosition: Float) {
        y = bottomEdgePosition - mesh.bottom()
    }


    open fun render(viewportMatrix: FloatArray) {
        //reset the model matrix and then translate (move) it into world space
        Matrix.setIdentityM(modelMatrix, OFFSET) //reset model matrix
        Matrix.translateM(modelMatrix, OFFSET, x, y, depth)
        //viewportMatrix * modelMatrix combines into the viewportModelMatrix
        //NOTE: projection matrix on the left side and the model matrix on the right side.
        Matrix.multiplyMM(viewportModelMatrix, OFFSET, viewportMatrix, OFFSET, modelMatrix, OFFSET)
        //apply a rotation around the Z-axis to our modelMatrix. Rotation is in degrees.
        Matrix.setRotateM(modelMatrix, OFFSET, rotation, 0f, 0f, 1.0f)
        //apply scaling to our modelMatrix, on the x and y axis only.
        Matrix.scaleM(modelMatrix, OFFSET, scale, scale, 1f)
        //finally, multiply the rotated & scaled model matrix into the model-viewport matrix
        //creating the final rotationViewportModelMatrix that we pass on to OpenGL
        Matrix.multiplyMM(rotationViewportModelMatrix,
            OFFSET,
            viewportModelMatrix,
            OFFSET,
            modelMatrix,
            OFFSET)
        draw(mesh, rotationViewportModelMatrix, color)
    }

    open fun onCollision(that: GLEntity?) {
        isAlive = false
    }

    fun setColor(color : Int) {
        this.color[0] = Color.red(color) / 255f
        this.color[1] = Color.green(color) / 255f
        this.color[2] = Color.blue(color) / 255f
        this.color[3] = Color.alpha(color.alpha) / 255f
    }

    fun setColors(colors: FloatArray) {
        assert(colors.size == 4)
        setColors(colors[0], colors[1], colors[2], colors[3])
    }

    fun setColors(r: Float, g: Float, b: Float, a: Float) {
        color[0] = r //red
        color[1] = g //green
        color[2] = b //blue
        color[3] = a //alpha (transparency)
    }

    open fun isDead(): Boolean {
        return !isAlive
    }

    open fun isColliding(that: GLEntity): Boolean {
        if (this === that) {
            throw AssertionError("isColliding: You shouldn't test Entities against themselves!")
        }
        return isAABBOverlapping(this, that)
    }

    open fun centerX(): Float {
        return x //assumes our mesh has been centered on [0,0] (normalized)
    }

    open fun centerY(): Float {
        return y //assumes our mesh has been centered on [0,0] (normalized)
    }

    open fun radius(): Float {
        //use the longest side to calculate radius
        return if (width > height) width * 0.5f else height * 0.5f
    }

    open fun getPointList(): ArrayList<PointF> {
        return mesh.getPointList(x, y, rotation)
    }
}

//a basic axis-aligned bounding box intersection test.
//https://gamedev.stackexchange.com/questions/586/what-is-the-fastest-way-to-work-out-2d-bounding-box-intersection
fun isAABBOverlapping(a: GLEntity, b: GLEntity): Boolean {
    return !(a.right() <= b.left() || b.right() <= a.left() || a.bottom() <= b.top() || b.bottom() <= a.top())
}

//a more refined AABB intersection test
//returns true on intersection, and sets the least intersecting axis in overlap
val overlap = PointF(0f, 0f); //re-usable PointF for collision detection. Assumes single threading.

@SuppressWarnings("UnusedReturnValue")
fun getOverlap(a: GLEntity, b: GLEntity, overlap: PointF): Boolean {
    overlap.x = 0.0f
    overlap.y = 0.0f
    val centerDeltaX = a.centerX() - b.centerX()
    val halfWidths = (a.width + b.width) * 0.5f
    var dx = abs(centerDeltaX) //cache the abs, we need it twice

    if (dx > halfWidths) return false //no overlap on x == no collision

    val centerDeltaY = a.centerY() - b.centerY()
    val halfHeights = (a.height + b.height) * 0.5f
    var dy = abs(centerDeltaY)

    if (dy > halfHeights) return false //no overlap on y == no collision

    dx = halfWidths - dx //overlap on x
    dy = halfHeights - dy //overlap on y
    if (dy < dx) {
        overlap.y = if (centerDeltaY < 0f) -dy else dy
    } else if (dy > dx) {
        overlap.x = if (centerDeltaX < 0) -dx else dx
    } else {
        overlap.x = if (centerDeltaX < 0) -dx else dx
        overlap.y = if (centerDeltaY < 0) -dy else dy
    }
    return true
}


fun areBoundingSpheresOverlapping(a: GLEntity, b: GLEntity): Boolean {
    val dx = a.centerX() - b.centerX() //delta x
    val dy = a.centerY() - b.centerY()
    val distanceSq = dx * dx + dy * dy
    val minDistance = a.radius() + b.radius()
    val minDistanceSq = minDistance * minDistance
    return distanceSq < minDistanceSq
}