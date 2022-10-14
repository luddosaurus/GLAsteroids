package com.luddosaurus.glasteroids

private val PARTICLE_MESH = Dot.mesh
const val PARTICLE_SPEED = 8f
const val PARTICLE_SPREAD = 8f
const val PARTICLE_TIME_TO_LIVE = 0.5f //seconds

class Particle : TTLEntity(){

    init {
        setColors(0f, 1f, 1f, 1f)
        mesh = PARTICLE_MESH
        ttl = PARTICLE_TIME_TO_LIVE
    }

    override fun fireFrom(source: GLEntity) {
        x = source.x + between(-PARTICLE_SPREAD, PARTICLE_SPEED)
        y = source.y + between(-PARTICLE_SPREAD, PARTICLE_SPEED)
        velX = between(-PARTICLE_SPEED, PARTICLE_SPEED)
        velY = between(-PARTICLE_SPEED, PARTICLE_SPEED)
        ttl = BULLET_TIME_TO_LIVE
    }

}