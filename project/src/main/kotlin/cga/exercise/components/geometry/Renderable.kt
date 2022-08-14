package cga.exercise.components.geometry

import cga.exercise.components.shader.ShaderProgram
import org.joml.Matrix3f
import org.joml.Vector2f

class Renderable (val meshList: MutableList<Mesh>): Transformable() , IRenderable, ITextureMatrix{

    private var textureMatrix = Matrix3f()

    override fun render(shaderProgram: ShaderProgram) {
        shaderProgram.setUniform("textureTransform", getTextureMatrix(), false)
        shaderProgram.setUniform("model_matrix", getWorldModelMatrix(), false)
        meshList.forEach { it.render(shaderProgram) }
    }

    override fun translateTexture(t: Vector2f) {
        textureMatrix.m20 += t.x
        textureMatrix.m21 += t.y
    }

    override fun scaleTexture(s: Vector2f) {
        if (s.x == 0f || s.y == 0f) throw Exception("Scale factor can't be zero!")
        textureMatrix.m00 /= s.x
        textureMatrix.m11 /= s.y
    }

    override fun rotateTexture(angle: Float) {
        textureMatrix.rotateZ(angle)
    }

    override fun getTextureMatrix() = Matrix3f(textureMatrix)

    override fun resetTextureMatrix() {
        textureMatrix = Matrix3f()
    }

}