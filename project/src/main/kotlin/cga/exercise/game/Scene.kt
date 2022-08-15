package cga.exercise.game

//import cga.exercise.components.texture.ShadowMap

import cga.exercise.components.Misc.Asteroid
import cga.exercise.components.Misc.Collision
import cga.exercise.components.Misc.MusicPlayer
import cga.exercise.components.camera.ICamera
import cga.exercise.components.camera.OrthoCamera
import cga.exercise.components.camera.TronCamera
import cga.exercise.components.geometry.Mesh
import cga.exercise.components.geometry.Renderable
import cga.exercise.components.light.DirectionalLight
import cga.exercise.components.light.PointLight
import cga.exercise.components.light.SpotLight
import cga.exercise.components.shader.ShaderProgram
import cga.exercise.components.texture.Skybox
import cga.framework.GLError
import cga.framework.GameWindow
import cga.framework.ModelLoader
import cga.framework.OBJLoader
import org.joml.Math
import org.joml.Math.*
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL30.*
import kotlin.math.PI


/**
 * Created by Fabian on 16.09.2017.
 */
class Scene(private val window: GameWindow) {

    // Shaders
    private val tronShader: ShaderProgram
    private val monoChromeRed: ShaderProgram
    private val toonShader: ShaderProgram
    private var currentShader: ShaderProgram

    // Skybox
    private val skyBoxShader: ShaderProgram
    private val skyBoxShaderMono: ShaderProgram
    private val skyBoxShaderToon: ShaderProgram
    private var currentSkyboxShader: ShaderProgram

    val skybox = Skybox()
    val skyBoxFaces = arrayListOf<String>()


    // Objects
    var ufo : Renderable
    val saturn: Renderable
    var astronaut : Renderable
    val asteroids = arrayListOf<Renderable>()
    val earth : Renderable
    val moon: Renderable
    val shuttle : Renderable
    val items = arrayListOf<Renderable>()
    val smallFlame : Renderable
    val bigFlame : Renderable


    // Cameras
    val camera = TronCamera()
    val orthocamera = OrthoCamera()
    val firstPersonCamera = TronCamera()
    var currentCamera : ICamera

    // Lights
    val dirLight : DirectionalLight
    val pointLight2 : PointLight
    val pointLight : PointLight
    val spotLight: SpotLight

    //MouseParam
    var notFirstFrame = false
    var oldMousePosX = 0.0
    var oldMousePosY = 0.0

    // Jetpack
    var timeOut = false
    var fuelInUse = false
    val maxFuelAmount = 100f
    val jetFuelTimeOutBufferQuot = 4
    var fuelAmount = maxFuelAmount

    var timerCollision = 50f

    var blinn = false

    var accTransValue = 0f
    var transFactor = 0.1f
    val ufoBoudingBox : Pair<Renderable, Vector2f>

    val collisionAstronaut : Collision
    val listOfAsteroids = arrayListOf<Asteroid>()

    var bigFlameRender : Boolean = false

    //scene setup
    init {

        tronShader = ShaderProgram("assets/shaders/tron_vert.glsl", "assets/shaders/tron_frag.glsl")
        skyBoxShader = ShaderProgram("assets/shaders/skyBox_vert.glsl", "assets/shaders/skyBox_frag.glsl")
        skyBoxShaderMono = ShaderProgram("assets/shaders/skyBox_vert.glsl", "assets/shaders/skyBoxMono_frag.glsl")
        skyBoxShaderToon = ShaderProgram("assets/shaders/skyBox_vert.glsl", "assets/shaders/skyBoxToon_frag.glsl")
        monoChromeRed = ShaderProgram("assets/shaders/monoChromeRed_vert.glsl", "assets/shaders/monoChromeRed_frag.glsl")
        toonShader = ShaderProgram("assets/shaders/toon_vert.glsl", "assets/shaders/toon_frag.glsl")

        currentSkyboxShader = skyBoxShader

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


        orthocamera.rotateLocal(toRadians(-85f), 0f, 0f)
        orthocamera.translateLocal(Vector3f(0f, 6f, 20f))

        camera.rotateLocal(Math.toRadians(-35f),0f, 0f)
        camera.translateLocal(Vector3f(0f, 0f, 4f))

        firstPersonCamera.translateLocal(Vector3f(0f,0.75f,-1f))


        ufo = ModelLoader.loadModel("assets/ufo/Low_poly_UFO.obj",
            toRadians(180f), toRadians(90f), 0f)?: throw Exception("Renderable can't be NULL!")

        saturn = ModelLoader.loadModel("assets/saturn/Saturn_V1.obj",
            toRadians(0f), toRadians(0f), 0f)?: throw Exception("Renderable can't be NULL!")

        astronaut = ModelLoader.loadModel("assets/astronaut/astronaut.obj", 0f, 0f, 0f)?: throw Exception("Renderable can't be NULL!")

        smallFlame = ModelLoader.loadModel("assets/flames/small_flame.obj", 0f, 0f, 0f)?: throw Exception("Renderable can't be NULL!")
        bigFlame = ModelLoader.loadModel("assets/flames/big_flame.obj", 0f, 0f, 0f)?: throw Exception("Renderable can't be NULL!")

        earth = ModelLoader.loadModel("assets/earth/kugel.obj", 0f, 0f, 0f)?: throw Exception("Renderable can't be NULL!")
        moon = ModelLoader.loadModel("assets/moon/Moon 2K.obj", 0f, 0f, 0f)?: throw Exception("Renderable can't be NULL!")
        shuttle = ModelLoader.loadModel("assets/shuttle/shuttle.obj", 0f, 0f, 0f)?: throw Exception("Renderable can't be NULL!")

        asteroids.add(ModelLoader.loadModel("assets/asteroid1/asteroid1.obj",0f,0f,0f)?: throw Exception("Renderable can't be NULL!"))
        asteroids.add(ModelLoader.loadModel("assets/asteroid2/asteroid2.obj",0f,0f,0f)?: throw Exception("Renderable can't be NULL!"))
        asteroids.add(ModelLoader.loadModel("assets/asteroid3/asteroid3.obj",0f, 0f,0f)?: throw Exception("Renderable can't be NULL!"))

        items.add(ModelLoader.loadModel("assets/wrench/wrench.obj", 0f, 0f, 0f)?: throw Exception("Renderable can't be NULL!"))
        items.add(ModelLoader.loadModel("assets/screw/screw.obj", 0f, 0f, 0f)?: throw Exception("Renderable can't be NULL!"))

        saturn.scaleLocal(Vector3f(0.01f))
        saturn.translateGlobal(Vector3f(30f, 0f, -30f))
        //saturn.rotateLocal(toRadians(90f),0f,0f)
        astronaut.scaleLocal(Vector3f(0.4f))

        camera.parent = astronaut
        firstPersonCamera.parent = astronaut

        earth.scaleLocal(Vector3f(0.3f))
        earth.translateLocal(Vector3f(-200f,0f,-200f))

        shuttle.scaleLocal(Vector3f(0.5f))
        shuttle.translateLocal(Vector3f(-20f,0f,-20f))
        shuttle.translateLocal(Vector3f(-10f,0f,-20f))

        ufo.scaleLocal(Vector3f(0.1f))
        ufo.translateLocal(Vector3f(-40f, 40f, -200f))

        items[0].translateLocal(Vector3f(10f,0f,-10f))
        items[0].scaleLocal(Vector3f(0.7f))

        items[1].translateLocal(Vector3f(-10f,0f,-10f))
        items[1].scaleLocal(Vector3f(0.1f))

        //astronaut.translateLocal(Vector3f(2f,0f,-2f))

        asteroids[0].scaleLocal(Vector3f(0.6f))
        asteroids[1].scaleLocal(Vector3f(0.3f))
        asteroids[2].scaleLocal(Vector3f(0.1f))

        asteroids[0].translateLocal(Vector3f(5f,2f,-40f))
        asteroids[1].translateLocal(Vector3f(-5f,3f,-60f))
        asteroids[2].translateLocal(Vector3f(15f,10f,-90f))

        collisionAstronaut = Collision(astronaut)

        for (a in asteroids) listOfAsteroids.add(Asteroid(astronaut, collisionAstronaut, Pair(a,Vector2f(2f))))


        MusicPlayer.playMusic("assets/music/spaceMusicV4.wav")

        orthocamera.parent = astronaut
        orthocamera.multiplier = 1.1f
        pointLight = PointLight(Vector3f(0f, 2f, 0f), Vector3f(1f, 1f, 0f),
                Vector3f(1f, 0.5f, 0.1f))
        pointLight2 = PointLight(Vector3f(0f, 2f, 0f), Vector3f(0f, 1f, 0f),
            Vector3f(1f, 0.5f, 0.1f))

        spotLight = SpotLight(Vector3f(0f, 1f, -2f), Vector3f(1f,1f,0.6f),
                Vector3f(0.5f, 0.05f, 0.01f), Vector2f(toRadians(15f), toRadians(30f)))

        dirLight = DirectionalLight(Vector3f(-1f, 0f, 0f), Vector3f(1f,1f,1f))
        spotLight.rotateLocal(toRadians(-10f), PI.toFloat(),0f)

        camera.parent = astronaut
        firstPersonCamera.parent = astronaut

        smallFlame.parent = astronaut
        bigFlame.parent = astronaut
        pointLight.parent = astronaut
        spotLight.parent = astronaut
        earth.parent = astronaut
        saturn.parent = astronaut
        moon.parent = earth
        moon.scaleLocal(Vector3f(10f))
        moon.translateLocal(Vector3f(0f,0f,10f))

        ufoBoudingBox = Pair(ufo, Vector2f(4f,4f))


        currentCamera = camera
        currentShader = tronShader


    }


    fun getXandZ_coord(asteroid : Renderable?): Pair<Float, Float>{


        //Gleiche für das bewegende Objekt
        val asteroidX = asteroid?.getWorldPosition();
        val asteroidY = asteroid?.getWorldPosition();

        val ex = asteroidX!!.x;
        val ey = asteroidY!!.z;


        return Pair(ex, ey)
    }

    fun asteroidLogic (asteroid: Renderable?, dt:Float) {
        //val oldPos = asteroid?.getWorldPosition()
        val asteroidpos = Vector3f(asteroid?.getWorldPosition())
        //var coord_asteroid = getXandZ_coord(asteroid)
        val isColliding: Boolean
        val astronautpos = Vector3f(astronaut.getWorldPosition())
        val toAstronaut = astronautpos.sub(asteroidpos).normalize(2f)
        //toAstronaut.z *= -1
        if (asteroid == null) {
            isColliding = false
            Exception("")
        }
        else {
            isColliding = collisionAstronaut.checkCollision(listOf(asteroid))
        }
        if (isColliding) astronaut.translateGlobal(Vector3f(toAstronaut.x, 0f, toAstronaut.z))
        else asteroid?.translateGlobal(toAstronaut.mul(1* dt))


        //println("asteroid${asteroid.getWorldPosition().min}")
        //println(asteroid.getWorldPosition())

    }


    fun render(dt: Float, t: Float) {

        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        skybox.render(currentSkyboxShader, currentCamera.getCalculateViewMatrix(), currentCamera.getCalculateProjectionMatrix())

        currentShader.use()

        if (blinn) currentShader.setUniform("blinn", 1)
        else currentShader.setUniform("blinn", 0)

        currentCamera.bind(currentShader)

        dirLight.bind(currentShader, "dirLight",currentCamera.getCalculateViewMatrix())
        spotLight.bind(currentShader, "spotLight", currentCamera.getCalculateViewMatrix())
        pointLight.bind(currentShader, "pointLight")
        pointLight2.bind(currentShader, "pointLight2")

        currentShader.setUniform("farbe", Vector3f(0f,0f,0f))
        earth.render(currentShader)

        saturn.render(currentShader)
        astronaut.render(currentShader)



        moon.render(currentShader)
        currentShader.setUniform("farbe", Vector3f(1f,1f,1f))
        shuttle.render(currentShader)

        for (i in asteroids){
            i.render(currentShader)
        }

        for (i in items){
            i.render(currentShader)
        }

        currentShader.setUniform("farbe", Vector3f(1f,1f,1f))
        ufo.render(currentShader)

        smallFlame.render(currentShader)
        if (bigFlameRender) bigFlame.render(currentShader)
        //currentShader.setUniform("farbe", Vector3f(abs(sin(t)), abs(sin(t/2f)), abs(sin(t/3f))))



        //ground.render(currentShader)


    }
    fun collisionResponse (p : Pair<Renderable, Vector2f>) {
        if (collisionAstronaut.checkCollision(p)) {
        val dirVector = astronaut.getWorldPosition().sub(p.first.getWorldPosition()).normalize(0.1f)
        astronaut.translateGlobal(Vector3f(dirVector.x, 0f, dirVector.z))
        }
    }

    fun update(dt: Float, t: Float) {
        earth.rotateLocal(0f, 0.05f * dt,0f)
        moon.rotateAroundPoint(0f, 0.05f * -dt, 0f, Vector3f(0f))
        moon.rotateAroundPoint(0.05f * dt, 0.05f * -dt, 0f, Vector3f(0f))
        moon.rotateLocal(0f,0.05f * dt,0f)
        //println(collisionAstronaut.checkCollision(ufoBoudingBox))
        //println(collisionAstronaut.checkCollision(Pair(shuttle, Vector2f(8f, 2.3f))))
        smallFlame.translateTexture(Vector2f(15f*dt,0f))
        bigFlame.translateTexture(Vector2f(15*dt,0f))
        collisionResponse(Pair(shuttle,Vector2f(8f,2f)))

        for (a in listOfAsteroids) a.update(dt)

        if (collisionAstronaut.checkCollision(ufoBoudingBox)) {
            val dirVector = astronaut.getWorldPosition().sub(ufo.getWorldPosition()).normalize(0.1f)
            astronaut.translateGlobal(Vector3f(dirVector.x, 0f, dirVector.z))
        }
        /*
        for(a in asteroids) {
            asteroidLogic(a, dt)
        }
        */
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

        if (window.getKeyState(GLFW_KEY_V)) currentCamera = firstPersonCamera

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
                    astronaut.rotateLocal(0f,1.5f * dt,0f)
                    earth.rotateAroundPoint(0f,1.5f *-dt,0f, Vector3f(0f))
                    saturn.rotateAroundPoint(0f,1.5f *-dt,0f, Vector3f(0f))
                }
                if (window.getKeyState(GLFW_KEY_D)) {
                    astronaut.rotateLocal(0f, 1.5f * -dt,0f)
                    earth.rotateAroundPoint(0f, 1.5f * dt,0f,Vector3f(0f))
                    saturn.rotateAroundPoint(0f, 1.5f * dt,0f,Vector3f(0f))
                }
                if (window.getKeyState(GLFW_KEY_LEFT_SHIFT)  && !timeOut) {
                    fuelInUse = true
                    fuelAmount -= 40 * dt
                    astronaut.translateLocal(Vector3f(0f, 0f, accMovementSpeedFactor * -dt))
                    bigFlameRender = true
                }
                else {
                    astronaut.translateLocal(Vector3f(0f, 0f, norMovementSpeedFactor * -dt))
                    bigFlameRender = false
                }
            }
            window.getKeyState(GLFW_KEY_S) -> {

                if (window.getKeyState(GLFW_KEY_A)) {
                    astronaut.rotateLocal(0f,1.5f * dt,0f)
                    earth.rotateAroundPoint(0f,1.5f * -dt,0f, Vector3f(0f))
                    saturn.rotateAroundPoint(0f, 1.5f * -dt,0f,Vector3f(0f))
                }
                if (window.getKeyState(GLFW_KEY_D)) {
                    astronaut.rotateLocal(0f, 1.5f * -dt,0f)
                    earth.rotateAroundPoint(0f,1.5f * dt,0f, Vector3f(0f))
                    saturn.rotateAroundPoint(0f, 1.5f * dt,0f,Vector3f(0f))
                }
                astronaut.translateLocal(Vector3f(0f, 0f, revMovementSpeedFactor * dt))
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

        if (collisionAstronaut.checkCollision(asteroids)){
            println("Kollision mit Asteroid")
        }

        // Cooldown zum Item aufsammeln
        timerCollision -= 10*dt
        //println(timerCollision)

        // Shader-Wechsel bei Aufsammeln eines Items
        if (collisionAstronaut.checkCollision(items)){
            println("Kollision mit Item")

            if (currentShader == tronShader && timerCollision <= 0){
                currentShader = monoChromeRed
                currentSkyboxShader = skyBoxShaderMono
                timerCollision = 50f
            }
            else if (currentShader == monoChromeRed && timerCollision <= 0){
                currentShader = toonShader
                currentSkyboxShader = skyBoxShaderToon
                timerCollision = 50f
            }
            else if (currentShader == toonShader && timerCollision <= 0){
                currentShader = tronShader
                currentSkyboxShader = skyBoxShader
                timerCollision = 50f
            }
        }
        /*
        //Astronaut Schweben
        if (t.toInt() % 2 == 0){
            astronaut.translateLocal(Vector3f(0f,0.2f* dt,0f))
            camera.translateLocal(Vector3f(0f,0.2f* -dt,0f))

        } else {
            astronaut.translateLocal(Vector3f(0f,-0.2f *dt,0f))
            camera.translateLocal(Vector3f(0f,0.2f* dt,0f))
        }
        */
    }

    fun onKey(key: Int, scancode: Int, action: Int, mode: Int) {}

    fun onMouseMove(xpos: Double, ypos: Double) {
        val deltaX = xpos - oldMousePosX
        var deltaY = ypos - oldMousePosY
        oldMousePosX = xpos
        oldMousePosY = ypos

        if(notFirstFrame) {

            if (currentCamera == camera)
                camera.rotateAroundPoint(0f, toRadians(deltaX.toFloat() * 0.05f), 0f, Vector3f(0f))

            else if (currentCamera == firstPersonCamera) {
                astronaut.rotateLocal(0f, -toRadians(deltaX.toFloat() * 0.05f), 0f)

                // negate parent rotation
                saturn.rotateAroundPoint(0f, toRadians(deltaX.toFloat() * 0.05f), 0f, Vector3f(0f))
                earth.rotateAroundPoint(0f, toRadians(deltaX.toFloat() * 0.05f), 0f, Vector3f(0f))
            }


        }

        notFirstFrame = true
    }

    // Zoom
    fun onMouseScroll(xoffset: Double, yoffset: Double){

        if (yoffset>0){
            if (camera.getWorldPosition().y >= 0.66)
                camera.translateLocal(Vector3f(0f, 0.04f*yoffset.toFloat(), -0.1f*yoffset.toFloat()))
        }
        else if (yoffset<0){
            if (camera.getWorldPosition().y <= 1.25)
                camera.translateLocal(Vector3f(0f, 0.04f*yoffset.toFloat(), -0.1f*yoffset.toFloat()))
        }
    }

    fun cleanup() {}
}