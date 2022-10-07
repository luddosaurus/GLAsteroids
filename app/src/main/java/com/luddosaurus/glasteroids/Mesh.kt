package com.luddosaurus.glasteroids

import android.opengl.GLES20
import java.lang.Math.PI
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

// find the size of the float type, in bytes
const val SIZE_OF_FLOAT = java.lang.Float.SIZE / java.lang.Byte.SIZE //32bit/8bit = 4 bytes
// number of coordinates per vertex in our meshes
const val COORDS_PER_VERTEX = 3 //X, Y, Z
// number of bytes per vertex
const val VERTEX_STRIDE = COORDS_PER_VERTEX * SIZE_OF_FLOAT

private const val TAG = "Mesh"
const val X = 0
const val Y = 1
const val Z = 2

open class Mesh(geometry: FloatArray, dm: Int = GLES20.GL_TRIANGLES) {
    lateinit var vertexBuffer: FloatBuffer
    var vertexCount = 0
    var drawMode = dm
    var width = 0f
    var height = 0f
    var depth = 0f
    var radius = 0f
    var min = Point3D()
    var max = Point3D()

    init{
        setVertices(geometry)
        applyDrawMode(drawMode)
    }

    open fun updateBounds() {
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var minZ = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        var maxY = -Float.MAX_VALUE
        var maxZ = -Float.MAX_VALUE
        var i = 0
        while (i < vertexCount * COORDS_PER_VERTEX) {
            val x = vertexBuffer[i + X]
            val y = vertexBuffer[i + Y]
            val z = vertexBuffer[i + Z]
            minX = min(minX, x)
            minY = min(minY, y)
            minZ = min(minZ, z)
            maxX = max(maxX, x)
            maxY = max(maxY, y)
            maxZ = max(maxZ, z)
            i += COORDS_PER_VERTEX
        }
        min[minX, minY] = minZ
        max[maxX, maxY] = maxZ
        width = maxX - minX
        height = maxY - minY
        depth = maxZ - minZ
        radius = max(max(width, height), depth) * 0.5f
    }

    open fun left()    = min.x
    open fun right()   = max.x
    open fun top()     = min.y
    open fun bottom()  = max.y
    open fun centerX() = min.x + width * 0.5f
    open fun centerY() = min.y + height * 0.5f

    private fun applyDrawMode(dm: Int) {
        assert(dm == GLES20.GL_TRIANGLES
                || dm == GLES20.GL_LINES
                || dm == GLES20.GL_POINTS)

        this.drawMode = dm
    }

    private fun setVertices(geometry: FloatArray) {
        // create a floating point buffer from a ByteBuffer
        vertexBuffer = ByteBuffer.allocateDirect(geometry.size * SIZE_OF_FLOAT)
            .order(ByteOrder.nativeOrder()) // use the device hardware's native byte order
            .asFloatBuffer()
        vertexBuffer.put(geometry) //add the coordinates to the FloatBuffer
        vertexBuffer.position(0) // set the buffer to read the first coordinate
        vertexCount = geometry.size / COORDS_PER_VERTEX
    }

    fun flip(axis: Int) {
        assert(axis == X || axis == Y || axis == Z)
        vertexBuffer.position(0)
        for (i in 0 until vertexCount) {
            val index = i * COORDS_PER_VERTEX + axis
            val invertedCoordinate = vertexBuffer[index] * -1
            vertexBuffer.put(index, invertedCoordinate)
        }
    }

    fun flipX() = flip(X)
    fun flipY() = flip(Y)
    fun flipZ() = flip(Z)

    fun vertexStride() = VERTEX_STRIDE
    fun coordinatesPerVertex() = COORDS_PER_VERTEX

}

fun generateLinePolygon(numPoints: Int, radius: Float): FloatArray {
    assert(numPoints > 2, { "a polygon requires at least 3 points." })
    val numVerts = numPoints * 2 //we render lines, and each line requires 2 points
    val verts = FloatArray(numVerts * COORDS_PER_VERTEX)
    val step = 2.0 * PI / numPoints
    var i = 0
    var point = 0
    while (point < numPoints) { //generate verts on circle, 2 per point
        var theta = point * step
        verts[i++] = (cos(theta) * radius).toFloat() //X
        verts[i++] = (sin(theta) * radius).toFloat() //Y
        verts[i++] = 0f //Z
        point++
        theta = point * step
        verts[i++] = (cos(theta) * radius).toFloat() //X
        verts[i++] = (sin(theta) * radius).toFloat() //Y
        verts[i++] = 0f //Z
    }
    return verts
}