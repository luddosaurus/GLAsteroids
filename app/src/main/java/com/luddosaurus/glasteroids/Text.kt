package com.luddosaurus.glasteroids

import android.opengl.Matrix
import com.luddosaurus.glasteroids.GLManager.draw

class Text(s: String, x: Float, y: Float) : GLEntity() {
    var meshes = ArrayList<Mesh>()
    private var spacing = GLYPH_SPACING //spacing between characters
    private var glyphWidth = GLYPH_WIDTH.toFloat()
    private var glyphHeight = GLYPH_HEIGHT.toFloat()
    init {
        setString(s)
        this.x = x
        this.y = y
        setScaling(0.5f);
//        color = floatArrayOf(0.0f, 1.0f, 0.0f, 1.0f)
    }

    fun setString(s: String) {
        meshes = GLPixelFont.getString(s)
    }

    override fun render(viewportMatrix: FloatArray) {
        for (i in meshes.indices) {
            if (meshes[i] == BLANK_SPACE) {
                continue
            }
            Matrix.setIdentityM(modelMatrix, OFFSET) //reset model matrix
            Matrix.translateM(modelMatrix, OFFSET, x + (glyphWidth + spacing) * i, y, depth)
            Matrix.scaleM(modelMatrix, OFFSET, scale, scale, 1f)
            Matrix.multiplyMM(
                viewportModelMatrix,
                OFFSET,
                viewportMatrix,
                OFFSET,
                modelMatrix,
                OFFSET
            )
            draw(meshes[i], viewportModelMatrix, color)
        }
    }

    fun setScaling(factor: Float) {
        scale = factor
        spacing = GLYPH_SPACING * scale
        glyphWidth = GLYPH_WIDTH * scale
        glyphHeight = GLYPH_HEIGHT * scale
        height = glyphHeight
        width = (glyphWidth + spacing) * meshes.size
    }

}