package me.nukmuk.sheepy

import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.util.concurrent.atomic.AtomicBoolean

class Animation(
    private val file: File,
    private val player: Player?,
    private val plugin: Sheepy,
    private var location: Location,
) {

    private val world: World
        get() = location.world

    private val currentAnimation = this

    private val raf = RandomAccessFile(file, "r")

    private var i = 0

    private var playing = AtomicBoolean(false)

    val name: String
        get() = file.nameWithoutExtension

    lateinit var bytes: ByteArray
    lateinit var bb: ByteBuffer

    private var task = object : BukkitRunnable() {
        var processing = false
        var shouldLoad = true
        var loading = false
        override fun run() {
            player!!.sendActionBar("processing $processing, shouldLoad $shouldLoad, loading $loading, playing: ${playing.get()} i: $i")
            if (processing || loading) return
            if (!playing.get()) return
            processing = true
            if (shouldLoad) {
                loading = true
                shouldLoad = false
                bytes = Files.readAllBytes(file.toPath())
                bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
                Utils.sendMessage(player, "loaded ${bytes.size} bytes")
                loading = false
                processing = false
                return
            }
            if (!bb.hasRemaining()) bb.position(0)
            step(bb)
            processing = false
        }
    }.runTaskTimerAsynchronously(plugin, 0L, 1L)


    fun start() {
        playing.set(true)
    }

    fun stop() {
        player?.let { Utils.sendMessage(it, "pausing animation $name") }
        playing.set(false)
    }

    fun remove() {
        player?.let { Utils.sendMessage(it, "removing animation $name") }
        playing.set(false)
        task.cancel()
    }

    fun stepFrame() {
        Utils.sendMessage(player!!, "not implemented")
    }

    private fun step(bb: ByteBuffer) {
        var frame: Frame

        if (bb.hasRemaining()) {
            val length = bb.getShort().toInt()

            frame = Frame(arrayOfNulls<AnimationParticle>(length))

            // loop over particles and add them to frame
            for (i in 0 until length) {
                frame.animationParticles[i] = AnimationParticle(
                    x = getPosComponent(bb),
                    y = getPosComponent(bb),
                    z = getPosComponent(bb),
                    color = Color.fromARGB(bb.getInt()),
                )
            }
//            Utils.sendMessage(
//                player!!,
//                "read frame with length ${frame.animationParticles.size}, remaining: ${bb.hasRemaining()}"
//            )
        } else {
            playing.set(false)
            Utils.sendMessage(player!!, "bytebuffer empty")
            return
        }


        val p1 = frame.animationParticles.getOrNull(0)

        val p1pos: Vector = p1?.let { p ->
            Vector(p.x + location.x, p.y + location.y, p.z + location.z)
        } ?: Vector(0, 0, 0)

//        player?.sendActionBar(Component.text("${ChatColor.GRAY}particles in current frame: ${frame.animationParticles.size}, running for: ${i}, pos: ${p1pos.x.toInt()}, ${p1pos.y.toInt()}, ${p1pos.z.toInt()}"))
        i++
        playFrame(frame, location)
    }

    private fun playFrame(frame: Frame, loc: Location) {
        for (p in frame.animationParticles) {
            if (p == null) continue
            world.spawnParticle(
                Particle.DUST,
                loc.x + p.x,
                loc.y + p.y,
                loc.z + p.z,
                1,
                0.0,
                0.0,
                0.0,
                0.0,
                Particle.DustOptions(p.color, p.scale.toFloat() / 255 * 3)
            )
        }
    }

    private fun getPosComponent(bb: ByteBuffer): Float {
        val posComponent = bb.getShort()
        val posComponentFloat = Utils.convertToFloat(posComponent.toInt())
        return posComponentFloat
    }
}

data class Frame(val animationParticles: Array<AnimationParticle?>)

data class AnimationParticle(
    val x: Float,
    val y: Float,
    val z: Float,
    val color: Color
) {
    val scale: Byte
        get() = color.alpha.toByte()
}