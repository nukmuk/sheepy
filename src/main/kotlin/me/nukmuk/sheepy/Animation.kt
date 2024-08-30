package me.nukmuk.sheepy

import it.unimi.dsi.fastutil.io.FastBufferedInputStream
import me.nukmuk.sheepy.utils.RepeatAnimationsConfigUtil
import me.nukmuk.sheepy.utils.Utils
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.joml.Vector3f
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Animation(
    val name: String,
    val file: File,
    private val player: Player?,
    location: Location,
    val repeat: Boolean
) {

    var location = location
        set(value) {
            value.pitch = 0f
            value.yaw = 0f
            field = value
            updateIfRepeating("location", value)
        }


    val world: World
        get() = location.world


    private val reader = FastBufferedInputStream(FileInputStream(file), 1024 * 64)

    private var i = 0

    var playing = false

    var particleScale = 1.0f
        set(value) {
            field = value
            updateIfRepeating("particlescale", value)
        }

    var animationScale = 1.0f
        set(value) {
            field = value
            updateIfRepeating("animationscale", value)
        }
    var animationRotationY = 0.0f
        set(value) {
            field = value
            updateIfRepeating("rotationY", value)
        }
    var animationRotationX = 0.0f
        set(value) {
            field = value
            updateIfRepeating("rotationX", value)
        }
    var animationRotationZ = 0.0f
        set(value) {
            field = value
            updateIfRepeating("rotationZ", value)
        }
    var renderType: RenderType? = RenderType.PARTICLE
        set(value) {
            field = value
            updateIfRepeating("rendertype", value)
        }

    var shouldBeDeleted = false

    var textForTextRenderer = name[0].toString()
        set(value) {
            field = value
            updateIfRepeating("textForTextRenderer", value)
        }
    var textMode = TextMode.BACKGROUND
        set(value) {
            field = value
            updateIfRepeating("textMode", value)
        }
    var randomRotationMode = RandomRotation.YAW
        set(value) {
            field = value
            updateIfRepeating("randomRotationMode", value)
        }

//    var singleFrame: Frame? = null

    fun start() {
        playing = true
    }

    fun stop() {
        player?.let { Utils.sendMessage(it, "pausing animation $name") }
        playing = false
    }

    fun remove() {
        player?.let { Utils.sendMessage(it, "removing animation $name") }
        playing = false
        shouldBeDeleted = true
        RepeatAnimationsConfigUtil.remove(name)
    }

    fun removeWithoutRemovingFromConfig() {
        player?.let { Utils.sendMessage(it, "removing animation $name") }
        playing = false
        shouldBeDeleted = true

    }

    fun seekToStart() {
        reader.position(0)
        i = 0
    }

    fun readerIsAtFileEnd(): Boolean {
        return reader.position() >= reader.length()
    }

    fun getNextFrame(worldLocationOffset: Vector3f): Frame? {
//        if (singleFrame != null) return singleFrame
//        var shouldSaveSingleFrame = false

        try {
            if (readerIsAtFileEnd()) {
                if (repeat) {
                    Utils.sendDebugMessage("ended: $name i: $i")
                    if (i == 1) {
                        Utils.sendDebugMessage("single frame detected")
//                        shouldSaveSingleFrame = true
                    }
                    seekToStart()
                } else {
                    remove()
                    l("Animation $name ended, returning null as frame")
                    return null
                }
            }


            val frame: Frame

            if (reader.available() < 14) { // length + at least 1 particle
                playing = false
                Utils.sendDebugMessage("Animation $name buffer empty")
                remove()
                return null
            }

            val length = getShort().toInt()

            if (length < 0) {
                Sheepy.instance.logger.warning("Invalid frame length: $length in animation $name")
                return null
            }

            frame = Frame(
                arrayOfNulls<AnimationParticle>(length),
                this
            )

            // loop over particles and add them to frame
            for (i in 0 until length) {
                val position = Vector3f(
                    getPosComponent() * animationScale,
                    getPosComponent() * animationScale,
                    getPosComponent() * animationScale
                )
                position.rotateY(animationRotationY)
                position.rotateX(animationRotationX)
                position.rotateZ(animationRotationZ)
                position.add(worldLocationOffset)
                frame.animationParticles[i] = AnimationParticle(
                    x = position.x,
                    y = position.y,
                    z = position.z,
                    color = Color.fromARGB(getInt()),
                    frame
                )
            }

            i++

//            if (shouldSaveSingleFrame) singleFrame = frame

            return frame
        } catch (e: Exception) {
            Sheepy.instance.logger.warning("Error reading $name, i: $i, error: $e")
            return null
        }
    }

    private fun getPosComponent(): Float {
        val posComponent = getShort()
        val posComponentFloat = Utils.convertToFloat(posComponent.toInt())
        return posComponentFloat
    }

    private fun getShort(): Short {
        return ByteBuffer.wrap(reader.readNBytes(2)).order(ByteOrder.LITTLE_ENDIAN).getShort()
    }

    private fun getInt(): Int {
        return ByteBuffer.wrap(reader.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).getInt()
    }

    private fun updateIfRepeating(property: String, value: Any?) {
        var value: Any? = value
        when (value) {
            is RenderType, is TextMode, is RandomRotation -> value = value.toString()
        }

        if (repeat)
            RepeatAnimationsConfigUtil.updateValueIfRepeating("$name.$property", value)
    }
}