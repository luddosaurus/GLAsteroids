package com.luddosaurus.glasteroids
import kotlin.math.cos
import kotlin.math.sin

const val ROTATION_VELOCITY = 360f //TODO: game play values!
const val THRUST = 4f
const val DRAG = 0.99f

private const val TAG = "Player"
class Player(x: Float, y: Float) : GLEntity() {

    private var bulletCooldown = 0f

    init {
        this.x = x
        this.y = y
        width = 1f; //TO DO: gameplay values! move to configs
        height = 1.5f;
        scale = 5f

        this.mesh = Triangle.mesh

        mesh.setWidthHeight(width, height);
        mesh.flipY();
    }


    override fun isColliding(that: GLEntity): Boolean {
        if (!areBoundingSpheresOverlapping(this, that)) {
            return false
        }
        val shipHull = getPointList()
        val asteroidHull = that.getPointList()
        if (polygonVsPolygon(shipHull, asteroidHull)) {
            return true
        }

        return polygonVsPoint(asteroidHull, x, y)
    }

    override fun render(viewportMatrix: FloatArray) {
        super.render(viewportMatrix)
    }

    override fun update(dt: Float) {

        bulletCooldown -= dt;
        if(engine.inputs.pressingA && bulletCooldown <= 0f){
            setColors(1f, 0f, 1f, 1f);
            if(engine.maybeFireBullet(this)){
                bulletCooldown = TIME_BETWEEN_SHOTS;
            }
//            // Recoil
//            val theta = rotation * TO_RADIANS
//            velX += sin(theta) * THRUST * -0.5f
//            velY -= cos(theta) * THRUST * -0.5f

        }else{
            setColors(1.0f, 1f, 1f,1f);
        }

        rotation += dt * ROTATION_VELOCITY * engine.inputs.horizontalFactor
        if (engine.inputs.pressingB) {
            engine.onGameEvent(GameEvent.Boost, this)
            val theta = rotation * TO_RADIANS
            velX += sin(theta) * THRUST
            velY -= cos(theta) * THRUST
        }
        velX *= DRAG
        velY *= DRAG
        super.update(dt)
    }

    override fun onCollision(that: GLEntity?) {
        when (that) {
            is Asteroid -> engine.onGameEvent(GameEvent.Dead, this)
        }
    }
}