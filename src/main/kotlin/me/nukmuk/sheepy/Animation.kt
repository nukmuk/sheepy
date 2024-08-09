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
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files

class Animation(
    val file: File,
    val player: Player?,
    val plugin: Sheepy,
    var location: Location,
    val animations: HashMap<String, Animation>
) {

    val world: World
        get() = location.world

    var task: BukkitTask? = null

    val currentAnimation = this

    val fileBytes = Files.readAllBytes(file.toPath())
    val bb = ByteBuffer.wrap(fileBytes).order(ByteOrder.LITTLE_ENDIAN)

    var i = 0

    val name: String
        get() = file.nameWithoutExtension

    fun start() {
        // particle spawner & reader
        task = object : BukkitRunnable() {

            override fun run() {
                step()
            }

        }.runTaskTimerAsynchronously(plugin, 0L, 1L)

    }

    fun stop() {
        player?.let { Utils.sendMessage(it, "stopping task $task") }
        task?.cancel()
        task = null
    }

    fun step() {
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
            Utils.sendMessage(
                player!!,
                "read frame with length ${frame.animationParticles.size}, remaining: ${bb.hasRemaining()}"
            )
        } else {
            task?.cancel()
            task = null
            animations.remove(currentAnimation.name)
            return
        }


        val p1 = frame.animationParticles.getOrNull(0)

        val p1pos: Vector = p1?.let { p ->
            Vector(p.x + location.x, p.y + location.y, p.z + location.z)
        } ?: Vector(0, 0, 0)

        player.sendActionBar(Component.text("${ChatColor.GRAY}particles in current frame: ${frame.animationParticles.size}, running for: ${i}, pos: ${p1pos.x.toInt()}, ${p1pos.y.toInt()}, ${p1pos.z.toInt()}"))
        i++
//        Utils.sendMessage(player, "playing frame")
        playFrame(frame, location)
//        Utils.sendMessage(player, "played")

    }

    private fun playFrame(frame: Frame, loc: Location) {
        for (p in frame.animationParticles) {
            if (p == null) continue
            world.spawnParticle(
                Particle.DUST,
                location.x + p.x,
                location.y + p.y,
                location.z + p.z,
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

data class AnimationParticle(val x: Float, val y: Float, val z: Float, val color: Color) {
    val scale: Byte
        get() = color.alpha.toByte()
}