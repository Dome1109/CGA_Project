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
    var isHit = false //accessed outside of object
    private var hitDir = Vector3f()
    private var hit = false
    init {
        reset()
    }

    fun update(dt: Float) {
        if (distance > 0 && !collision.checkCollision(asteroid) &&!hit) {
            val tP = Vector3f(toPlayer)
            asteroid.first.translateGlobal(tP.mul(velocity * dt))
            distance-= velocity * dt
        }
        else if (collision.checkCollision(asteroid) && !hit){
            isHit = true
            hit = true
            hitDir = player.getWorldPosition().sub(asteroid.first.getWorldPosition().sub(toPlayer))
            num = hitDir.length()* 0.5f
            reset()
        }
        else if (hit) {
            if (num > 0) {
                val hD = Vector3f(hitDir).normalize(num)
                player.translateGlobal(hD.mul(10 * dt))
                num -= 2*dt
                isHit = false
            }
            else {
                num = 1.2f
                hit = false
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