package me.nukmuk.sheepy

import it.unimi.dsi.fastutil.io.FastBufferedInputStream
import me.nukmuk.sheepy.utils.Utils
import org.bukkit.Bukkit
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
    file: File,
    private val player: Player?,
    var location: Location,
) {

    val world: World
        get() = location.world

    private val reader = FastBufferedInputStream(FileInputStream(file))

    private var i = 0

    var playing = false


    var particleScale = 1.0f
    var animationScale = 1.0f
    var animationRotationY = 0.0f
    var animationRotationX = 0.0f
    var animationRotationZ = 0.0f
    var repeat = false
    var renderType: RenderType? = RenderType.PARTICLE

    var shouldBeDeleted = false

    var textForTextRenderer = name[0].toString()
    var textMode = TextMode.BACKGROUND
    var randomRotationMode = RandomRotation.YAW

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
    }

    fun stepFrame() {
        Utils.sendMessage(player!!, "not implemented")
    }

    fun seekToStart() {
        reader.position(0)
    }

    fun readerIsAtFileEnd(): Boolean {
        return reader.position() >= reader.length()
    }

    fun getNextFrame(worldLocationOffset: Vector3f): Frame? {

        if (readerIsAtFileEnd()) {
            if (repeat) {
                seekToStart()
            } else {
                remove()
                Bukkit.getLogger().info("Animation $name ended, returning null as frame")
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
            Bukkit.getLogger().warning("Invalid frame length: $length in animation $name")
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
        return frame
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
}