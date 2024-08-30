package me.nukmuk.sheepy.renderers

import me.nukmuk.sheepy.AnimationParticle
import me.nukmuk.sheepy.AnimationsManager
import me.nukmuk.sheepy.Frame
import me.nukmuk.sheepy.Sheepy
import me.nukmuk.sheepy.ShouldBeLeftInWorld
import me.nukmuk.sheepy.TextMode
import me.nukmuk.sheepy.renderers.packet.BlockDisplayPacketRenderer
import me.nukmuk.sheepy.utils.Utils
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f

object TextDisplayRenderer {

    private val entities = mutableListOf<TextDisplay>()

    private var pointsToSpawn = mutableListOf<AnimationParticle>()

    fun initializeTextDisplaysEntityHandler(plugin: Sheepy) {
        var processing = false
        plugin.server.scheduler.runTaskTimer(plugin, Runnable {
            try {
//                Utils.sendDebugMessage("entities: ${entities.size} pointsToSpawn: ${pointsToSpawn.size}, processing $processing")
                if (processing) {
                    Utils.sendDebugMessage("${this.javaClass.simpleName} still processing!")
                    return@Runnable
                }
                processing = true
                val pointsIterator = pointsToSpawn.iterator()
                var pointIndex = 0
                while (pointsIterator.hasNext()) {
                    val point = pointsIterator.next()
                    val animation = point.frame.animation
                    val world = plugin.server.worlds.first()
                    val location = Location(world, point.x.toDouble(), point.y.toDouble(), point.z.toDouble())

                    // spawn entities for new points that have a higher index than the previous highest one
                    if (pointIndex >= entities.size) {
                        // on spawn
                        world.spawn(location, TextDisplay::class.java) { entity ->
                            entity.isPersistent =
                                animation.shouldBeLeftInWorld == ShouldBeLeftInWorld.YES_AND_MAKE_PERSISTENT
                            entities.add(entity)
                            entity.billboard = Display.Billboard.CENTER
                            entity.backgroundColor = point.color.setAlpha(255)
                        }
                    }

                    // every frame
                    val entity = entities[pointIndex]
                    try {
                        entity.teleport(location)
                        val animation = point.frame.animation
                        var scale = BlockDisplayPacketRenderer.getBlockScale(
                            AnimationsManager.maxParticlesPerTick,
                            point.frame,
                            point
                        ) * 8
                        when (animation.textMode) {
                            TextMode.TEXT -> {
                                entity.text(
                                    Component.text(animation.textForTextRenderer)
                                        .color(TextColor.color(point.color.asRGB()))
                                )
                                entity.backgroundColor = Color.fromARGB(0, 0, 0, 0)
                            }

                            TextMode.BACKGROUND -> {
                                entity.backgroundColor = point.color.setAlpha(255)
                                scale *= 5
                            }
                        }
                        entity.transformation = Transformation(
                            Vector3f(),
                            AxisAngle4f(),
                            Vector3f(scale, scale, scale),
                            AxisAngle4f()
                        )
                    } catch (e: Exception) {
                        plugin.logger.warning("TextDisplayRenderer error @everyframe: ${e.stackTraceToString()}")
                        processing = false
                        return@Runnable
                    }
                    if (animation.shouldBeLeftInWorld != ShouldBeLeftInWorld.NO) {
                        entities.remove(entity)
                        pointsIterator.remove()
                    } else {
                        pointIndex++
                    }
                }

                // remove extra leftovers from previous frame
                if (pointsToSpawn.size < entities.size) {
//                Utils.sendDebugMessage("removing entities, entities: ${entities.size} pointsToSpawn: ${pointsToSpawn.size}")
                    for (i in entities.size - 1 downTo pointsToSpawn.size) {
//                    Utils.sendDebugMessage("removing entity: $i, entities: ${entities.size} pointsToSpawn: ${pointsToSpawn.size}")
                        entities[i].remove()
                        entities.remove(entities[i])
                    }
                } else {
//                Utils.sendDebugMessage("not removing entities, entities: ${entities.size} pointsToSpawn: ${pointsToSpawn.size}")
                }
                pointsToSpawn.clear()
//                processing = false
            } catch (e: Exception) {
                plugin.logger.warning("TextDisplayRenderer error: ${e.stackTraceToString()}")
            } finally {
                processing = false
            }
        }, 0, 1)
    }

    fun prepareFrames(frames: Collection<Frame>, maxParticles: Int, plugin: Sheepy) {
        plugin.server.scheduler.runTask(plugin, Runnable {

            pointsToSpawn.clear()
            frames.forEach { frame ->
                frame.animationParticles.forEachIndexed { pointIndex, point ->
                    if (point == null) return@forEachIndexed
                    if (pointIndex < maxParticles || maxParticles == 0) {
                        pointsToSpawn.add(point)
                    }
                }
            }
        })
    }
}
