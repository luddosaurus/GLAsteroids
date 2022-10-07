package com.luddosaurus.glasteroids

import android.opengl.GLES20
import android.opengl.Matrix
import com.luddosaurus.glasteroids.GLManager.draw

//re-usable singleton TriangleMesh
object Triangle{
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

    open fun update(dt: Float) {
        x += velX * dt;
        y += velY * dt;

        if(left() > WORLD_WIDTH){
            setRight(0f);
        }else if(right() < 0f){
            setLeft(WORLD_WIDTH);
        }

        if(top() > WORLD_HEIGHT){
            setBottom(0f);
        }else if(bottom() < 0f){
            setTop(WORLD_HEIGHT);
        }

        if(y > WORLD_HEIGHT/2f){
            setColors(1f, 0f, 0f, 1f);
        }else{
            setColors(1f, 1f, 1f, 1f);
        }
    }

    fun left() =  x + mesh.left()
    fun right()=  x + mesh.right()
    fun setLeft(leftEdgePosition: Float) {
        x = leftEdgePosition - mesh.left()
    }
    fun setRight(rightEdgePosition: Float) {
        x = rightEdgePosition - mesh.right()
    }

    fun top() =  y + mesh.top()
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
        Matrix.multiplyMM(rotationViewportModelMatrix, OFFSET, viewportModelMatrix, OFFSET, modelMatrix, OFFSET)
        draw(mesh, rotationViewportModelMatrix, color)
    }

    open fun onCollision(that: GLEntity?) {}

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
}