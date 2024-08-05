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
import java.util.concurrent.ArrayBlockingQueue

class Animation(val file: File, val player: Player?, val plugin: Sheepy, var location: Location) {

    var shouldStop = false
    val frames = ArrayBlockingQueue<Frame>(100)
    val world: World
        get() = location.world

    fun start() {

        object : BukkitRunnable() {
            override fun run() {
                Utils.sendMessage(player!!, "hello")

            }
        }.runTaskAsynchronously(plugin)

        // particle spawner
//        runnable = object : BukkitRunnable() {
//            override fun run() {
//                player?.sendActionBar(Component.text(ChatColor.GRAY.toString() + "frames in queue: " + frames.size))
//                val frame = frames.take()
//                if (frame == null) {
//                    if (shouldStop) {
//                        player?.sendActionBar(Component.text(ChatColor.GREEN.toString() + "end :)"))
//                        frames.clear()
//                        this.cancel()
//                        return
//                    }
//                    player?.sendActionBar(Component.text(ChatColor.RED.toString() + "lagaa"))
//                    return
//                }
//                playFrame(frame, location)
//            }
//
//            fun playFrame(frame: Frame, loc: Location) {
//                for (frame in frames) {
//                    for (p in frame.animationParticles) {
//                        if (p == null) continue
//                        world.spawnParticle(
//                            Particle.CLOUD,
//                            location.add(p.x.toDouble(), p.y.toDouble(), p.z.toDouble()),
//                            1
//                        )
//                    }
//                }
//            }
//
//        }.runTaskTimer(plugin, 0L, 1L)


        // file loader
//        object : BukkitRunnable() {
//            override fun run() {
//                Utils.sendMessage(player!!, "0")
//                try {
//                    Utils.sendMessage(player!!, "0.5")
//                    val fileBytes = Files.readAllBytes(file.toPath())
//                    Utils.sendMessage(player!!, "0.6")
//                    val bb = ByteBuffer.wrap(fileBytes)
//                    Utils.sendMessage(player!!, "0.7")
//                    bb.order(ByteOrder.LITTLE_ENDIAN)
//
//                    Utils.sendMessage(player!!, "1")
//
//
//                    while (bb.hasRemaining() && !shouldStop) {
//                        Utils.sendMessage(player, "2")
//                        val length = bb.getShort()
////                        val frame = arrayOfNulls<Particle>(length.toInt())
//                        val frame = Frame(arrayOfNulls<AnimationParticle>(length.toInt()))
//                        Utils.sendMessage(player, "3")
//
//                        // loop over particles and add them to frame
//                        for (i in 0 until length) {
//                            Utils.sendMessage(player, "4")
//                            frame.animationParticles[i] = AnimationParticle(
//                                x = getPosComponent(bb),
//                                y = getPosComponent(bb),
//                                z = getPosComponent(bb),
//                                color = Color.fromARGB(bb.getInt()),
//                            )
//                            Utils.sendMessage(player, "5")
//                        }
//                        frames.put(frame)
//                        Utils.sendMessage(player!!, "read frame with length ${frame.animationParticles.size}")
//                    }
//                    shouldStop = true
//                } catch (e: Exception) {
//                    //                    Bukkit.getLogger().info(e.toString());
//                    shouldStop = true
//                    if (player != null) {
//                        Utils.sendMessage(player, "${ChatColor.RED}error streaming file: $e")
//                    }
//                }
//            }
//
//            fun getPosComponent(bb: ByteBuffer): Float {
//                val posComponent = bb.getShort()
//                val posComponentFloat = Utils.convertToFloat(posComponent.toInt())
//                return posComponentFloat
//            }
//        }.runTask(plugin)
    }

    fun stop() {
        shouldStop = true
    }
}

data class Frame(val animationParticles: Array<AnimationParticle?>)

data class AnimationParticle(val x: Float, val y: Float, val z: Float, val color: Color) {
    val scale: Byte
        get() = color.alpha.toByte()
}