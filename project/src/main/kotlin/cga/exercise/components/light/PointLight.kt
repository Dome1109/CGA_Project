package cga.exercise.components.light

import cga.exercise.components.geometry.Transformable
import cga.exercise.components.shader.ShaderProgram
import org.joml.Vector3f

open class PointLight (var lightPosition : Vector3f,
                       var lightColor: Vector3f, var attParam : Vector3f): Transformable(), IPointLight {



    init {
        translateGlobal(lightPosition)
    }

    override fun bind(shaderProgram: ShaderProgram, name: String) {
        shaderProgram.setUniform(name + "Pos", getWorldPosition())
        shaderProgram.setUniform(name + "Color", lightColor)
        shaderProgram.setUniform(name + "AttParam", attParam)
    }
}