package cga.exercise.components.light

import cga.exercise.components.geometry.Transformable
import cga.exercise.components.shader.ShaderProgram
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f

class DirectionalLight (var lightDir: Vector3f, var lightColor: Vector3f): Transformable(){

    fun bind(shaderProgram: ShaderProgram, name: String, viewMatrix: Matrix4f) {

        val calcDir = Vector4f(lightDir,0f).mul(viewMatrix)
        val dir = Vector3f(calcDir.x,calcDir.y,calcDir.z)

        shaderProgram.setUniform(name + "Dir", dir)
        shaderProgram.setUniform(name + "Col", lightColor)

    }
}