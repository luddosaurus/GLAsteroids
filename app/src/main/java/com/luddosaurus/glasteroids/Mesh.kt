package com.luddosaurus.glasteroids

import android.graphics.PointF
import android.opengl.GLES20
import android.util.Log
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

open class Mesh(
    geometry: FloatArray,
    dm: Int = GLES20.GL_TRIANGLES,
    private val norm: Boolean = true,
) {
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
        updateBounds()
        if (norm) normalize()
    }

    private fun updateBounds() {
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

    open fun getPointList(offsetX: Float, offsetY: Float, facingAngleDegrees: Float): ArrayList<PointF> {
        val sinTheta = sin(facingAngleDegrees * TO_RADIANS)
        val cosTheta = cos(facingAngleDegrees * TO_RADIANS)
        val verts = FloatArray(vertexCount * COORDS_PER_VERTEX)
        vertexBuffer.position(0)
        vertexBuffer.get(verts)
        vertexBuffer.position(0)
        val out = ArrayList<PointF>(vertexCount)
        var i = 0
        while (i < vertexCount * COORDS_PER_VERTEX) {
            val x = verts[i + X]
            val y = verts[i + Y]
            val rotatedX = (x * cosTheta - y * sinTheta) + offsetX
            val rotatedY = (y * cosTheta + x * sinTheta) + offsetY
            //final float z = verts[i + Z];
            out.add(PointF(rotatedX, rotatedY)) //warning! creating new PointFs... use a pool!
            i += COORDS_PER_VERTEX
        }
        return out
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

    fun setWidthHeight(w: Float, h: Float) {
        normalize() //a normalized mesh is centered at [0,0] and ranges from [-1,1]
        scale(
            (w * 0.5f),
            (h * 0.5f),
            1.0f
        ) //meaning we now scale from the center, so *0.5 (radius)
    }

    private fun scale(xFactor: Float, yFactor: Float, zFactor: Float) {
        var i = 0
        while (i < vertexCount * COORDS_PER_VERTEX) {
            vertexBuffer.put(i + X, (vertexBuffer[i + X] * xFactor).toFloat())
            vertexBuffer.put(i + Y, (vertexBuffer[i + Y] * yFactor).toFloat())
            vertexBuffer.put(i + Z, (vertexBuffer[i + Z] * zFactor).toFloat())
            i += COORDS_PER_VERTEX
        }
        updateBounds()
    }

    fun scale(factor: Float) = scale(factor, factor, factor)
    private fun scaleX(factor: Float) = scale(factor, 1.0f, 1.0f)
    private fun scaleY(factor: Float) = scale(1.0f, factor, 1.0f)
    private fun scaleZ(factor: Float) = scale(1.0f, 1.0f, factor)
    fun flipX() = scaleX(-1.0f)
    fun flipY() = scaleY(-1.0f)
    fun flipZ() = scaleZ(-1.0f)

    fun vertexStride() = VERTEX_STRIDE
    fun coordinatesPerVertex() = COORDS_PER_VERTEX

    //scale mesh to normalized device coordinates [-1.0, 1.0]
    private fun normalize() {

        val inverseW = if (width == 0.0f) 0.0f else (1f / width)
        val inverseH = if (height == 0.0f) 0.0f else (1f / height)
        val inverseD = if (depth == 0.0f) 0.0f else (1f / depth)
        var i = 0
        while (i < vertexCount * COORDS_PER_VERTEX) {
            val dx = (vertexBuffer[i + X] - min.x)
            val dy = (vertexBuffer[i + Y] - min.y)
            val dz = (vertexBuffer[i + Z] - min.z)
            val xNorm = 2.0f * (dx * inverseW) - 1.0f //(dx * inverseW) is equivalent to (dx / width)
            val yNorm = 2.0f * (dy * inverseH) - 1.0f //but avoids the risk of division-by-zero.
            val zNorm = 2.0f * (dz * inverseD) - 1.0f
            vertexBuffer.put(i + X, xNorm)
            vertexBuffer.put(i + Y, yNorm)
            vertexBuffer.put(i + Z, zNorm)
            i += COORDS_PER_VERTEX
        }
        updateBounds()
        assert(min.x >= -1.0f && max.x <= 1.0f
        ) { "normalized x[${min.x} , ${max.x}] expected x[-1.0, 1.0]" }
        assert(min.y >= -1.0f && max.y <= 1.0f
        ) { "normalized y[${min.y} , ${max.y}] expected y[-1.0, 1.0]" }
        assert(min.z >= -1.0f && max.z <= 1.0f
        ) { "normalized z[${min.z} , ${max.z}] expected z[-1.0, 1.0]" }
    }

    private fun rotate(axis: Int, theta: Float) {
        assert(axis == X || axis == Y || axis == Z)
        val sinTheta = sin(theta)
        val cosTheta = cos(theta)
        var i = 0
        while (i < vertexCount * COORDS_PER_VERTEX) {
            val x = vertexBuffer[i + X]
            val y = vertexBuffer[i + Y]
            val z = vertexBuffer[i + Z]
            if (axis == Z) {
                vertexBuffer.put(i + X, (x * cosTheta - y * sinTheta))
                vertexBuffer.put(i + Y, (y * cosTheta + x * sinTheta))
            } else if (axis == Y) {
                vertexBuffer.put(i + X, (x * cosTheta - z * sinTheta))
                vertexBuffer.put(i + Z, (z * cosTheta + x * sinTheta))
            } else if (axis == X) {
                vertexBuffer.put(i + Y, (y * cosTheta - z * sinTheta))
                vertexBuffer.put(i + Z, (z * cosTheta + y * sinTheta))
            }
            i += COORDS_PER_VERTEX
        }
        updateBounds()
    }
    fun rotateX(theta: Float) =  rotate(X, theta)
    fun rotateY(theta: Float) = rotate(Y, theta)
    fun rotateZ(theta: Float) = rotate(Z, theta)

}

fun generateLinePolygon(numPoints: Int, radius: Float): FloatArray {
    assert(numPoints > 2) { "a polygon requires at least 3 points." }
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