package com.luddosaurus.glasteroids

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.random.Random

const val PREFS = "com.luddosaurus.glasteroids_preferences"


const val STARTING_LIFE = 3
const val STARTING_LEVEL = 1
const val STAR_COUNT = 100
const val ASTEROID_COUNT = 2
const val PARTICLE_COUNT = 100
const val TIME_BETWEEN_SHOTS = 0.25f //seconds
const val BULLET_COUNT = (BULLET_TIME_TO_LIVE / TIME_BETWEEN_SHOTS).toInt() + 1

lateinit var engine: Game

var SECOND_IN_NANOSECONDS: Long = 1000000000
var MILLISECOND_IN_NANOSECONDS: Long = 1000000
var NANOSECONDS_TO_MILLISECONDS = 1.0f / MILLISECOND_IN_NANOSECONDS
var NANOSECONDS_TO_SECONDS = 1.0f / SECOND_IN_NANOSECONDS

enum class GameState {
    Active, GameOver, LevelFinish
}

enum class GameEvent {
    Boost, DAMAGE, PEW, LevelGoal, LevelStart, HIT

}

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

    // Audio
    private val jukebox = Jukebox(this)
    private var audioQueue = LinkedHashSet<GameEvent>()

    val camera = Camera(getScreenWidth(), getScreenHeight())

    // Entities
    private val player = Player(camera.worldCenterX(), camera.worldCenterY())
    private val border = Border(camera.worldCenterX(), camera.worldCenterY(), camera.worldWidth, camera.worldHeight)
    private val stars = ArrayList<Star>()
    private val asteroids = ArrayList<Asteroid>()
    private val asteroidsToAdd = ArrayList<Asteroid>()
    private var bullets = ArrayList<Bullet>(BULLET_COUNT)
    private var particles = ArrayList<Particle>(PARTICLE_COUNT)

    private var hud = HUD()

    // Game
    private var score = 0
    private var level = 1
    private var health = STARTING_LIFE
    private var gameState = GameState.Active
    private var levelToBeLoaded = -1


    var inputs = InputManager() //empty but valid default


    init {
        engine = this

        for (i in 0 until BULLET_COUNT) {
            bullets.add(Bullet())
        }
        for (i in 0 until PARTICLE_COUNT) {
            particles.add(Particle())
        }

        loadLevel()
        setEGLContextClientVersion(2)
        setRenderer(this)

    }

    private fun randomColor() : Int {
        val colors = listOf(
            Color.MAGENTA,
            Color.CYAN,
            Color.BLUE,
            Color.YELLOW,
            Color.GREEN,
            Color.RED
        )
        return colors.shuffled().first()
    }

    private fun loadLevel(lvl : Int = STARTING_LEVEL) {
        levelToBeLoaded = -1
        val color = randomColor()
        level = lvl
        if (level == 1) {
            score = 0
            health = STARTING_LIFE
        }


        stars.clear()
        asteroids.clear()

        border.setColor(color)
        hud.setColor(color)
        for (i in 0 until STAR_COUNT) {
            val x = Random.nextInt(camera.worldWidth.toInt()).toFloat()
            val y = Random.nextInt(camera.worldHeight.toInt()).toFloat()
            stars.add(Star(x, y, color))
        }

        for (i in 0 until ASTEROID_COUNT * level) {
            val x = Random.nextInt(camera.worldWidth.toInt()).toFloat()
            val y = Random.nextInt(camera.worldHeight.toInt()).toFloat()
            val type = AsteroidSize.values().toList().shuffled().first()
            asteroids.add(Asteroid(x, y, type))
        }
        gameState = GameState.Active
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
            if (b.isDead()) {
                b.fireFrom(source)
                return true
            }
        }
        return false
    }

    private fun collisionDetection() {
        for (bullet in bullets) {
            if (bullet.isDead()) {
                continue
            } //skip dead bullets

            for (asteroid in asteroids) {
                if (asteroid.isDead()) {
                    continue
                } //skip dead asteroids

                if (bullet.isColliding(asteroid)) {
                    bullet.onCollision(asteroid) //notify each entity so they can decide what to do
                    asteroid.onCollision(bullet)
                }
            }
        }
        for (asteroid in asteroids) {
            if (asteroid.isDead()) {
                continue
            } //skip dead asteroids

            if (player.isColliding(asteroid)) {
                if (health <= 0) continue
                player.onCollision(asteroid)
                asteroid.onCollision(player)
            }
        }
    }

    private fun removeDeadEntities() {
        val count = asteroids.size
        for (i in count - 1 downTo 0) {
            if (asteroids[i].isDead()) {
                asteroids.removeAt(i)
            }
        }
    }

    // trying a fixed time-step with accumulator, courtesy of
    // https://gafferongames.com/post/fix_your_timestep/Links to an external site.
    private val dt = 0.01f
    private var accumulator = 0.0f
    private var currentTime = (System.nanoTime() * NANOSECONDS_TO_SECONDS)

    private fun update() {
        val newTime = (System.nanoTime() * NANOSECONDS_TO_SECONDS)
        val frameTime = newTime - currentTime

        currentTime = newTime
        accumulator += frameTime
        while (accumulator >= dt) {

            if (levelToBeLoaded > 0) loadLevel(levelToBeLoaded)

            // Updates
            for (a in asteroids) a.update(dt)

            for (b in bullets) {
                if (b.isDead()) {
                    continue
                }
                b.update(dt)
            }

            for (particle in particles) particle.update(dt)
            player.update(dt)

            // Collision
            collisionDetection()
            removeDeadEntities()

            // Add
            for (a in asteroidsToAdd)
                asteroids.add(a)
            asteroidsToAdd.clear()

            if (asteroids.isEmpty() && health > 0) {
                gameState = GameState.LevelFinish
                inputs.display(false)

            }


            accumulator -= dt
            playAudio()
            hud.update(score, health, fps = 1 / frameTime, gameState, level)


        }

            camera.lookAt(player.x, player.y)
    }


    private fun render() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT) //clear buffer to background color
        val viewportMatrix = camera.getViewportMatrix()
        border.render(viewportMatrix)
        for (s in stars) {
            s.render(viewportMatrix)
        }
        for (a in asteroids) {
            a.render(viewportMatrix)
        }

        hud.render(viewportMatrix)



        for (particle in particles)
            particle.render(viewportMatrix)

        if (gameState == GameState.Active) {
            for (b in bullets.filter { bullet -> !bullet.isDead() })
                b.render(viewportMatrix)
            player.render(viewportMatrix)
        }
    }


    private fun playAudio() {
        for (event in audioQueue)
            jukebox.playEventSound(event)

        audioQueue.clear()
    }


    fun onGameEvent(event: GameEvent, e: GLEntity?) {
        audioQueue.add(event)

        when (event) {
            GameEvent.DAMAGE -> onDamage()
            GameEvent.HIT -> onHit(e)
            else -> {}
        }

    }



    private fun onHit(e: GLEntity?) {
        val asteroid = e as Asteroid
        val x = asteroid.x
        val y = asteroid.y
        val type = asteroid.type.childType
        score += asteroid.type.points
        for (i in 1 .. asteroid.type.splitNbr) {
            asteroidsToAdd.add(Asteroid(x,y, type!!))
        }
        generateParticles(asteroid)

    }

    private fun generateParticles(source: Asteroid) {
        val particleCount = Random.nextInt(PARTICLE_COUNT / source.type.points)
        var count = 0
        for (particle in particles) {
            if (count > particleCount) break
            if (particle.isDead()) {
                particle.fireFrom(source)
                count++
            }

        }
    }

    private fun onDamage() {
        health--
        if (health == 0) {
            gameState = GameState.GameOver
            inputs.display(false)
        }
    }

    fun pause() {
        jukebox.pauseBgMusic()
    }


    fun resume() {
        jukebox.resumeBgMusic()

    }


    // JukeBox2
    fun getActivity() = context as MainActivity
    fun getAssets() = context.assets
    fun getPreferences() = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    fun getPreferencesEditor() = getPreferences().edit()
    fun savePreference(key: String, v: Boolean) =
        getPreferencesEditor().putBoolean(key, v).commit()

    private fun getScreenHeight() = context.resources.displayMetrics.heightPixels
    private fun getScreenWidth() = context.resources.displayMetrics.widthPixels

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event!!.action and MotionEvent.ACTION_MASK) {

            MotionEvent.ACTION_DOWN -> {

                when (gameState) {
                    GameState.GameOver -> {
                        levelToBeLoaded = STARTING_LEVEL
                        inputs.display(true)
                    }
                    GameState.LevelFinish -> {
                        levelToBeLoaded = level + 1
                        inputs.display(true)
                    }
                    else -> {}
                }


            }
        }
        return true


    }


}


