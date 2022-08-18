package cga.exercise.components.texture


import cga.exercise.components.shader.ShaderProgram
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.*
import org.lwjgl.stb.STBImage
import java.util.*
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30.*

class Skybox {

    private var texID: Int = -1
        private set

    private var skyBoxVao = 0
    private var skyBoxVbo = 0
    private var skyBoxIbo = 0

    private var indexCount = 0


    init {

        var s = 10f

        var skyboxVertices = floatArrayOf(
            // Positions
            -s, -s,  s,
             s, -s,  s,
             s, -s, -s,
            -s, -s, -s,
            -s,  s,  s,
             s,  s,  s,
             s,  s, -s,
            -s,  s, -s
        )

        var skyboxIndices = intArrayOf(
            // Right
            1, 2, 6,
            6, 5, 1,
            // Left
            0, 4, 7,
            7, 3, 0,
            // Top
            4, 5, 6,
            6, 7, 4,
            // Bottom
            0, 3, 2,
            2, 1, 0,
            // Back
            0, 1, 5,
            5, 4, 0,
            // Front
            3, 7, 6,
            6, 2, 3
        )

        indexCount = skyboxIndices.size

        // generate IDs
        skyBoxVao = GL30.glGenVertexArrays()
        skyBoxVbo = GL15.glGenBuffers()
        skyBoxIbo = GL15.glGenBuffers()

        // bind objects
        GL30.glBindVertexArray(skyBoxVao)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, skyBoxVbo)
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, skyBoxIbo)

        // upload mesh data
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, skyboxVertices, GL15.GL_STATIC_DRAW)
        GL20.glEnableVertexAttribArray(0)
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 12, 0)
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, skyboxIndices, GL15.GL_STATIC_DRAW)
        // unbind

        GL30.glBindVertexArray(0)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0)
    }

    fun applyTextures(faces : ArrayList<String>){
        texID = GL11.glGenTextures()
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texID)
        //GL13.glEnable(AMDSeamlessCubemapPerTexture.GL_TEXTURE_CUBE_MAP_SEAMLESS)



        for(i in 0 until faces.size)
        {
            var width = BufferUtils.createIntBuffer(1)
            var height = BufferUtils.createIntBuffer(1)
            var nrChannels = BufferUtils.createIntBuffer(1)

            STBImage.stbi_set_flip_vertically_on_load(true)

            val imageData = STBImage.stbi_load(faces[i], width, height, nrChannels, 4)
                ?: throw Exception("Image file \"" + faces[i] + "\" couldn't be read:\n" + STBImage.stbi_failure_reason())


            GL11.glTexImage2D(
                GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i,
                0, GL13.GL_RGBA8, width.get(), height.get(),
                0, GL13.GL_RGBA, GL30.GL_UNSIGNED_BYTE, imageData)


            STBImage.stbi_image_free(imageData)
        }

        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL13.GL_TEXTURE_MAG_FILTER, GL13.GL_LINEAR)
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL13.GL_TEXTURE_MIN_FILTER, GL13.GL_LINEAR)
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL13.GL_TEXTURE_WRAP_S, GL13.GL_CLAMP_TO_EDGE)
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL13.GL_TEXTURE_WRAP_T, GL13.GL_CLAMP_TO_EDGE)
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL13.GL_TEXTURE_WRAP_R, GL13.GL_CLAMP_TO_EDGE)


    }

    fun render(skyBoxShader : ShaderProgram, view : Matrix4f, projection: Matrix4f){

        glDepthFunc(GL_LEQUAL)

        skyBoxShader.use()

        val noTransView = Matrix4f(Matrix3f(view))

        skyBoxShader.setUniform("view", noTransView , false)
        skyBoxShader.setUniform("projection", projection, false)

        GL30.glBindVertexArray(skyBoxVao)
        GL30.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texID)
        GL11.glDrawElements(GL11.GL_TRIANGLES, indexCount, GL11.GL_UNSIGNED_INT, 0)

        GL30.glBindVertexArray(0)
        glDepthFunc(GL_LESS)

    }
}
