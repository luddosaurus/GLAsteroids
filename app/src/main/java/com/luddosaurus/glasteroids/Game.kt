package com.luddosaurus.glasteroids

import android.content.Context
import android.graphics.Color
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.AttributeSet
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.random.Random


const val WORLD_WIDTH = 144f//all dimensions are in meters
const val WORLD_HEIGHT = 70f
const val METERS_TO_SHOW_X = 144f//160m x 90m, the entire game world in view
const val METERS_TO_SHOW_Y = 70f //TO DO: calculate to match screen aspect ratio
const val STAR_COUNT = 100
const val ASTEROID_COUNT = 10
const val TIME_BETWEEN_SHOTS = 0.25f //seconds. TO DO: game play setting!
const val BULLET_COUNT = (TIME_TO_LIVE / TIME_BETWEEN_SHOTS).toInt()+1

lateinit var engine: Game

var SECOND_IN_NANOSECONDS: Long = 1000000000
var MILLISECOND_IN_NANOSECONDS: Long = 1000000
var NANOSECONDS_TO_MILLISECONDS = 1.0f / MILLISECOND_IN_NANOSECONDS
var NANOSECONDS_TO_SECONDS = 1.0f / SECOND_IN_NANOSECONDS

// SurfaceView & Renderer
class Game(
    context: Context,
    attributeSet: AttributeSet? = null,
) : GLSurfaceView(context, attributeSet), GLSurfaceView.Renderer {


    private val bgColorHex = Color.parseColor("#17223F")
    private val bgColor = floatArrayOf(
        hexToFloat(bgColorHex.red),
        hexToFloat(bgColorHex.green),
        hexToFloat(bgColorHex.blue),
        hexToFloat(bgColorHex.alpha)
    )

    // Entities
    private val player = Player(WORLD_WIDTH / 2f, WORLD_HEIGHT/2f)
    private val border = Border(WORLD_WIDTH/2f, WORLD_HEIGHT/2f, WORLD_WIDTH, WORLD_HEIGHT)
    private val stars = ArrayList<Star>()
    private val asteroids = ArrayList<Asteroid>()
    private val texts = ArrayList<Text>()
    private var bullets = ArrayList<Bullet>(BULLET_COUNT)


    var inputs = InputManager() //empty but valid default

    private val viewportMatrix = FloatArray(4 * 4) // Camera

    init {
        engine = this
        for (i in 0 until STAR_COUNT) {
            val x = Random.nextInt(WORLD_WIDTH.toInt()).toFloat()
            val y = Random.nextInt(WORLD_HEIGHT.toInt()).toFloat()
            stars.add(Star(x, y))
        }

        for (i in 0 until ASTEROID_COUNT) {
            val x = Random.nextInt(WORLD_WIDTH.toInt()).toFloat()
            val y = Random.nextInt(WORLD_HEIGHT.toInt()).toFloat()
            val points = Random.nextInt(6) + 3
            asteroids.add(Asteroid(x, y, points))
        }

        for (i in 0 until BULLET_COUNT) {
            bullets.add(Bullet())
        }

        setEGLContextClientVersion(2)
        setRenderer(this)

    }


    fun setControls(input: InputManager) {
        inputs.onPause()
        inputs.onStop()
        inputs = input
        inputs.onResume()
        inputs.onStart()
    }

    private fun hexToFloat(hex: Int): Float = hex / 255f

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {

        //compile, link and upload our shaders to the GPU
        GLManager.buildProgram()

        //set clear color
        GLES20.glClearColor(
            bgColor[0],
            bgColor[1],
            bgColor[2],
            bgColor[3]
        )


    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

    }

    override fun onDrawFrame(p0: GL10?) {
        update()
        render()

    }

    fun maybeFireBullet(source: GLEntity): Boolean {
        for (b in bullets) {
            if (!b.isAlive) {
                b.fireFrom(source)
                return true
            }
        }
        return false
    }

    // trying a fixed time-step with accumulator, courtesy of
    // https://gafferongames.com/post/fix_your_timestep/Links to an external site.
    private val dt = 0.01f
    private var accumulator = 0.0f
    var currentTime = (System.nanoTime() * NANOSECONDS_TO_SECONDS).toFloat()

    private fun update() {
        val newTime = (System.nanoTime() * NANOSECONDS_TO_SECONDS).toFloat()
        val frameTime = newTime - currentTime

        currentTime = newTime
        accumulator += frameTime
        while (accumulator >= dt) {
            for (a in asteroids) {
                a.update(dt)
            }

            for (b in bullets) {
                if (!b.isAlive) {
                    continue //skip
                }
                b.update(dt)
            }

            player.update(dt)
            accumulator -= dt

            fps = 1f/frameTime
            texts.clear()
            texts.add(Text("FPS:${fps}", 8f, 8f))
        }

    }

    var fps = 0f
    private fun render() {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT) //clear buffer to background color
        val offset = 0
        val left = 0f
        val right = METERS_TO_SHOW_X
        val bottom = METERS_TO_SHOW_Y
        val top = 0f
        val near = 0f
        val far = 1f
        Matrix.orthoM(viewportMatrix, offset, left, right, bottom, top, near, far)
        border.render(viewportMatrix)
        for (s in stars) {
            s.render(viewportMatrix)
        }
        for (a in asteroids) {
            a.render(viewportMatrix)
        }
        for (t in texts) {
            t.render(viewportMatrix)
        }
        for (b in bullets.filter { bullet -> bullet.isAlive }) {
            b.render(viewportMatrix)
        }
        player.render(viewportMatrix)
    }


    private var previousX: Float = 0f
    private var previousY: Float = 0f
}

