package me.nukmuk.sheepy.frameRenderers

import me.nukmuk.sheepy.Animation
import me.nukmuk.sheepy.ColorUtils
import me.nukmuk.sheepy.Frame
import me.nukmuk.sheepy.Sheepy
import me.nukmuk.sheepy.Utils
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.EntityType
import net.minecraft.world.phys.Vec3
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.joml.Vector3f
import java.util.UUID

typealias EntityList = MutableList<Int>

object EntityRenderer {

    val reservedEntityIds = IntArray(16384)

    //    private var entitiesAlive = 0
    private var entitiesAliveWithAnimations = HashMap<Animation, EntityList>()

    fun playFramesWithBlockDisplays(frames: List<Frame>, maxParticles: Int, plugin: Sheepy) {
        plugin.server.onlinePlayers.forEachIndexed { playerIndex, player ->
            val craftPlayer = player as CraftPlayer
            val connection = craftPlayer.handle.connection

            var globalParticleIndex = 0

            frames.forEach { frame ->

                val total = frame.animationParticles.size
                val particleScale = frame.animation.particleScale

                val divider: Int = if (maxParticles == 0) 0 else total / maxParticles

//        val scaleMultiplier = 1 + Math.clamp(divider / 30.0f, 0.0f, 2.0f)


                frame.animationParticles.forEachIndexed { pointIndex, point ->
                    if (point == null) return
                    if (divider != 0 && pointIndex % divider != 0) return@forEachIndexed

                    if (globalParticleIndex > maxParticles) return@forEachIndexed

//                Utils.sendMessage(
//                    player,
//                    "Sending packet :) index: $particleIndex entityId: ${AnimationsManager.reservedEntityIds[particleIndex]}"
//                )


                    val block = ColorUtils.getBlockWithColor(point.color)

                    if (globalParticleIndex > entitiesAliveWithAnimations.values.fold(0) { acc, list -> acc + list.size }) {
                        Utils.sendMessage(player, "Creating block index $globalParticleIndex")
                        connection.sendPacket(
                            ClientboundAddEntityPacket(
                                reservedEntityIds[globalParticleIndex], UUID.randomUUID(),
                                frame.animation.location.x,
                                frame.animation.location.y,
                                frame.animation.location.z,
                                0.0f,
                                0.0f,
                                EntityType.BLOCK_DISPLAY,
                                0,
                                zeroVec,
                                0.0
                            )
                        )
                        if (entitiesAliveWithAnimations[frame.animation] == null)
                            entitiesAliveWithAnimations[frame.animation] = mutableListOf()
                        entitiesAliveWithAnimations[frame.animation]?.add(globalParticleIndex)
                    }

                    val metasCreated = listOf(
                        SynchedEntityData.DataValue(
                            23,
                            EntityDataSerializers.BLOCK_STATE,
                            block.defaultBlockState()
                        ),
                        SynchedEntityData.DataValue(
                            11,
                            EntityDataSerializers.VECTOR3,
                            Vector3f(
                                (point.x - frame.animation.location.x).toFloat(),
                                (point.y - frame.animation.location.y).toFloat(),
                                (point.z - frame.animation.location.z).toFloat()
                            )
//                        Vector3f(point.x, point.y, point.z)
                        ),

                        )

                    connection.send(
                        ClientboundSetEntityDataPacket(
                            reservedEntityIds[globalParticleIndex],
                            metasCreated
                        )
                    )
                    globalParticleIndex++
                }
            }
            if (globalParticleIndex < entitiesAliveWithAnimations.values.fold(0) { acc, list -> acc + list.size } - 1) {
                val aliveIndex = entitiesAliveWithAnimations.values.fold(0) { acc, list -> acc + list.size } - 1
                val correctIndex = globalParticleIndex

                val idsToRemove = reservedEntityIds.slice(correctIndex + 1..aliveIndex)
//                Utils.sendMessage(player, "Removed $idsToRemove")
                connection.send(ClientboundRemoveEntitiesPacket(*idsToRemove.toIntArray()))
            }
        }

    }

    fun initializeEntityIds(plugin: Sheepy) {
        val entityType = EntityType.PIG
        val level = (plugin.server.worlds[0] as CraftWorld).handle
        repeat(reservedEntityIds.size) { index ->
            val entity = entityType.create(level)
            reservedEntityIds[index] = entity?.id ?: -1
        }
    }

    fun sendRemoveAllEntitiesPacket(plugin: Sheepy) {
        for (player in plugin.server.onlinePlayers) {
            val craftPlayer = player as CraftPlayer
            val connection = craftPlayer.handle.connection
            connection.send(ClientboundRemoveEntitiesPacket(*reservedEntityIds))
        }
        entitiesAliveWithAnimations.clear()
    }

    fun clean(plugin: Sheepy) {
        val alive = entitiesAliveWithAnimations.values.fold(0) { acc, list -> acc + list.size }
        if (alive > 0) {
            plugin.logger.info("Running EntityRenderer cleanup for $alive entities")
            sendRemoveAllEntitiesPacket(plugin)
        }
    }

    val zeroVec = Vec3(0.0, 0.0, 0.0)
}