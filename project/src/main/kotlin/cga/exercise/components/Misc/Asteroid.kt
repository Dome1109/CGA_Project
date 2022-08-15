package cga.exercise.components.Misc

import cga.exercise.components.geometry.Renderable
import org.joml.Vector2f
import org.joml.Vector3f

class Asteroid (val player: Renderable, val collision: Collision, val asteroid : Pair<Renderable, Vector2f>) {

    private var toPlayer = Vector3f()
    private var velocity = 0f
    private val maxDistence = 40f
    private var distance = maxDistence

    init {
        reset()
    }

    fun update(dt: Float) {
        if (distance > 0 && !collision.checkCollision(asteroid)) {
            println("working")
            val tP = Vector3f(toPlayer)
            asteroid.first.translateGlobal(tP.mul(velocity * dt))
            distance-= velocity * dt
            println(toPlayer)
        }
        else reset()
    }

    fun reset() {
        val playerPos = player.getWorldPosition()
        val pos = Vector3f(playerPos)
        val toPlayerFirst = pos.sub(asteroid.first.getWorldPosition())
        val randomOffset = -kotlin.random.Random.nextInt(20,40).toFloat()

        asteroid.first.translateGlobal(toPlayerFirst.add(0f,0f,randomOffset))


        velocity = kotlin.random.Random.nextDouble(2.0,8.0).toFloat()
        toPlayer = playerPos.sub(asteroid.first.getWorldPosition()).normalize()

        distance = maxDistence
    }
}