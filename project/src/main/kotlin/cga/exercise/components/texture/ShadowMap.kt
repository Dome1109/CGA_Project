package cga.exercise.components.texture


import cga.exercise.components.shader.ShaderProgram
import cga.framework.GLError
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.opengl.*
import org.lwjgl.opengl.ARBFramebufferObject.*
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.*
import org.lwjgl.system.MemoryUtil.NULL


class ShadowMap (var lightPos: Vector3f) {
    val shadowMapDim = 2048
    var texID = -1
        private set
    var shadowMapFBO = 0
    init {
        texID = GL15.glGenTextures()
        shadowMapFBO = glGenFramebuffers()

        glBindTexture(GL_TEXTURE_2D, texID)

        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, shadowMapDim, shadowMapDim, 0, GL_DEPTH_COMPONENT, GL_FLOAT , NULL)

        glTexParameteri(ARBInternalformatQuery2.GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER)
        glTexParameteri(ARBInternalformatQuery2.GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER)
        glTexParameteri(ARBInternalformatQuery2.GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(ARBInternalformatQuery2.GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

        val clampColor = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)

        glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, clampColor)

        glBindFramebuffer(GL_FRAMEBUFFER, shadowMapFBO)
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, texID,0)
        glDrawBuffer(GL_NONE)
        glReadBuffer(GL_NONE)
        glBindFramebuffer(GL_FRAMEBUFFER, 0)


    }

    fun orthogonalProjection() : Matrix4f {
        val orthoProj = Matrix4f()
        return  orthoProj.ortho(-10f, 10f, -10f, 10f,0.1f, 75f)
    }

    fun lightView() : Matrix4f {
        val lightView = Matrix4f()
        return lightView.lookAt(Vector3f(0f).mul(lightPos), Vector3f(0f), Vector3f(0f,1f,0f))
    }

    fun lightProjection(): Matrix4f = orthogonalProjection().mul(lightView())

    fun setShadowUniforms (shader: ShaderProgram) {
        shader.use()
        glBindFramebuffer(GL_FRAMEBUFFER, shadowMapFBO)
        GL11.glClear(GL_DEPTH_BUFFER_BIT)
        glActiveTexture(GL_TEXTURE0 + 3)
        glBindTexture(GL_TEXTURE_2D, texID);GLError.checkThrow()
        shader.setUniform("shadowMap", texID)
        shader.setUniform("lightProjection", lightProjection(), false)
        glBindFramebuffer(GL_FRAMEBUFFER,0)
    }

    fun render (shader: ShaderProgram) {
        shader.setUniform("lightProjection", lightProjection(), false)
        GL11.glViewport(0,0,shadowMapDim, shadowMapDim)
        glBindFramebuffer(GL_FRAMEBUFFER, shadowMapFBO)
        GL11.glClear(GL_DEPTH_BUFFER_BIT)









    }
}