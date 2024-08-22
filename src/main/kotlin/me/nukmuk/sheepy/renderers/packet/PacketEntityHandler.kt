package me.nukmuk.sheepy.renderers.packet

import me.nukmuk.sheepy.*
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.world.entity.EntityType
import net.minecraft.world.phys.Vec3
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.entity.CraftPlayer
import java.util.*
import kotlin.math.min

class PacketEntityHandler(private val renderer: IEntityRenderer) {

    // entity = particle = point
    val reservedEntityIds = IntArray(16384)

    var aliveEntityIndices = mutableSetOf<Int>() // index into reservedEntityIds

    private var shouldUpdateEntities = false
    private var animationsLastTick = 0
    private var maxParticlesLastTick: Int? = null

    val playersWhoPacketsHaveBeenSentTo = mutableListOf<UUID>()

    fun playFrames(frames: List<Frame>, maxParticles: Int, plugin: Sheepy) {
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

                val entitiesStartIndex = (maxParticlesPerTick / frames.size) * frameIndex

//                Utils.sendDebugMessage("particlesAllocated: $particlesAllocatedPerAnimation, entityStartIndex: $entitiesStartIndex, frameIndex: $frameIndex, name: ${frame.animation.name}")



                frame.animationParticles.forEachIndexed pointLoop@{ pointIndex, point ->
                    if (point == null) return@pointLoop

                    if (pointIndex > particlesAllocatedPerAnimation) return@pointLoop

                    val entityIndexInReservedArray = entitiesStartIndex + pointIndex

                    renderer.render(
                        point,
                        entityIndexInReservedArray,
                        connection,
                        frame,
                        player,
                        maxParticles
                    )
                }
            }

        }
        animationsLastTick = frames.size
        maxParticlesLastTick = maxParticlesPerTick
        shouldUpdateEntities = false
    }


    private fun initializeEntityIds(plugin: Sheepy) {
        val entityType = EntityType.PIG
        val level = (plugin.server.worlds[0] as CraftWorld).handle
        repeat(reservedEntityIds.size) { index ->
            val entity = entityType.create(level)
            reservedEntityIds[index] = entity?.id ?: -1
        }
        
    }

    private fun sendRemoveAllEntitiesPacket(plugin: Sheepy) {
        for (player in plugin.server.onlinePlayers) {
            val craftPlayer = player as CraftPlayer
            val connection = craftPlayer.handle.connection
            connection.send(ClientboundRemoveEntitiesPacket(*aliveEntityIndices.map { reservedEntityIds[it] }
                .toIntArray()))
        }
        aliveEntityIndices.clear()
    }

    private fun clean(plugin: Sheepy) {
        val numberOfAliveEntities = aliveEntityIndices.size
        if (numberOfAliveEntities > 0) {
            plugin.logger.info("Running EntityRenderer cleanup for $numberOfAliveEntities entities")
            sendRemoveAllEntitiesPacket(plugin)
        }
    }

    companion object {
        val zeroVec = Vec3(0.0, 0.0, 0.0)

        // must be manually updated 🙄
        private val entityRenderers = arrayListOf(BlockDisplayPacketRenderer, TextDisplayPacketRenderer)

        fun cleanEntityRenderers(plugin: Sheepy) {
            entityRenderers.forEach {
                it.packetEntityHandler.clean(plugin)
            }
        }

        fun initializeAllEntityRenderers(plugin: Sheepy) {
            entityRenderers.forEach {
                it.packetEntityHandler.initializeEntityIds(plugin)
            }
        }
    }
}