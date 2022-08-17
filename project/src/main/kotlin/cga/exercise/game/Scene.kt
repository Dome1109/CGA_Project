package cga.exercise.game

import cga.exercise.components.Misc.Asteroid
import cga.exercise.components.Misc.Collision
import cga.exercise.components.Misc.MusicPlayer
import cga.exercise.components.camera.ICamera
import cga.exercise.components.camera.OrthoCamera
import cga.exercise.components.camera.TronCamera
import cga.exercise.components.geometry.Renderable
import cga.exercise.components.light.DirectionalLight
import cga.exercise.components.light.PointLight
import cga.exercise.components.light.SpotLight
import cga.exercise.components.shader.ShaderProgram
import cga.exercise.components.texture.Skybox
import cga.framework.GLError
import cga.framework.GameWindow
import cga.framework.ModelLoader
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
    private val shaderList = mutableListOf<ShaderProgram>()

    // Skybox
    private val skyBoxShader: ShaderProgram
    private val skyBoxShaderMono: ShaderProgram
    private val skyBoxShaderToon: ShaderProgram
    private var currentSkyboxShader: ShaderProgram
    private val skyboxShaderList = mutableListOf<ShaderProgram>()
    val skybox = Skybox()
    val skyBoxFaces = arrayListOf<String>()


    // Objects
    var ufo : Renderable
    val saturn: Renderable
    var astronaut : Renderable
    val asteroids = arrayListOf<Renderable>()
    val earth : Renderable
    val moon: Renderable
    val mars : Renderable
    val jupiter : Renderable
    val mercury : Renderable
    val venus : Renderable
    val shuttle: Renderable
    val shuttleDestroyed : Renderable
    val items = mutableListOf<Renderable>()
    val smallFlame : Renderable
    val bigFlame : Renderable
    val planets = arrayListOf<Renderable>()
    val titleScreen: Renderable

    // Cameras
    val camera = TronCamera()
    val orthocamera = OrthoCamera()
    val firstPersonCamera = TronCamera()
    val outroCamera = TronCamera()
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


    val camFPMax =  35f
    val camFPMin = -35f
    var verticalFPCameraPos = 0f

    val lifehearts = mutableListOf<Renderable>()
    val hitCoolDownMax = 0.5f
    var hitCoolDownTimer = hitCoolDownMax
    var timerActive = false

    var gameOver = false
    var repairTimer = 2f
    var repair = false
    var shuttleRepaired = false


    private var outro = false

    //scene setup
    init {

        tronShader = ShaderProgram("assets/shaders/tron_vert.glsl", "assets/shaders/tron_frag.glsl")
        skyBoxShader = ShaderProgram("assets/shaders/skyBox_vert.glsl", "assets/shaders/skyBox_frag.glsl")
        skyBoxShaderMono = ShaderProgram("assets/shaders/skyBox_vert.glsl", "assets/shaders/skyBoxMono_frag.glsl")
        skyBoxShaderToon = ShaderProgram("assets/shaders/skyBox_vert.glsl", "assets/shaders/skyBoxToon_frag.glsl")
        monoChromeRed = ShaderProgram("assets/shaders/monoChromeRed_vert.glsl", "assets/shaders/monoChromeRed_frag.glsl")
        toonShader = ShaderProgram("assets/shaders/toon_vert.glsl", "assets/shaders/toon_frag.glsl")

        shaderList.add(tronShader)
        shaderList.add(toonShader)
        shaderList.add(monoChromeRed)

        skyboxShaderList.add(skyBoxShader)
        skyboxShaderList.add(skyBoxShaderToon)
        skyboxShaderList.add(skyBoxShaderMono)

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

        camera.rotateLocal(Math.toRadians(-30f), toRadians(0f), 0f)
        camera.translateLocal(Vector3f(0f, 0f, 4f))

        outroCamera.rotateLocal(Math.toRadians(-35f), toRadians(0f), 0f)
        outroCamera.translateLocal(Vector3f(0f, 0f, 10f))

        firstPersonCamera.translateLocal(Vector3f(0f,0.75f,-1f))


        ufo = ModelLoader.loadModel("assets/ufo/Low_poly_UFO.obj",
            toRadians(180f), toRadians(90f), 0f)?: throw Exception("Renderable can't be NULL!")

        saturn = ModelLoader.loadModel("assets/saturn/Saturn_V1.obj",
            toRadians(0f), toRadians(0f), toRadians(0f)) ?: throw Exception("Renderable can't be NULL!")

        astronaut = ModelLoader.loadModel("assets/astronaut/astronaut.obj", 0f, 0f, 0f)?: throw Exception("Renderable can't be NULL!")

        smallFlame = ModelLoader.loadModel("assets/flames/small_flame.obj", 0f, 0f, 0f)?: throw Exception("Renderable can't be NULL!")
        bigFlame = ModelLoader.loadModel("assets/flames/big_flame.obj", 0f, 0f, 0f)?: throw Exception("Renderable can't be NULL!")

        earth = ModelLoader.loadModel("assets/earth/kugel.obj", 0f, 0f, 0f)?: throw Exception("Renderable can't be NULL!")
        mars = ModelLoader.loadModel("assets/mars/kugel.obj", 0f, 0f, 0f)?: throw Exception("Renderable can't be NULL!")
        jupiter = ModelLoader.loadModel("assets/jupiter/kugel.obj", 0f, 0f, 0f)?: throw Exception("Renderable can't be NULL!")
        mercury = ModelLoader.loadModel("assets/mercury/kugel.obj", 0f, 0f, 0f)?: throw Exception("Renderable can't be NULL!")
        venus = ModelLoader.loadModel("assets/venus/kugel.obj", 0f, 0f, 0f)?: throw Exception("Renderable can't be NULL!")
        moon = ModelLoader.loadModel("assets/moon/Moon 2K.obj", 0f, 0f, 0f)?: throw Exception("Renderable can't be NULL!")
        shuttleDestroyed = ModelLoader.loadModel("assets/shuttle/shuttle_destroyed.obj", 0f, toRadians(-90f), 0f)?: throw Exception("Renderable can't be NULL!")
        shuttle = ModelLoader.loadModel("assets/shuttle/shuttle.obj", 0f, toRadians(-90f), 0f)?: throw Exception("Renderable can't be NULL!")

        lifehearts.add(ModelLoader.loadModel("assets/lifeheart1/Heart1.obj",0f,0f,0f)?: throw Exception("Renderable can't be NULL!"))
        lifehearts.add(ModelLoader.loadModel("assets/lifeheart2/Heart2.obj",0f,0f,0f)?: throw Exception("Renderable can't be NULL!"))
        lifehearts.add(ModelLoader.loadModel("assets/lifeheart3/Heart3.obj",0f,0f,0f)?: throw Exception("Renderable can't be NULL!"))

        asteroids.add(ModelLoader.loadModel("assets/asteroid1/asteroid1.obj",0f,0f,0f)?: throw Exception("Renderable can't be NULL!"))
        asteroids.add(ModelLoader.loadModel("assets/asteroid2/asteroid2.obj",0f,0f,0f)?: throw Exception("Renderable can't be NULL!"))
        asteroids.add(ModelLoader.loadModel("assets/asteroid3/asteroid3.obj",0f, 0f,0f)?: throw Exception("Renderable can't be NULL!"))
        asteroids.add(ModelLoader.loadModel("assets/asteroid1/asteroid1.obj",0f, toRadians(55f),0f)?: throw Exception("Renderable can't be NULL!"))
        asteroids.add(ModelLoader.loadModel("assets/asteroid2/asteroid2.obj",0f,toRadians(45f),0f)?: throw Exception("Renderable can't be NULL!"))
        asteroids.add(ModelLoader.loadModel("assets/asteroid3/asteroid3.obj",0f, toRadians(75f),0f)?: throw Exception("Renderable can't be NULL!"))
        asteroids.add(ModelLoader.loadModel("assets/asteroid1/asteroid1.obj",0f, toRadians(30f),0f)?: throw Exception("Renderable can't be NULL!"))
        asteroids.add(ModelLoader.loadModel("assets/asteroid2/asteroid2.obj",0f,toRadians(10f),0f)?: throw Exception("Renderable can't be NULL!"))
        asteroids.add(ModelLoader.loadModel("assets/asteroid3/asteroid3.obj",0f, toRadians(120f),0f)?: throw Exception("Renderable can't be NULL!"))

        items.add(ModelLoader.loadModel("assets/wrench/wrench.obj", toRadians(45f), 0f, 0f)?: throw Exception("Renderable can't be NULL!"))
        items.add(ModelLoader.loadModel("assets/screw/screw.obj", toRadians(45f), 0f, 0f)?: throw Exception("Renderable can't be NULL!"))
        items.add(ModelLoader.loadModel("assets/hammer/hammer.obj", 0f, 0f, 0f)?: throw Exception("Renderable can't be NULL!"))
        titleScreen = ModelLoader.loadModel("assets/titlescreen/untitled.obj", toRadians(0f), 0f, 0f)?: throw Exception("Renderable can't be NULL!")
        saturn.scaleLocal(Vector3f(0.04f))
        saturn.translateGlobal(Vector3f(-150f, 20f, -100f))
        //saturn.rotateLocal(toRadians(90f),0f,0f)
        astronaut.scaleLocal(Vector3f(0.4f))

        earth.scaleLocal(Vector3f(0.3f))
        earth.translateLocal(Vector3f(0f,0f,-300f))

        mars.scaleLocal(Vector3f(0.05f))
        mars.translateLocal(Vector3f(-1000f,-200f,-2000f))

        jupiter.scaleLocal(Vector3f(0.4f))
        jupiter.translateLocal(Vector3f(-550f,0f,200f))

        mercury.scaleLocal(Vector3f(0.05f))
        mercury.translateLocal(Vector3f(3000f,0f,0f))

        venus.scaleLocal(Vector3f(0.3f))
        venus.translateLocal(Vector3f(700f,-20f,500f))

        planets.add(mercury)
        planets.add(venus)
        planets.add(earth)
        planets.add(mars)
        planets.add(jupiter)
        planets.add(saturn)

        shuttle.scaleLocal(Vector3f(0.5f))
        shuttle.translateLocal(Vector3f(-20f,0f,-20f))
        shuttle.translateLocal(Vector3f(-10f,0f,-20f))

        shuttleDestroyed.scaleLocal(Vector3f(0.5f))
        shuttleDestroyed.translateLocal(Vector3f(-20f,0f,-20f))
        shuttleDestroyed.translateLocal(Vector3f(-10f,0f,-20f))

        ufo.scaleLocal(Vector3f(0.1f))
        ufo.translateLocal(Vector3f(-40f, 40f, -200f))

        items[0].translateLocal(Vector3f(-10f,0f,-10f))
        items[0].scaleLocal(Vector3f(0.7f))

        items[1].translateLocal(Vector3f(0f,0f,-10f))
        items[1].scaleLocal(Vector3f(0.1f))

        items[2].translateLocal(Vector3f(10f,0f,-10f))
        items[2].scaleLocal(Vector3f(0.5f))


        asteroids[0].scaleLocal(Vector3f(0.6f))
        asteroids[1].scaleLocal(Vector3f(0.3f))
        asteroids[2].scaleLocal(Vector3f(0.1f))
        asteroids[3].scaleLocal(Vector3f(0.6f))
        asteroids[4].scaleLocal(Vector3f(0.3f))
        asteroids[5].scaleLocal(Vector3f(0.1f))
        asteroids[6].scaleLocal(Vector3f(0.6f))
        asteroids[7].scaleLocal(Vector3f(0.3f))
        asteroids[8].scaleLocal(Vector3f(0.1f))

        asteroids[0].translateLocal(Vector3f(5f,2f,-40f))
        asteroids[1].translateLocal(Vector3f(-5f,3f,-60f))
        asteroids[2].translateLocal(Vector3f(15f,10f,-90f))

        collisionAstronaut = Collision(astronaut)

        for (a in asteroids) listOfAsteroids.add(Asteroid(astronaut, collisionAstronaut, Pair(a,Vector2f(1.8f))))


        MusicPlayer.playMusic("assets/music/spaceMusicV4.wav")

        orthocamera.parent = astronaut
        orthocamera.multiplier = 1.1f
        pointLight = PointLight(Vector3f(0f, 2f, 0f), Vector3f(1f, 1f, 0f),
            Vector3f(1f, 0.5f, 0.1f))
        pointLight2 = PointLight(Vector3f(0f, 4f, 0f), Vector3f(0f, 1f, 0f),
            Vector3f(1f, 0.5f, 0.1f))

        spotLight = SpotLight(Vector3f(0f, 1f, -2f), Vector3f(1f,1f,0.6f),
            Vector3f(0.5f, 0.05f, 0.05f), Vector2f(toRadians(15f), toRadians(30f)))

        dirLight = DirectionalLight(Vector3f(-1f, 0f, 0f), Vector3f(1f,1f,1f))
        spotLight.rotateLocal(toRadians(-10f), PI.toFloat(),0f)

        camera.parent = astronaut
        firstPersonCamera.parent = astronaut
        outroCamera.parent = shuttleDestroyed
        smallFlame.parent = astronaut
        bigFlame.parent = astronaut

        spotLight.parent = astronaut
        moon.parent = earth
        for (cb in planets)  cb.parent = astronaut
        moon.scaleLocal(Vector3f(10f))
        moon.translateLocal(Vector3f(0f,0f,10f))

        ufoBoudingBox = Pair(ufo, Vector2f(3f,3f))
        pointLight.parent = items[1]
        pointLight2.parent = items[0]
        currentCamera = camera
        currentShader = tronShader

        for (a in lifehearts) {
            a.scaleLocal(Vector3f(0.03f))
        }
        lifehearts[0].translateGlobal(Vector3f(-1f, 2f, 0f))
        lifehearts[1].translateGlobal(Vector3f(0f, 2f, 0f))
        lifehearts[2].translateGlobal(Vector3f(1f, 2f, 0f))

        for (b in lifehearts) {
            b.rotateLocal(-1.55f,0f,0f)
        }
        for (a in lifehearts){
            a.parent = astronaut
        }


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
        if (!outro && items.isNotEmpty()) items[0].render(currentShader)
        for (cb in planets) cb.render(currentShader)

        moon.render(currentShader)
        titleScreen.render(currentShader)


        currentShader.setUniform("farbe", Vector3f(1f,0f,0f))
        if (shuttleRepaired) shuttle.render(currentShader)
        else shuttleDestroyed.render(currentShader)
        currentShader.setUniform("farbe", Vector3f(0f,0f,0f))
        for (i in asteroids){
            i.render(currentShader)
        }

        if (!outro) {
            astronaut.render(currentShader)
            currentShader.setUniform("farbe", Vector3f(1f,0f,0f))
            for (a in lifehearts) {
                a.render(currentShader)
            }
            currentShader.setUniform("farbe", Vector3f(1f,1f,1f))
            smallFlame.render(currentShader)
            if (bigFlameRender) bigFlame.render(currentShader)
        }
        currentShader.setUniform("farbe", Vector3f(1f,1f,1f))
        ufo.render(currentShader)


        //currentShader.setUniform("farbe", Vector3f(abs(sin(t)), abs(sin(t/2f)), abs(sin(t/3f))))


    }
    fun collisionResponse (p : Pair<Renderable, Vector2f>) {
        if (collisionAstronaut.checkCollision(p)) {
            val dirVector = astronaut.getWorldPosition().sub(p.first.getWorldPosition()).normalize(0.05f)
            astronaut.translateGlobal(Vector3f(dirVector.x, 0f, dirVector.z))
        }
    }

    fun update(dt: Float, t: Float) {

        earth.rotateLocal(0f, 0.05f * dt,0f)
        moon.rotateAroundPoint(0f, 0.05f * -dt, 0f, Vector3f(0f))
        moon.rotateAroundPoint(0.05f * dt, 0.05f * -dt, 0f, Vector3f(0f))
        moon.rotateLocal(0f,0.05f * dt,0f)
        saturn.rotateLocal(0.01f *dt,0.2f *dt, 0f)

        smallFlame.translateTexture(Vector2f(15f*dt,0f))
        bigFlame.translateTexture(Vector2f(15*dt,0f))

        for (i in items) i.rotateLocal(0f,2f*dt,0f)

        collisionResponse(Pair(shuttleDestroyed,Vector2f(1.8f,4f)))

        if (!outro && !gameOver) for (a in listOfAsteroids) a.update(dt)
        if (collisionAstronaut.checkCollision(Pair(shuttleDestroyed, Vector2f(2f,5f))) && items.isEmpty()) {
            outro = true
            repair = true
        }

        if (repair) {
            if (repairTimer > 0) {
                repairTimer -= dt
                if (repairTimer > 2*dt) shuttleRepaired = true
            }
            else {
                currentCamera = outroCamera
                repair = false
            }
        }

        if (timerActive)
            if (hitCoolDownTimer > 0) hitCoolDownTimer-=dt
            else {
                timerActive = false
                hitCoolDownTimer = hitCoolDownMax
            }

        if (lifehearts.isNotEmpty()) {
            if (listOfAsteroids.any {it.isHit} && !timerActive) {
                lifehearts.removeAt(lifehearts.size - 1)
                timerActive = true
            }
        }
        else gameOver = true

        if (collisionAstronaut.checkCollision(ufoBoudingBox)) {
            val dirVector = astronaut.getWorldPosition().sub(ufo.getWorldPosition()).normalize(0.1f)
            astronaut.translateGlobal(Vector3f(dirVector.x, 0f, dirVector.z))
        }
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
        }

        if (window.getKeyState(GLFW_KEY_F)) currentCamera = camera

        if (window.getKeyState(GLFW_KEY_R)) currentCamera = orthocamera

        if (window.getKeyState(GLFW_KEY_V)) currentCamera = firstPersonCamera
        if (window.getKeyState(GLFW_KEY_Q)) currentCamera = outroCamera
        //pointLight.lightColor = Vector3f(abs(sin(t/3f)), abs(sin(t/4f)), abs(sin(t/2)))

        val norMovementSpeedFactor = 10
        val accMovementSpeedFactor = 20
        val revMovementSpeedFactor = 2




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
        bigFlameRender = false
        if(!outro && !gameOver && !repair) {
            when {
                window.getKeyState(GLFW_KEY_W) -> {
                    if (window.getKeyState(GLFW_KEY_A)) {
                        astronaut.rotateLocal(0f, 1.5f * dt, 0f)
                        for (cb in planets) cb.rotateAroundPoint(0f, 1.5f * -dt, 0f, Vector3f(0f))

                    }
                    if (window.getKeyState(GLFW_KEY_D)) {
                        astronaut.rotateLocal(0f, 1.5f * -dt, 0f)
                        for (cb in planets) cb.rotateAroundPoint(0f, 1.5f * dt, 0f, Vector3f(0f))
                    }
                    if (window.getKeyState(GLFW_KEY_LEFT_SHIFT) && !timeOut) {
                        fuelInUse = true
                        fuelAmount -= 40 * dt
                        astronaut.translateLocal(Vector3f(0f, 0f, accMovementSpeedFactor * -dt))
                        bigFlameRender = true
                    } else {
                        astronaut.translateLocal(Vector3f(0f, 0f, norMovementSpeedFactor * -dt))
                        bigFlameRender = false
                    }
                }
                window.getKeyState(GLFW_KEY_S) -> {

                    if (window.getKeyState(GLFW_KEY_A)) {
                        astronaut.rotateLocal(0f, 1.5f * dt, 0f)
                        for (cb in planets) cb.rotateAroundPoint(0f, 1.5f * -dt, 0f, Vector3f(0f))
                    }
                    if (window.getKeyState(GLFW_KEY_D)) {
                        astronaut.rotateLocal(0f, 1.5f * -dt, 0f)
                        for (cb in planets) cb.rotateAroundPoint(0f, 1.5f * dt, 0f, Vector3f(0f))
                    }
                    astronaut.translateLocal(Vector3f(0f, 0f, revMovementSpeedFactor * dt))
                }
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


        //println(timerCollision)
        if (!outro) {
            if (items.isNotEmpty()) {
                if (collisionAstronaut.checkCollision(Pair(items[0], Vector2f(0.6f)))) {
                    items.removeAt(0)
                    shaderList.removeAt(0)
                    skyboxShaderList.removeAt(0)
                    if (shaderList.isNotEmpty()){
                        currentShader = shaderList[0]
                        currentSkyboxShader = skyboxShaderList[0]
                    }
                }

            }
            else {

                currentShader = tronShader
                currentSkyboxShader = skyBoxShader

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

            if (currentCamera == camera)
                camera.rotateAroundPoint(0f, toRadians(deltaX.toFloat() * 0.05f), 0f, Vector3f(0f))

            else if (currentCamera == firstPersonCamera) {
                astronaut.rotateLocal(0f, -toRadians(deltaX.toFloat() * 0.05f), 0f)
                // negate parent rotation
                for(cb in planets) cb.rotateAroundPoint(0f, toRadians(deltaX.toFloat() * 0.05f), 0f, Vector3f(0f))

            }
            val adjustedDeltaY = deltaY.toFloat() * -0.02f

            if (currentCamera == firstPersonCamera) {
                when{
                    verticalFPCameraPos > camFPMin && verticalFPCameraPos < camFPMax -> {
                        firstPersonCamera.rotateLocal(toRadians(adjustedDeltaY),0f, 0f)
                        verticalFPCameraPos += adjustedDeltaY
                    }
                    verticalFPCameraPos >= camFPMax -> {
                        if(deltaY > 0) {
                            firstPersonCamera.rotateLocal(toRadians(adjustedDeltaY),0f, 0f)
                            verticalFPCameraPos += adjustedDeltaY
                        }
                        else verticalFPCameraPos = camFPMax
                    }
                    verticalFPCameraPos <= camFPMin -> {
                        if(deltaY < 0) {
                            firstPersonCamera.rotateLocal(toRadians(adjustedDeltaY),0f, 0f)
                            verticalFPCameraPos += adjustedDeltaY
                        }
                        else verticalFPCameraPos = camFPMin
                    }
                }

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