package me.nukmuk.sheepy

import it.unimi.dsi.fastutil.io.FastBufferedInputStream
import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicBoolean

class Animation(
    private val file: File,
    private val player: Player?,
    plugin: Sheepy,
    private var location: Location,
    private val animations: HashMap<String, Animation>,
) {

    private val world: World
        get() = location.world

    private val reader = FastBufferedInputStream(FileInputStream(file))

    private var i = 0

    private var playing = AtomicBoolean(false)

    val name: String
        get() = file.nameWithoutExtension


    var particleScale = 1.0f
    var animationScale = 1.0f
    var repeat = false

    var maxParticlesPerFrame = 1000

    private var task = object : BukkitRunnable() {
        var processing = false
        override fun run() {
//            player!!.sendActionBar("processing $processing, shouldLoad $shouldLoad, loading $loading, playing: ${playing.get()} i: $i")
            if (processing) {
                player?.sendActionBar("${ChatColor.RED}previous frame not played yet")
                return
            }
            if (!playing.get()) return
            processing = true
            if (reader.position() == reader.length()) {
                if (repeat) {
                    reader.position(0)
                } else {
                    Utils.sendMessage(player!!, "stopping anim $name @end")
                    playing.set(false)
                    processing = false
                    remove()
                    animations.remove(name)
                    return
                }
            }
            step()
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

    private fun step() {

        var frame: Frame

        if (reader.available() > 0) {
            val length = getShort().toInt()

            frame = Frame(arrayOfNulls<AnimationParticle>(length))

            // loop over particles and add them to frame
            for (i in 0 until length) {
                frame.animationParticles[i] = AnimationParticle(
                    x = getPosComponent(),
                    y = getPosComponent(),
                    z = getPosComponent(),
                    color = Color.fromARGB(getInt()),
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

        player?.sendActionBar(Component.text("${ChatColor.GRAY}particles in current frame: ${frame.animationParticles.size}, running for: ${i}, pos: ${p1pos.x.toInt()}, ${p1pos.y.toInt()}, ${p1pos.z.toInt()}"))
        i++
        playFrame(frame, location)
    }

    private fun playFrame(frame: Frame, loc: Location) {
        val total = frame.animationParticles.size

        val divider: Int = if (maxParticlesPerFrame == 0) 0 else total / maxParticlesPerFrame

        val scaleMultiplier = 1 + Math.clamp(divider / 30.0f, 0.0f, 2.0f)

        frame.animationParticles.forEachIndexed { idx, p ->
            if (p == null) return
            if (divider != 0 && idx % divider != 0) return@forEachIndexed
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
                Particle.DustOptions(p.color, p.scale.toFloat() / 255 * particleScale * scaleMultiplier * 5),
                true
            )
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