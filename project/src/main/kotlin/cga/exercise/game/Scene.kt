package cga.exercise.game

import cga.exercise.components.Misc.MusicPlayer
import cga.exercise.components.camera.ICamera
import cga.exercise.components.camera.OrthoCamera
import cga.exercise.components.camera.TronCamera
import cga.exercise.components.geometry.*
import cga.exercise.components.light.DirectionalLight
import cga.exercise.components.light.PointLight
import cga.exercise.components.light.SpotLight
import cga.exercise.components.shader.ShaderProgram
//import cga.exercise.components.texture.ShadowMap
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


/**
 * Created by Fabian on 16.09.2017.
 */
class Scene(private val window: GameWindow) {
    private val staticShader: ShaderProgram
    private val tronShader: ShaderProgram
    private val monoChromeRed: ShaderProgram
    private val toonShader: ShaderProgram

    private val skyBoxShader: ShaderProgram
    private val skyBoxShaderMono: ShaderProgram
    private val skyBoxShaderToon: ShaderProgram
    private var currentSkyboxShader: ShaderProgram



    //private var shaderNumber = 2

    private var currentShader: ShaderProgram


    private val meshListGround = mutableListOf<Mesh>()

    val bodenmatrix: Matrix4f = Matrix4f()
    val kugelMatrix: Matrix4f = Matrix4f()

    val ground: Renderable
    var cycle : Renderable
    var ufo : Renderable
    val saturn: Renderable
    var astronaut : Renderable
    var asteroids = arrayListOf<Renderable>()


//    val shadowMap: ShadowMap

    private var currentCamera : ICamera
    val camera = TronCamera()
    val orthocamera = OrthoCamera()
    val dirLight : DirectionalLight
    val pointLight2 : PointLight
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
    val jetFuelTimeOutBufferQuot = 4
    var fuelAmount = maxFuelAmount

    var blinn = false

    var accTransValue = 0f
    var transFactor = 0.1f

    val camZ : Vector3f



    //scene setup
    init {
        staticShader = ShaderProgram("assets/shaders/simple_vert.glsl", "assets/shaders/simple_frag.glsl")
        tronShader = ShaderProgram("assets/shaders/tron_vert.glsl", "assets/shaders/tron_frag.glsl")
        skyBoxShader = ShaderProgram("assets/shaders/skyBox_vert.glsl", "assets/shaders/skyBox_frag.glsl")
        skyBoxShaderMono = ShaderProgram("assets/shaders/skyBox_vert.glsl", "assets/shaders/skyBoxMono_frag.glsl")
        skyBoxShaderToon = ShaderProgram("assets/shaders/skyBox_vert.glsl", "assets/shaders/skyBoxToon_frag.glsl")
        monoChromeRed = ShaderProgram("assets/shaders/monoChromeRed_vert.glsl", "assets/shaders/monoChromeRed_frag.glsl")
        toonShader = ShaderProgram("assets/shaders/toon_vert.glsl", "assets/shaders/toon_frag.glsl")

        currentSkyboxShader = skyBoxShader
        currentShader = tronShader

//        shadowMap = ShadowMap(Vector3f(-1f,0f,0f))

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



        for (mesh in objMeshListGround) {
            meshListGround.add(Mesh(mesh.vertexData, mesh.indexData, vertexAttributes, groundMaterial))
        }

        ground = Renderable(meshListGround)



        orthocamera.rotateLocal(toRadians(-85f), 0f, 0f)
        orthocamera.translateLocal(Vector3f(0f, 0f, 5f))

        camera.rotateLocal(Math.toRadians(-35f),0f, 0f)
        camera.translateLocal(Vector3f(0f, 0f, 4f))
        cycle = ModelLoader.loadModel("assets/light Cycle/light Cycle/HQ_Movie cycle.obj",
                toRadians(-90f), toRadians(90f), 0f)?: throw Exception("Renderable can't be NULL!")

        ufo = ModelLoader.loadModel("assets/ufo/Low_poly_UFO.obj",
            toRadians(180f), toRadians(90f), 0f)?: throw Exception("Renderable can't be NULL!")

        saturn = ModelLoader.loadModel("assets/saturn/Saturn_V1.obj",
            toRadians(0f), toRadians(0f), 0f)?: throw Exception("Renderable can't be NULL!")

        astronaut = ModelLoader.loadModel("assets/astronaut/astronaut.obj", 0f, toRadians(270f), 0f)?: throw Exception("Renderable can't be NULL!")

       asteroids.add(ModelLoader.loadModel("assets/asteroid1/asteroid1.obj",0f,0f,0f)?: throw Exception("Renderable can't be NULL!"))
       asteroids.add(ModelLoader.loadModel("assets/asteroid2/asteroid2.obj",0f,0f,0f)?: throw Exception("Renderable can't be NULL!"))
       asteroids.add(ModelLoader.loadModel("assets/asteroid3/asteroid3.obj",0f, toRadians(90f),0f)?: throw Exception("Renderable can't be NULL!"))


        saturn.scaleLocal(Vector3f(0.01f))
        saturn.translateGlobal(Vector3f(30f, 0f, -30f))
        //saturn.rotateLocal(toRadians(90f),0f,0f)
        cycle.scaleLocal(Vector3f(0.8f))
        camera.parent = cycle


        ufo.scaleLocal(Vector3f(0.1f))
        ufo.translateLocal(Vector3f(0f, 40f, -50f))

        astronaut.translateLocal(Vector3f(2f,0f,-2f))

        asteroids[0].scaleLocal(Vector3f(0.6f))
        asteroids[1].scaleLocal(Vector3f(0.3f))
        asteroids[2].scaleLocal(Vector3f(0.1f))


        MusicPlayer.playMusic("assets/music/spaceMusicTest.wav")

        orthocamera.parent = cycle

        pointLight = PointLight(Vector3f(0f, 2f, 0f), Vector3f(1f, 1f, 0f),
                Vector3f(1f, 0.5f, 0.1f))
        pointLight2 = PointLight(Vector3f(0f, 2f, 5f), Vector3f(0f, 1f, 0f),
            Vector3f(1f, 0.5f, 0.1f))

        spotLight = SpotLight(Vector3f(0f, 1f, -2f), Vector3f(1f,1f,0.6f),
                Vector3f(0.5f, 0.05f, 0.01f), Vector2f(toRadians(15f), toRadians(30f)))

        dirLight = DirectionalLight(Vector3f(-1f, 0f, 0f), Vector3f(1f,1f,1f))
        spotLight.rotateLocal(toRadians(-10f), PI.toFloat(),0f)

        pointLight.parent = cycle
        spotLight.parent = cycle

        currentCamera = camera
        camZ = camera.getZAxis()
    }

    fun render(dt: Float, t: Float) {

        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)


        skybox.render(currentSkyboxShader, currentCamera.getCalculateViewMatrix(), currentCamera.getCalculateProjectionMatrix())


        currentShader.use()
//        shadowMap.setShadowUniforms(currentShader)
        if (blinn) currentShader.setUniform("blinn", 1)
        else currentShader.setUniform("blinn", 0)

        currentCamera.bind(currentShader)
        dirLight.bind(currentShader, "dirLight",currentCamera.getCalculateViewMatrix())
        spotLight.bind(currentShader, "spotLight", currentCamera.getCalculateViewMatrix())
        pointLight.bind(currentShader, "pointLight")
        pointLight2.bind(currentShader, "pointLight2")

        ufo.render(currentShader)

        saturn.render(currentShader)

        currentShader.setUniform("farbe", Vector3f(0f,0f,0f))
       astronaut.render(currentShader)

        for (i in asteroids){
            i.render(currentShader)
        }

        currentShader.setUniform("farbe", Vector3f(abs(sin(t)), abs(sin(t/2f)), abs(sin(t/3f))))
        cycle.render(currentShader)

        currentShader.setUniform("farbe", Vector3f(0f,1f,0f))

        ground.render(currentShader)


    }

    fun update(dt: Float, t: Float) {

        if (window.getKeyState(GLFW_KEY_L)) ufo.rotateLocal(1.5f * dt,0f,0f)

        if (window.getKeyState(GLFW_KEY_1)) {
            currentShader = monoChromeRed
            currentSkyboxShader = skyBoxShaderMono
        }
        if (window.getKeyState(GLFW_KEY_2)) {
            currentShader = tronShader
            currentSkyboxShader = skyBoxShader
        }

        if (window.getKeyState(GLFW_KEY_3)){
            currentShader = toonShader
            currentSkyboxShader = skyBoxShaderToon
        }

        if (window.getKeyState(GLFW_KEY_B)){
            blinn = true
        }
        if (window.getKeyState(GLFW_KEY_N)){
            blinn = false
            println("N_KEY pressed")
        }

        if (window.getKeyState(GLFW_KEY_F)) currentCamera = camera

        if (window.getKeyState(GLFW_KEY_R)) currentCamera = orthocamera

        pointLight.lightColor = Vector3f(abs(sin(t/3f)), abs(sin(t/4f)), abs(sin(t/2)))

        val norMovementSpeedFactor = 10
        val accMovementSpeedFactor = 20
        val revMovementSpeedFactor = 2




        saturn.rotateLocal(0f,0f, 0.2f *dt)

        //ufo Movement
        ufo.rotateLocal(0f,0.9f *dt,0f)
        val maxDiscrepancy = 0.1f
        accTransValue += transFactor * dt
        ufo.translateGlobal(Vector3f(0f,transFactor *dt,0f))
        if (accTransValue >= maxDiscrepancy || accTransValue <= -maxDiscrepancy) transFactor *= -1


        // end timeOut when fuel regeneration hits target
        if (timeOut) if (fuelAmount > maxFuelAmount/jetFuelTimeOutBufferQuot) {
            timeOut = false
            println("Jet Fuel Capacity at ${100/jetFuelTimeOutBufferQuot} %")
        }

        fuelInUse = false
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

                    cycle.translateLocal(Vector3f(0f, 0f, norMovementSpeedFactor * -dt))
                }
            }
            window.getKeyState(GLFW_KEY_S) -> {

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
        if (fuelAmount <= 0 && !timeOut) {
            fuelAmount = 0f
            timeOut = true
            println("Jet TIMEOUT!")
        }

        // fuel regeneration
        if (!fuelInUse && fuelAmount < maxFuelAmount) {
            fuelAmount += 10 * dt

            if (fuelAmount > maxFuelAmount) {
                fuelAmount = maxFuelAmount
                println("Jet Fuel capacity at 100 %")
            }

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
            //camera.rotateAroundPoint(toRadians(deltaX.toFloat() * 0.03f), 0f, 0f, Vector3f(0f))

            //camera.setZAxis(camZ)
        }

        notFirstFrame = true
    }

    fun cleanup() {}
}
