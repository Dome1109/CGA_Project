package cga.exercise.components.Misc

import java.io.File
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.FloatControl
import javax.sound.sampled.FloatControl.Type

object MusicPlayer {

    fun playMusic (musicLoc: String) {

        try {
            val musicPath = File(musicLoc)
            if (musicPath.exists()){
                val audioinput = AudioSystem.getAudioInputStream(musicPath)
                val clip = AudioSystem.getClip()
                clip.open(audioinput)
                var gainControl = clip.getControl(Type.MASTER_GAIN) as FloatControl
                println(gainControl.value)
                gainControl.value = -10f
                println(gainControl.value)


                clip.start()
            }
            else {
                println("File not found!")
            }
        }
        catch (ex: Exception) {
            ex.printStackTrace()
        }

    }
}