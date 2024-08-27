package me.nukmuk.sheepy.renderers.packet

import me.nukmuk.sheepy.AnimationParticle
import me.nukmuk.sheepy.Frame
import me.nukmuk.sheepy.Sheepy
import me.nukmuk.sheepy.utils.BlockColorUtil
import me.nukmuk.sheepy.utils.PacketUtil
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.EntityType
import org.joml.Vector3f
import java.util.*

object BlockDisplayPacketRenderer : IEntityRenderer {

    override val packetEntityHandler = PacketEntityHandler(this)

    override fun render(
        point: AnimationParticle,
        entityIndexInReservedArray: Int,
        frame: Frame,
        maxParticles: Int,
        plugin: Sheepy
    ) {
        val block = BlockColorUtil.getBlockWithColor(point.color)

        if (!packetEntityHandler.aliveEntityIndices.contains(entityIndexInReservedArray)) {
            // on spawn
            val spawnPacket = ClientboundAddEntityPacket(
                packetEntityHandler.reservedEntityIds[entityIndexInReservedArray],
                UUID.randomUUID(),
                frame.animation.location.x,
                frame.animation.location.y,
                frame.animation.location.z,
                0.0f,
                0.0f,
                EntityType.BLOCK_DISPLAY,
                0,
                PacketEntityHandler.zeroVec,
                0.0
            )
            val entityDataPacket = ClientboundSetEntityDataPacket(
                packetEntityHandler.reservedEntityIds[entityIndexInReservedArray], listOf(
                    SynchedEntityData.DataValue(
                        17, // View range
                        EntityDataSerializers.FLOAT, 100f
                    ),
                )
            )
            PacketUtil.sendPacketsToAllPlayers(plugin, spawnPacket, entityDataPacket)

            packetEntityHandler.aliveEntityIndices.add(entityIndexInReservedArray)


        }

        val blockScale = getBlockScale(maxParticles, frame, point)

        val metas = listOf(
            SynchedEntityData.DataValue(
                23, // Displayed block state
                EntityDataSerializers.BLOCK_STATE, block.defaultBlockState()
            ),
            SynchedEntityData.DataValue(
                11, // Translation
                EntityDataSerializers.VECTOR3, Vector3f(
                    (point.x - frame.animation.location.x).toFloat(),
                    (point.y - frame.animation.location.y).toFloat(),
                    (point.z - frame.animation.location.z).toFloat()
                )
            ),
            SynchedEntityData.DataValue(
                12, // Scale
                EntityDataSerializers.VECTOR3, Vector3f(
                    blockScale,
                    blockScale,
                    blockScale,
                )
            )
        )

        val entityDataPacket = ClientboundSetEntityDataPacket(
            packetEntityHandler.reservedEntityIds[entityIndexInReservedArray], metas
        )
        PacketUtil.sendPacketsToAllPlayers(plugin, entityDataPacket)
    }

    fun getBlockScale(
        maxParticles: Int,
        frame: Frame,
        point: AnimationParticle
    ): Float {
        val divider: Int = if (maxParticles == 0) 0 else frame.animationParticles.size / maxParticles

        val scaleMultiplier = 1 + Math.clamp(divider / 30.0f, 0.0f, 2.0f)
        val makeBlocksApproxParticleSizedConstant = 0.005f
        val blockScale =
            frame.animation.particleScale * point.scale.toFloat() * makeBlocksApproxParticleSizedConstant * scaleMultiplier
        return blockScale
    }
}