package me.nukmuk.sheepy.renderers.packet

import me.nukmuk.sheepy.AnimationParticle
import me.nukmuk.sheepy.Frame
import me.nukmuk.sheepy.utils.ColorUtil
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.entity.EntityType
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.joml.Vector3f
import java.util.*

object BlockDisplayPacketRenderer : IEntityRenderer {

    override val packetEntityHandler = PacketEntityHandler(this)

    override fun render(
        point: AnimationParticle,
        entityIndexInReservedArray: Int,
        connection: ServerGamePacketListenerImpl,
        frame: Frame,
        player: CraftPlayer,
        maxParticles: Int
    ) {
        val block = ColorUtil.getBlockWithColor(point.color)

        if (!packetEntityHandler.aliveEntityIndices.contains(entityIndexInReservedArray)) {
            // on spawn
            connection.send(
                ClientboundAddEntityPacket(
                    packetEntityHandler.reservedEntityIds[entityIndexInReservedArray], UUID.randomUUID(),
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
            )

            packetEntityHandler.aliveEntityIndices.add(entityIndexInReservedArray)
            packetEntityHandler.playersWhoPacketsHaveBeenSentTo.add(player.uniqueId)

            connection.send(
                ClientboundSetEntityDataPacket(
                    packetEntityHandler.reservedEntityIds[entityIndexInReservedArray],
                    listOf(
                        SynchedEntityData.DataValue(
                            17, // View range
                            EntityDataSerializers.FLOAT,
                            100f
                        ),
                    )

                )
            )
        }

        val divider: Int = if (maxParticles == 0) 0 else frame.animationParticles.size / maxParticles

        val scaleMultiplier = 1 + Math.clamp(divider / 30.0f, 0.0f, 2.0f)
        val makeBlocksApproxParticleSizedConstant = 0.005f
        val blockScale =
            frame.animation.particleScale * point.scale.toFloat() * makeBlocksApproxParticleSizedConstant * scaleMultiplier

        val metas = listOf(
            SynchedEntityData.DataValue(
                23, // Displayed block state
                EntityDataSerializers.BLOCK_STATE,
                block.defaultBlockState()
            ),
            SynchedEntityData.DataValue(
                11, // Translation
                EntityDataSerializers.VECTOR3,
                Vector3f(
                    (point.x - frame.animation.location.x).toFloat(),
                    (point.y - frame.animation.location.y).toFloat(),
                    (point.z - frame.animation.location.z).toFloat()
                )
            ),
            SynchedEntityData.DataValue(
                12, // Scale
                EntityDataSerializers.VECTOR3,
                Vector3f(
                    blockScale,
                    blockScale,
                    blockScale,
                )
            )
        )

        connection.send(
            ClientboundSetEntityDataPacket(
                packetEntityHandler.reservedEntityIds[entityIndexInReservedArray],
                metas
            )
        )
    }

}