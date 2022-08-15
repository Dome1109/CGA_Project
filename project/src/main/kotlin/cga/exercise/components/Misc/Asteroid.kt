package cga.exercise.components.Misc

import cga.exercise.components.geometry.Renderable
import org.joml.Vector2f
import org.joml.Vector3f

class Asteroid (val player: Renderable, val collision: Collision, val asteroid : Pair<Renderable, Vector2f>) {

    private var toPlayer = Vector3f()
    private var velocity = 0f
    private val maxDistence = 30f
    private var distance = maxDistence

    init {
        reset()
    }

    fun update(dt: Float) {
        if (distance > 0 && !collision.checkCollision(asteroid)) {
            println("working")
            asteroid.first.translateGlobal(toPlayer.mul(velocity * dt))
            distance-= velocity * dt
            println(toPlayer)
        }
        else reset()
    }

    fun reset() {
        val playerPos = player.getWorldPosition()
        val toPlayer = playerPos.sub(asteroid.first.getWorldPosition())
        val randomOffset = -kotlin.random.Random.nextInt(10,30).toFloat()

        asteroid.first.translateGlobal(toPlayer)
        asteroid.first.translateGlobal(Vector3f(0f,0f,randomOffset))

        this.velocity = kotlin.random.Random.nextDouble(4.0,8.0).toFloat()
        this.toPlayer = playerPos.sub(asteroid.first.getWorldPosition())

        distance = maxDistence
    }
}