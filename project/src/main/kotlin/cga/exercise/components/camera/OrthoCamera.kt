package cga.exercise.components.camera

import cga.exercise.components.geometry.Transformable
import cga.exercise.components.shader.ShaderProgram
import org.joml.Math
import org.joml.Matrix4f

class OrthoCamera (val width: Float = 16f, val height: Float = 9f, var nearPlane: Float = 0.1f,
                   var farPlane: Float = 100f) : ICamera , Transformable(){
    var multiplier = 1.0f
    override fun getCalculateViewMatrix(): Matrix4f {
        val view = Matrix4f()
        return view.lookAt(getWorldPosition(), getWorldPosition().sub(getWorldZAxis()), getWorldYAxis())
    }

    override fun getCalculateProjectionMatrix(): Matrix4f {
        var m = Matrix4f()
        return m.orthoSymmetric(width * multiplier, height * multiplier, nearPlane, farPlane)
    }

    override fun bind(shader: ShaderProgram) {
        shader.setUniform("view", getCalculateViewMatrix(), false)
        shader.setUniform("projection", getCalculateProjectionMatrix(), false)
    }
}