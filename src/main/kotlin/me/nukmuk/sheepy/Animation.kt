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
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files

class Animation(
    val file: File,
    val player: Player?,
    val plugin: Sheepy,
    var location: Location,
    val animations: ArrayList<Animation>
) {

    val world: World
        get() = location.world

    var task: BukkitTask? = null

    val currentAnimation = this

    val fileBytes = Files.readAllBytes(file.toPath())
    val bb = ByteBuffer.wrap(fileBytes).order(ByteOrder.LITTLE_ENDIAN)

    fun start() {
        // particle spawner & reader
        task = object : BukkitRunnable() {
            var i = 0

            override fun run() {

                var frame = Frame(arrayOf<AnimationParticle?>())

                try {
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
                        this.cancel()
                        task = null
                        animations.remove<Animation>(currentAnimation)
                        return
                    }
                } catch (e: Exception) {
                    if (player != null) {
                        Utils.sendMessage(player, "${ChatColor.RED}error streaming file: ${e.message}")
                    }
                }


                val p1 = frame.animationParticles.getOrNull(0)
                player?.sendActionBar(Component.text("${ChatColor.GRAY}size of frame in queue: ${frame.animationParticles.size}, running for: ${i}, pos: ${p1?.x?.toInt()}, ${p1?.y?.toInt()}, ${p1?.z?.toInt()}"))
                i++
                Utils.sendMessage(player!!, "playing frame")
                playFrame(frame, location)
                Utils.sendMessage(player, "played")


            }

            fun playFrame(frame: Frame, loc: Location) {
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
                        Particle.DustOptions(p.color, p.scale.toFloat() / 255 * 1)
                    )
                }
            }

            fun getPosComponent(bb: ByteBuffer): Float {
                val posComponent = bb.getShort()
                val posComponentFloat = Utils.convertToFloat(posComponent.toInt())
                return posComponentFloat
            }

        }.runTaskTimerAsynchronously(plugin, 0L, 1L)


        // file loader
        /*
        val loaderTask = object : BukkitRunnable() {
            override fun run() {
                try {
                    val fileBytes = Files.readAllBytes(file.toPath())
                    val bb = ByteBuffer.wrap(fileBytes)
                    bb.order(ByteOrder.LITTLE_ENDIAN)



                    while (bb.hasRemaining() && !shouldStop) {
                        val length = bb.getShort()
//                        val frame = arrayOfNulls<Particle>(length.toInt())
                        val frame = Frame(arrayOfNulls<AnimationParticle>(length.toInt()))

                        // loop over particles and add them to frame
                        for (i in 0 until length) {
                            frame.animationParticles[i] = AnimationParticle(
                                x = getPosComponent(bb),
                                y = getPosComponent(bb),
                                z = getPosComponent(bb),
                                color = Color.fromARGB(bb.getInt()),
                            )
                        }
                        frames.put(frame)
                        Utils.sendMessage(
                            player!!,
                            "read frame with length ${frame.animationParticles.size}, remaining: ${bb.hasRemaining()}"
                        )
                    }
                    shouldStop = true
                } catch (e: Exception) {
                    shouldStop = true
                    if (player != null) {
                        Utils.sendMessage(player, "${ChatColor.RED}error streaming file: $e")
                    }
                }
            }

            fun getPosComponent(bb: ByteBuffer): Float {
                val posComponent = bb.getShort()
                val posComponentFloat = Utils.convertToFloat(posComponent.toInt())
                return posComponentFloat
            }
        }.runTaskAsynchronously(plugin)

        tasks.add(loaderTask)
         */
    }

    fun stop() {
        player?.let { Utils.sendMessage(it, "stopping task $task") }
        task?.cancel()
        task = null
    }
}

data class Frame(val animationParticles: Array<AnimationParticle?>)

data class AnimationParticle(val x: Float, val y: Float, val z: Float, val color: Color) {
    val scale: Byte
        get() = color.alpha.toByte()
}