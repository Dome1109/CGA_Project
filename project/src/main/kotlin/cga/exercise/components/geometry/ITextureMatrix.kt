package cga.exercise.components.geometry

import org.joml.Matrix3f
import org.joml.Vector2f

interface ITextureMatrix {
    fun translateTexture(t: Vector2f)

    fun scaleTexture(s: Vector2f)

    fun rotateTexture(angle: Float)

    fun getTextureMatrix(): Matrix3f

    fun resetTextureMatrix()
}