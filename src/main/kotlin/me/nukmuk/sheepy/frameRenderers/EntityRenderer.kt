package me.nukmuk.sheepy.frameRenderers

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
import kotlin.math.min

object EntityRenderer {

    // entity = particle = point
    val reservedEntityIds = IntArray(16384)

    //    private var entitiesAlive = 0
    private var aliveEntityIndices = mutableSetOf<Int>() // index into reservedEntityIds

    private var shouldUpdateEntities = false
    private var animationsLastTick = 0

    fun playFramesWithBlockDisplays(frames: List<Frame>, maxParticles: Int, plugin: Sheepy) {
        if (frames.isEmpty()) return
        val maxParticlesPerTick = if (maxParticles == 0) reservedEntityIds.size else min(
            reservedEntityIds.size,
            maxParticles
        )
        val particlesAllocatedPerAnimation = maxParticlesPerTick / frames.size

        if (animationsLastTick != frames.size) shouldUpdateEntities = true
        if (shouldUpdateEntities)
            clean(plugin)
//        Utils.sendDebugMessage("aliveEntities: ${aliveEntityIndices.size}, shouldUpdateEntities: $shouldUpdateEntities, animationsLastTick: $animationsLastTick, frames: ${frames.size}")
        plugin.server.onlinePlayers.forEachIndexed { playerIndex, player ->
            val craftPlayer = player as CraftPlayer
            val connection = craftPlayer.handle.connection


            frames.forEachIndexed { frameIndex, frame ->

//                val total = frame.animationParticles.size
                val particleScale = frame.animation.particleScale

                // 1 = (maxP / 3) * 0
                // 2 = (maxP / 3) * 1
                // 3 = (maxP / 3) * 2
                val entitiesStartIndex = (maxParticlesPerTick / frames.size) * frameIndex

                Utils.sendDebugMessage("particlesAllocated: $particlesAllocatedPerAnimation, entityStartIndex: $entitiesStartIndex, frameIndex: $frameIndex, name: ${frame.animation.name}")

//                val divider: Int = if (maxParticles == 0) 0 else total / maxParticles

//        val scaleMultiplier = 1 + Math.clamp(divider / 30.0f, 0.0f, 2.0f)


                frame.animationParticles.forEachIndexed { pointIndex, point ->
                    if (point == null) return@forEachIndexed
//                    if (divider != 0 && pointIndex % divider != 0) return@forEachIndexed

                    if (pointIndex > particlesAllocatedPerAnimation) return@forEachIndexed

                    val entityIndexInReservedArray = entitiesStartIndex + pointIndex

//                Utils.sendMessage(
//                    player,
//                    "Sending packet :) index: $particleIndex entityId: ${AnimationsManager.reservedEntityIds[particleIndex]}"
//                )


                    val block = ColorUtils.getBlockWithColor(point.color)

                    if (!aliveEntityIndices.contains(entityIndexInReservedArray)) {
//                        Utils.sendMessage(player, "Creating block index $entityIndexInReservedArray")
                        connection.sendPacket(
                            ClientboundAddEntityPacket(
                                reservedEntityIds[entityIndexInReservedArray], UUID.randomUUID(),
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
                        aliveEntityIndices.add(entityIndexInReservedArray)
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
                            reservedEntityIds[entityIndexInReservedArray],
                            metasCreated
                        )
                    )
                }
            }

        }
        animationsLastTick = frames.size
        shouldUpdateEntities = false
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
            connection.send(ClientboundRemoveEntitiesPacket(*aliveEntityIndices.map { reservedEntityIds[it] }
                .toIntArray()))
        }
        aliveEntityIndices.clear()
    }

    fun clean(plugin: Sheepy) {
        val numberOfAliveEntities = aliveEntityIndices.size
        if (numberOfAliveEntities > 0) {
            plugin.logger.info("Running EntityRenderer cleanup for $numberOfAliveEntities entities")
            sendRemoveAllEntitiesPacket(plugin)
        }
    }

    val zeroVec = Vec3(0.0, 0.0, 0.0)
}