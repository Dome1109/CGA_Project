package cga.exercise.components.Misc

import cga.exercise.components.geometry.Renderable
import org.joml.Vector2f

class Collision (val char: Renderable) {
    /*val charWidth: Float
    val charDepth : Float
    val c : Float
    init {
        val list = mutableListOf<Float>()
        var xValues = mutableListOf<Float>()
        var zValues = mutableListOf<Float>()
        for (mesh in char.meshList) list.addAll(mesh.vertexdata.toMutableList())
        for (i in list.indices) if (i % 8 == 0) xValues.add(list[i])
        for (i in list.indices) if (i % 8 == 2) zValues.add(list[i])

        val xMax = maxValue(xValues)
        val xMin = minValue(xValues)



        charWidth = maxValue(xValues) - minValue(xValues)
        charDepth = maxValue(zValues) - minValue(zValues)

        for (mesh in char.meshList) xValues.add(mesh.vertexdata.reduce { a: Float, b: Float -> a.coerceAtLeast(b) })
        for (mesh in char.meshList) list.add(mesh.vertexdata.max()?: throw Exception())
        c = list.max()?: throw Exception()

    }
    */
    fun maxValue (list: List<Float>) : Float = list.reduce  { a: Float, b: Float -> a.coerceAtLeast(b) }

    fun minValue (list: List<Float>) : Float = list.reduce  { a: Float, b: Float -> a.coerceAtMost(b) }

    fun checkCollision (list: List<Renderable>) :Boolean {
        val charX = char.getWorldPosition().x
        val charZ = char.getWorldPosition().z
        for (gameObject in list) {
            val objX = gameObject.getWorldPosition().x
            val objZ = gameObject.getWorldPosition().z
            val collisionX = charX + 1.2f >= objX && objX + 1.2f >= charX
            val collisionZ = charZ + 1.2f >= objZ && objZ + 1.2f >= charZ
            if (collisionX && collisionZ) return true
        }
        return false
    }

    @JvmName("checkCollision1")
    fun checkCollision (list: List<Pair<Renderable, Vector2f>>) : Boolean{
        for (gameObject in list) if (checkCollision(gameObject)) return true
        return false
    }

    fun checkCollision (r: Pair<Renderable, Vector2f>) :Boolean{
        val charX = char.getWorldPosition().x
        val charZ = char.getWorldPosition().z
        val rX = r.first.getWorldPosition().x
        val rZ = r.first.getWorldPosition().z
        val collisionX = (charX + 1.2f >= rX && rX + r.second.x >= charX) || (charX - 1.2f <= rX && rX - r.second.x <= charX)
        val collisionZ = (charZ + 1.2f >= rZ && rZ + r.second.y >= charZ) || (charZ - 1.2f <= rZ && rZ - r.second.y <= charZ)
        return collisionX && collisionZ
    }

}