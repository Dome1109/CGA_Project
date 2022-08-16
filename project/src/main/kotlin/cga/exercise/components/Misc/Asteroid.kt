package cga.exercise.components.Misc

import cga.exercise.components.geometry.Renderable
import org.joml.Vector2f
import org.joml.Vector3f

class Asteroid (val player: Renderable, val collision: Collision, val asteroid : Pair<Renderable, Vector2f>) {

    private var toPlayer = Vector3f()
    private var velocity = 0f
    private val maxDistence = 60f
    private var distance = maxDistence
    private var num = 1.2f
    private var isHit = false
    private var hitDir = Vector3f()
    init {
        reset()
    }

    fun update(dt: Float) {
        if (distance > 0 && !collision.checkCollision(asteroid) &&!isHit) {
            val tP = Vector3f(toPlayer)
            asteroid.first.translateGlobal(tP.mul(velocity * dt))
            distance-= velocity * dt
        }
        else if (collision.checkCollision(asteroid) && !isHit){
            isHit = true
            hitDir = player.getWorldPosition().sub(asteroid.first.getWorldPosition()).normalize()
            reset()
        }
        else if (isHit) {
            if (num > 0) {
                val hD = Vector3f(hitDir).normalize(num)
                player.translateGlobal(hD.mul(10 * dt))
                println(num)
                num -= 2*dt
            }
            else {
                num = 1.2f
                isHit = false
            }
        }
        else reset()
    }

    fun reset() {
        val playerPos = player.getWorldPosition()
        val pos = Vector3f(playerPos)
        val toPlayerFirst = pos.sub(asteroid.first.getWorldPosition())
        val randomOffsetZ = -kotlin.random.Random.nextInt(20,40).toFloat()
        val randomOffsetX = kotlin.random.Random.nextInt(0,40).toFloat()
        asteroid.first.translateGlobal(toPlayerFirst.add(randomOffsetX,0f,randomOffsetZ))


        velocity = kotlin.random.Random.nextDouble(2.0,8.0).toFloat()
        toPlayer = playerPos.sub(asteroid.first.getWorldPosition()).normalize()

        distance = maxDistence
    }
}