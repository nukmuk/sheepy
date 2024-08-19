package me.nukmuk.sheepy.renderers

import me.nukmuk.sheepy.ColorUtils
import me.nukmuk.sheepy.Frame
import me.nukmuk.sheepy.RenderType
import me.nukmuk.sheepy.Sheepy
import me.nukmuk.sheepy.Utils
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.Vec3
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.util.CraftChatMessage
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
    private var maxParticlesLastTick: Int? = null

    private val playersWhoPacketsHaveBeenSentTo = mutableListOf<UUID>()

    fun playFramesWithBlockDisplays(frames: List<Frame>, maxParticles: Int, plugin: Sheepy) {
        if (frames.isEmpty()) return
        val maxParticlesPerTick = if (maxParticles == 0) reservedEntityIds.size else min(
            reservedEntityIds.size,
            maxParticles
        )
        val particlesAllocatedPerAnimation = maxParticlesPerTick / frames.size

        if (animationsLastTick != frames.size || maxParticlesLastTick != maxParticlesPerTick)
            shouldUpdateEntities = true

        if (shouldUpdateEntities)
            clean(plugin)
//        Utils.sendDebugMessage("aliveEntities: ${aliveEntityIndices.size}, shouldUpdateEntities: $shouldUpdateEntities, animationsLastTick: $animationsLastTick, frames: ${frames.size}")
        plugin.server.onlinePlayers.forEach { player ->
            val craftPlayer = player as CraftPlayer
            val connection = craftPlayer.handle.connection


            frames.forEachIndexed { frameIndex, frame ->

                val frameRenderType = frame.animation.renderType

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


                    var block = Blocks.REDSTONE_TORCH

                    if (frameRenderType == RenderType.BLOCK_DISPLAY)
                        block = ColorUtils.getBlockWithColor(point.color)

                    if (!aliveEntityIndices.contains(entityIndexInReservedArray)) {
//                        Utils.sendMessage(player, "Creating block index $entityIndexInReservedArray")

                        val entityType = when (frameRenderType) {
                            RenderType.TEXT_DISPLAY -> EntityType.TEXT_DISPLAY
                            RenderType.BLOCK_DISPLAY -> EntityType.BLOCK_DISPLAY
                            else -> throw Exception("Non entity type in EntityRenderer")
                        }

                        connection.sendPacket(
                            ClientboundAddEntityPacket(
                                reservedEntityIds[entityIndexInReservedArray], UUID.randomUUID(),
                                frame.animation.location.x,
                                frame.animation.location.y,
                                frame.animation.location.z,
                                0.0f,
                                0.0f,
                                entityType,
                                0,
                                zeroVec,
                                0.0
                            )
                        )
                        aliveEntityIndices.add(entityIndexInReservedArray)
                        playersWhoPacketsHaveBeenSentTo.add(player.uniqueId)
                    }

                    val entityInfo = when (frameRenderType) {
                        RenderType.BLOCK_DISPLAY -> SynchedEntityData.DataValue(
                            23,
                            EntityDataSerializers.BLOCK_STATE,
                            block.defaultBlockState()
                        )

                        RenderType.TEXT_DISPLAY -> SynchedEntityData.DataValue(
                            23,
                            EntityDataSerializers.COMPONENT,
                            CraftChatMessage.fromString("hello")[0]
                        )

                        else -> throw Exception("Non entity type in EntityRenderer")
                    }

                    val metasCreated = listOf(
                        entityInfo,
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
        maxParticlesLastTick = maxParticlesPerTick
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