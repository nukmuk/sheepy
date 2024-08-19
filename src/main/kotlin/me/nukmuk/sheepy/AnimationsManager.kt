package me.nukmuk.sheepy

import me.nukmuk.sheepy.renderers.EntityRenderer
import me.nukmuk.sheepy.renderers.ParticleRenderer
import org.bukkit.Location
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.joml.Vector3f
import java.io.File
import java.util.UUID
import kotlin.math.ceil

object AnimationsManager {
    val animations = HashMap<String, Animation>()
    private lateinit var plugin: Sheepy
    private lateinit var task: BukkitTask
    val debugPlayers = HashSet<UUID>()

    var maxParticlesPerTick = 2000

    private var _animsInFolder = listOf<File>()
    val animsInFolder: List<File>
        get() = _animsInFolder

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
//        plugin.server.scheduler.runTaskLater(plugin, Runnable {
//            Utils.sendDebugMessage("clearing animations...")
//            EntityRenderer.sendRemoveAllEntitiesPacket(plugin)
//        }, 20L)
    }

    fun initialize(plugin: Sheepy) {
        this.plugin = plugin
        plugin.server.onlinePlayers.filter { it.isOp }.forEach { debugPlayers.add(it.uniqueId) }
        task = object : BukkitRunnable() {
            var processing = false
            var i = 0
            override fun run() {
                if (processing) {
                    sendDebugPlayersActionBar("${Config.ERROR_COLOR}previous frame still processing, animations playing: ${animations.keys} i: $i")
                    return
                }
                if (animations.values.filter { it.renderType == RenderType.BLOCK_DISPLAY }
                        .find { it.playing } == null) EntityRenderer.clean(plugin)
                if (animations.isEmpty()) return
                processing = true
                val framesToBePlayed = ArrayList<Frame>()
                val animationIterator = animations.values.iterator()
                while (animationIterator.hasNext()) {
                    val animation = animationIterator.next()
                    if (animation.shouldBeDeleted) animationIterator.remove()
                    if (!animation.playing) continue
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

                RenderType.entries.forEach { entry ->
                    val framesOfThisType = framesToBePlayed.filter { it.animation.renderType == entry }
                    when (entry) {
                        RenderType.PARTICLE -> ParticleRenderer.playFrames(framesOfThisType, maxParticles)
                        RenderType.BLOCK_DISPLAY -> EntityRenderer.playFramesWithBlockDisplays(
                            framesOfThisType,
                            maxParticles,
                            plugin
                        )
                    }
                }

                i++
                processing = false
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 1L)
    }

    fun sendDebugPlayersActionBar(message: String) {
        plugin.server.onlinePlayers.filter { debugPlayers.contains(it.uniqueId) }
            .forEach { it.sendActionBar(Utils.mm.deserialize(message)) }
    }

    fun getAnimsInFolder(plugin: Sheepy): List<File> {
        val pluginFolder = plugin.dataFolder
        val animFolder = File(pluginFolder, "")

        val files = animFolder.listFiles()?.filter { file -> file.extension == Config.FILE_EXTENSION }

        if (files != null) {
            _animsInFolder = files
        } else {
            return listOf()
        }

        return files.toList()
    }
}

enum class RenderType {
    PARTICLE,
    BLOCK_DISPLAY
}