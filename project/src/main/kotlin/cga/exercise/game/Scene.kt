package cga.exercise.game

import cga.exercise.components.camera.TronCamera
import cga.exercise.components.geometry.Material
import cga.exercise.components.geometry.Mesh
import cga.exercise.components.geometry.Renderable
import cga.exercise.components.geometry.VertexAttribute
import cga.exercise.components.light.PointLight
import cga.exercise.components.light.SpotLight
import cga.exercise.components.shader.ShaderProgram
import cga.exercise.components.texture.Skybox
import cga.exercise.components.texture.Texture2D
import cga.framework.GLError
import cga.framework.GameWindow
import cga.framework.ModelLoader
import cga.framework.OBJLoader
import org.joml.Math
import org.joml.Math.*
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW.*

import org.lwjgl.opengl.GL30.*
import kotlin.math.PI
import kotlin.random.Random


/**
 * Created by Fabian on 16.09.2017.
 */
class Scene(private val window: GameWindow) {
    private val staticShader: ShaderProgram
    private val tronShader: ShaderProgram
    private val skyBoxShader: ShaderProgram

    private var currentShader: ShaderProgram

    private val meshListSphere = mutableListOf<Mesh>()
    private val meshListGround = mutableListOf<Mesh>()
    val bodenmatrix: Matrix4f = Matrix4f()
    val kugelMatrix: Matrix4f = Matrix4f()

    val ground: Renderable
    val sphere: Renderable
    var cycle : Renderable

    val camera = TronCamera()

    val pointLight : PointLight
    val spotLight: SpotLight
    //MouseParam
    var notFirstFrame = false
    var oldMousePosX = 0.0
    var oldMousePosY = 0.0

    val skybox = Skybox()
    val skyBoxFaces = arrayListOf<String>()

    var timeOut = false
    var fuelInUse = false

    val maxFuelAmount = 100f
    var fuelAmount = maxFuelAmount



    //scene setup
    init {
        staticShader = ShaderProgram("assets/shaders/simple_vert.glsl", "assets/shaders/simple_frag.glsl")
        tronShader = ShaderProgram("assets/shaders/tron_vert.glsl", "assets/shaders/tron_frag.glsl")
        skyBoxShader = ShaderProgram("assets/shaders/skyBox_vert.glsl", "assets/shaders/skyBox_frag.glsl")

        currentShader = tronShader

        skyBoxFaces.add("assets/textures/skybox/right.png")
        skyBoxFaces.add("assets/textures/skybox/left.png")
        skyBoxFaces.add("assets/textures/skybox/bottom.png")
        skyBoxFaces.add("assets/textures/skybox/top.png")
        skyBoxFaces.add("assets/textures/skybox/front.png")
        skyBoxFaces.add("assets/textures/skybox/back.png")

        skybox.applyTextures(skyBoxFaces)

        glClearColor(0.0f, 0.0f, 0.0f, 1.0f); GLError.checkThrow()
        glDisable(GL_CULL_FACE); GLError.checkThrow()
        glFrontFace(GL_CCW)
        glCullFace(GL_BACK)
        glEnable(GL_DEPTH_TEST); GLError.checkThrow()
        glDepthFunc(GL_LESS); GLError.checkThrow()


        val objResSphere : OBJLoader.OBJResult = OBJLoader.loadOBJ("assets/models/sphere.obj")
        val objMeshListSphere : MutableList<OBJLoader.OBJMesh> = objResSphere.objects[0].meshes

        val objResGround : OBJLoader.OBJResult = OBJLoader.loadOBJ("assets/models/ground.obj")
        val objMeshListGround : MutableList<OBJLoader.OBJMesh> = objResGround.objects[0].meshes

        val stride = 8 * 4
        val attrPos = VertexAttribute(3, GL_FLOAT, stride, 0)
        val attrTC = VertexAttribute(2, GL_FLOAT, stride, 3 * 4)
        val attrNorm = VertexAttribute(3, GL_FLOAT, stride, 5 * 4)

        val vertexAttributes = arrayOf(attrPos,attrTC, attrNorm)

        val groundEmitTexture = Texture2D("assets/textures/ground_emit.png", true)
        val groundDiffTexture = Texture2D("assets/textures/ground_diff.png", true)
        val groundSpecTexture = Texture2D("assets/textures/ground_spec.png", true)

        groundEmitTexture.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR)
        groundDiffTexture.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR)
        groundSpecTexture.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR)

        val groundShininess = 60f
        val groundTCMultiplier = Vector2f(64f)

        val groundMaterial = Material(groundDiffTexture, groundEmitTexture, groundSpecTexture, groundShininess,
                groundTCMultiplier)

        for (mesh in objMeshListSphere) {
            meshListSphere.add(Mesh(mesh.vertexData, mesh.indexData, vertexAttributes))
        }

        for (mesh in objMeshListGround) {
            meshListGround.add(Mesh(mesh.vertexData, mesh.indexData, vertexAttributes, groundMaterial))
        }

        bodenmatrix.scale(0.03f)
        bodenmatrix.rotateX(90f)

        kugelMatrix.scale(0.5f)

        ground = Renderable(meshListGround)
        sphere = Renderable(meshListSphere)

        camera.rotateLocal(Math.toRadians(-35f),0f, 0f)
        camera.translateLocal(Vector3f(0f, 0f, 4f))
        cycle = ModelLoader.loadModel("assets/light Cycle/light Cycle/HQ_Movie cycle.obj",
                toRadians(-90f), toRadians(90f), 0f)?: throw Exception("Renderable can't be NULL!")

        cycle.scaleLocal(Vector3f(0.8f))
        camera.parent = cycle

        pointLight = PointLight(Vector3f(0f, 2f, 0f), Vector3f(1f, 1f, 0f),
                Vector3f(1f, 0.5f, 0.1f))

        spotLight = SpotLight(Vector3f(0f, 1f, -2f), Vector3f(1f,1f,0.6f),
                Vector3f(0.5f, 0.05f, 0.01f), Vector2f(toRadians(15f), toRadians(30f)))

        spotLight.rotateLocal(toRadians(-10f), PI.toFloat(),0f)

        pointLight.parent = cycle
        spotLight.parent = cycle

    }

    fun render(dt: Float, t: Float) {

        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        skybox.render(skyBoxShader, camera.getCalculateViewMatrix(), camera.getCalculateProjectionMatrix())

        currentShader.use()
        camera.bind(currentShader)

        spotLight.bind(currentShader, "spot", camera.getCalculateViewMatrix())
        pointLight.bind(currentShader, "point")




        currentShader.setUniform("farbe", Vector3f(abs(sin(t)), abs(sin(t/2f)), abs(sin(t/3f))))
        cycle.render(currentShader)

        currentShader.setUniform("farbe", Vector3f(0f,1f,0f))
        ground.render(tronShader)

    }

    fun update(dt: Float, t: Float) {

        pointLight.lightColor = Vector3f(abs(sin(t/3f)), abs(sin(t/4f)), abs(sin(t/2)))

        val norMovementSpeedFactor = 10
        val accMovementSpeedFactor = 20
        val revMovementSpeedFactor = 2



        // end timeOut when fuel regeneration hits target
        if (timeOut) if (fuelAmount > maxFuelAmount/4) timeOut = false

        when {
            window.getKeyState(GLFW_KEY_W) -> {
                if (window.getKeyState(GLFW_KEY_A)) {
                    cycle.rotateLocal(0f,1.5f * dt,0f)
                }
                if (window.getKeyState(GLFW_KEY_D)) {
                    cycle.rotateLocal(0f, 1.5f * -dt,0f)
                }
                if (window.getKeyState(GLFW_KEY_LEFT_SHIFT)  && !timeOut) {
                    fuelInUse = true
                    fuelAmount -= 40 * dt
                    cycle.translateLocal(Vector3f(0f, 0f, accMovementSpeedFactor * -dt))


                }

                else {
                    fuelInUse = false
                    cycle.translateLocal(Vector3f(0f, 0f, norMovementSpeedFactor * -dt))
                }
            }
            window.getKeyState(GLFW_KEY_S) -> {
                fuelInUse = false
                if (window.getKeyState(GLFW_KEY_A)) {
                    cycle.rotateLocal(0f,1.5f * dt,0f)
                }
                if (window.getKeyState(GLFW_KEY_D)) {
                    cycle.rotateLocal(0f, 1.5f * -dt,0f)
                }
                cycle.translateLocal(Vector3f(0f, 0f, revMovementSpeedFactor * dt))
            }
        }

        // start timOut when fuelAmount reaches (below) zero
        if (fuelAmount <= 0) {
            fuelAmount = 0f
            timeOut = true
        }

        // fuel regeneration
        if (!fuelInUse && fuelAmount < maxFuelAmount) {
            fuelAmount += 10 * dt

            if (fuelAmount > maxFuelAmount) fuelAmount = maxFuelAmount

        }
    }

    fun onKey(key: Int, scancode: Int, action: Int, mode: Int) {}

    fun onMouseMove(xpos: Double, ypos: Double) {
        val deltaX = xpos - oldMousePosX
        var deltaY = ypos - oldMousePosY
        oldMousePosX = xpos
        oldMousePosY = ypos

        if(notFirstFrame) {
            camera.rotateAroundPoint(0f, toRadians(deltaX.toFloat() * 0.05f), 0f, Vector3f(0f))
        }

        notFirstFrame = true
    }

    fun cleanup() {}
}
