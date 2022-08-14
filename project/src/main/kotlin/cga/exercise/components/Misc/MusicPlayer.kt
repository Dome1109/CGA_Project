package cga.exercise.components.Misc

import java.io.File
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
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
                gainControl.value = -10f



                //clip.start()
                clip.loop(Clip.LOOP_CONTINUOUSLY)
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