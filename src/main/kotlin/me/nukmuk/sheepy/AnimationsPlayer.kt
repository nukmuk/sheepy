package me.nukmuk.sheepy

import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.joml.Vector3f
import java.io.File
import java.util.UUID
import kotlin.math.ceil

object AnimationsPlayer {
    val animations = HashMap<String, Animation>()
    private lateinit var plugin: Sheepy
    private lateinit var task: BukkitTask
    val debugPlayers = HashSet<UUID>()

    var maxParticlesPerTick = 2000

    fun animationNames(): Set<String> {
        return animations.keys
    }

    fun createAnimation(name: String, file: File, location: Location): Animation {
        val animation = Animation(name, file, null, location)
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
                    sendDebugPlayersActionBar("${Config.ERROR_COLOR}previous frame not played yet, animations playing: ${animations.keys} i: $i")
                    return
                }
                if (animations.isEmpty()) return
                processing = true
                val framesToBePlayed = ArrayList<Frame>()
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
                sendDebugPlayersActionBar(
                    "${Config.PRIMARY_COLOR}playing: ${Config.VAR_COLOR}${animations.keys} ${Config.PRIMARY_COLOR}i: ${Config.VAR_COLOR}$i ${Config.PRIMARY_COLOR}total particles loaded: ${Config.VAR_COLOR}${
                        framesToBePlayed.fold(
                            0
                        ) { acc, frame -> acc + frame.animationParticles.size }
                    }"
                )
                if (framesToBePlayed.isEmpty()) {
                    processing = false
                    return
                }
                val maxParticles: Int = ceil((maxParticlesPerTick.toDouble() / framesToBePlayed.size)).toInt()
                framesToBePlayed.forEach { frame ->
                    playFrame(frame, maxParticles)
                }
                i++
                processing = false
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 1L)
    }

    private fun playFrame(frame: Frame, maxParticles: Int) {
        val total = frame.animationParticles.size
        val particleScale = frame.animation.particleScale

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

    fun sendDebugPlayersActionBar(message: String) {
        plugin.server.onlinePlayers.filter { debugPlayers.contains(it.uniqueId) }
            .forEach { it.sendActionBar(Utils.mm.deserialize(message)) }
    }
}