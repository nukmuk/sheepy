package me.nukmuk.sheepy

import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.joml.Vector3f
import java.io.File

object AnimationsPlayer {
    private val animations = HashMap<String, Animation>()
    private lateinit var plugin: Sheepy
    private lateinit var task: BukkitTask

    fun animations(): Set<String> {
        return animations.keys
    }

    fun createAnimation(name: String, file: File, location: Location): Animation {
        val animation = Animation(file, null, plugin, location)
        animations.put(name, animation)
        return animation
    }

    fun getAnimation(name: String): Animation? {
        val animation = animations.get(name)
        if (animation == null) return null
        return animation
    }

    fun clearAnimations() {
        animations.values.forEach { it.shouldBeDeleted = true }
    }

    fun initialize(plugin: Sheepy) {
        this.plugin = plugin
        task = object : BukkitRunnable() {
            var processing = false
            var i = 0
            override fun run() {
                if (processing) {
                    sendPlayersActionBar("${ChatColor.RED}previous frame not played yet, animations playing: ${animations.keys} i: $i")
                    return
                }
                processing = true
                val framesToBePlayed = ArrayList<Frame>()
                sendPlayersActionBar("${Config.PRIMARY_COLOR}playing: ${Config.VAR_COLOR}${animations.keys} ${Config.PRIMARY_COLOR}i: ${Config.VAR_COLOR}$i")
                val animationIterator = animations.values.iterator()
                while (animationIterator.hasNext()) {
                    val animation = animationIterator.next()
                    if (animation.shouldBeDeleted) animationIterator.remove()
                    if (!animation.playing.get()) continue
                    val frame = animation.getNextFrame(
                        Vector3f(
                            animation.location.x.toFloat(),
                            animation.location.y.toFloat(), animation.location.z.toFloat()
                        )
                    )
                    if (frame == null) {
                        continue
                        plugin.logger.info("${animation.name} frame null after getNextFrame")
                    }
                    framesToBePlayed.add(frame)
                }
                framesToBePlayed.forEach { frame ->
                    playFrame(frame, 1000, 1.0f)
                }
                i++
                processing = false
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 1L)
    }

    private fun playFrame(frame: Frame, maxParticles: Int, particleScale: Float) {
        val total = frame.animationParticles.size

        val divider: Int = if (maxParticles == 0) 0 else total / maxParticles

        val scaleMultiplier = 1 + Math.clamp(divider / 30.0f, 0.0f, 2.0f)

        frame.animationParticles.forEachIndexed { idx, p ->
            if (p == null) return
            if (divider != 0 && idx % divider != 0) return@forEachIndexed
            frame.animation.world.spawnParticle(
                Particle.DUST,
                p.x.toDouble(),
                p.y.toDouble(),
                p.z.toDouble(),
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

    fun sendPlayersActionBar(message: String) {
        plugin.server.onlinePlayers.filter { it.isOp }
            .forEach { it.sendActionBar(message) }
    }
}