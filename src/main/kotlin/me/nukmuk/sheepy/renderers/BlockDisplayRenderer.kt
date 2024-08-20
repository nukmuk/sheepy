package me.nukmuk.sheepy.renderers

import me.nukmuk.sheepy.AnimationParticle
import me.nukmuk.sheepy.utils.ColorUtil
import me.nukmuk.sheepy.Frame
import me.nukmuk.sheepy.RenderType
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.block.Blocks
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.joml.Vector3f
import java.util.*

object BlockDisplayRenderer : IEntityRenderer {

    override val entityHandler = EntityHandler(this)

    override fun render(
        frameRenderType: RenderType,
        point: AnimationParticle,
        entityIndexInReservedArray: Int,
        connection: ServerGamePacketListenerImpl,
        frame: Frame,
        player: CraftPlayer,
    ) {
        var block = Blocks.REDSTONE_TORCH

        if (frameRenderType == RenderType.BLOCK_DISPLAY)
            block = ColorUtil.getBlockWithColor(point.color)

        if (!entityHandler.aliveEntityIndices.contains(entityIndexInReservedArray)) {
            //                        Utils.sendMessage(player, "Creating block index $entityIndexInReservedArray")

            val entityType = when (frameRenderType) {
                RenderType.TEXT_DISPLAY -> EntityType.TEXT_DISPLAY
                RenderType.BLOCK_DISPLAY -> EntityType.BLOCK_DISPLAY
                else -> throw Exception("Non entity type in EntityRenderer")
            }

            connection.send(
                ClientboundAddEntityPacket(
                    entityHandler.reservedEntityIds[entityIndexInReservedArray], UUID.randomUUID(),
                    frame.animation.location.x,
                    frame.animation.location.y,
                    frame.animation.location.z,
                    0.0f,
                    0.0f,
                    entityType,
                    0,
                    EntityHandler.zeroVec,
                    0.0
                )
            )

            if (frameRenderType == RenderType.TEXT_DISPLAY) {
                connection.send(
                    ClientboundSetEntityDataPacket(
                        entityHandler.reservedEntityIds[entityIndexInReservedArray],
                        listOf(
                            SynchedEntityData.DataValue(
                                8,
                                EntityDataSerializers.INT,
                                10
                            ),
                            SynchedEntityData.DataValue(
                                9,
                                EntityDataSerializers.INT,
                                1
                            ),
                            SynchedEntityData.DataValue(
                                15,
                                EntityDataSerializers.BYTE,
                                0
                            ),
                            SynchedEntityData.DataValue(
                                23,
                                EntityDataSerializers.COMPONENT,
                                Component.literal(frame.animation.textForTextRenderer)
                                //                                                .withColor(point.color.asRGB())
                            ),
                        )
                    )
                )
            }

            entityHandler.aliveEntityIndices.add(entityIndexInReservedArray)
            entityHandler.playersWhoPacketsHaveBeenSentTo.add(player.uniqueId)
        }

        val entityInfo = when (frameRenderType) {
            RenderType.BLOCK_DISPLAY -> SynchedEntityData.DataValue(
                23,
                EntityDataSerializers.BLOCK_STATE,
                block.defaultBlockState()
            )

            RenderType.TEXT_DISPLAY -> SynchedEntityData.DataValue(
                25,
                EntityDataSerializers.INT,
                point.color.setAlpha(255).asARGB()
            )

            else -> null
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
            ),

            )

        connection.send(
            ClientboundSetEntityDataPacket(
                entityHandler.reservedEntityIds[entityIndexInReservedArray],
                metasCreated
            )
        )
    }

}