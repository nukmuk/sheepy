package me.nukmuk.sheepy.renderers

import me.nukmuk.sheepy.AnimationParticle
import me.nukmuk.sheepy.Frame
import me.nukmuk.sheepy.Sheepy
import me.nukmuk.sheepy.utils.Utils
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay
import java.util.concurrent.ConcurrentLinkedQueue

object TextDisplayRenderer {

    private val entities = mutableListOf<TextDisplay>()

    @Volatile
    private var pointsToSpawn = mutableListOf<AnimationParticle>()

    fun initializeTextDisplaysEntityHandler(plugin: Sheepy) {
        var processing = false
        plugin.server.scheduler.runTaskTimer(plugin, Runnable {
//            Utils.sendDebugMessage("entities: ${entities.size} pointsToSpawn: ${pointsToSpawn.size}, processing $processing")
            if (processing) {
//                Utils.sendDebugMessage("${this.javaClass.simpleName} still processing!")
                return@Runnable
            }
            processing = true
            pointsToSpawn.forEachIndexed { pointIndex, point ->
                val world = plugin.server.worlds.first()
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
                val entity = entities[pointIndex]
                entity.teleport(location)
                entity.backgroundColor = point.color.setAlpha(255)
            }

            // remove extra leftovers from previous frame
            if (pointsToSpawn.size < entities.size) {
                Utils.sendDebugMessage("removing entities, entities: ${entities.size} pointsToSpawn: ${pointsToSpawn.size}")
                for (i in entities.size - 1 downTo pointsToSpawn.size) {
//                    Utils.sendDebugMessage("removing entity: $i, entities: ${entities.size} pointsToSpawn: ${pointsToSpawn.size}")
                    entities[i].remove()
                    entities.remove(entities[i])
                }
            } else {
//                Utils.sendDebugMessage("not removing entities, entities: ${entities.size} pointsToSpawn: ${pointsToSpawn.size}")
            }
            pointsToSpawn.clear()
            processing = false
        }, 0, 1)
    }

    fun prepareFrames(frames: Collection<Frame>, maxParticles: Int, plugin: Sheepy) {
        pointsToSpawn.clear()
        frames.forEach { frame ->
            frame.animationParticles.forEachIndexed { pointIndex, point ->
                if (point == null) return@forEachIndexed
                if (pointIndex < maxParticles) {
                    pointsToSpawn.add(point)
                }
            }
        }
    }


    fun removeEntities() {
        entities.forEach { it.remove() }
        entities.clear()
    }
}
