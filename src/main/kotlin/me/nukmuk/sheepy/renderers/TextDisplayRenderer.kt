package me.nukmuk.sheepy.renderers.packet

import me.nukmuk.sheepy.AnimationParticle
import me.nukmuk.sheepy.Frame
import me.nukmuk.sheepy.Sheepy
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay

object TextDisplayRenderer {

    private var entities = mutableListOf<TextDisplay>()

    fun playFrames(frames: Collection<Frame>, maxParticles: Int, plugin: Sheepy) {
        frames.forEach { frame ->
            frame.animationParticles.forEachIndexed { pointIndex, point ->
                if (point == null) return@forEachIndexed
                render(point, pointIndex, frame, maxParticles, plugin)
            }
        }
    }

    private fun render(
        point: AnimationParticle,
        pointIndex: Int,
        frame: Frame,
        maxParticles: Int,
        plugin: Sheepy
    ) {
        plugin.server.scheduler.runTask(plugin, Runnable {

            if (pointIndex >= maxParticles) return@Runnable
            val world = frame.animation.world
            val location = Location(world, point.x.toDouble(), point.y.toDouble(), point.z.toDouble())
            if (pointIndex >= entities.size) {
            // on spawn
                world.spawn(location, TextDisplay::class.java) { entity ->
                    entities.add(entity)
                    entity.isPersistent = false
                    entity.billboard = Display.Billboard.CENTER
                    entity.backgroundColor = point.color.setAlpha(255)
                }
        }


        // every frame
            val entity = entities.getOrNull(pointIndex)
            if (entity == null) return@Runnable
            entity.teleport(location)
            entity.backgroundColor = point.color.setAlpha(255)
        })

    }

    fun removeEntities() {
        entities.forEach { it.remove() }
        entities.clear()
    }
}
