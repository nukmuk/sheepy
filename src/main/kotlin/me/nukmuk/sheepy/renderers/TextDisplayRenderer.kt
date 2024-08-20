package me.nukmuk.sheepy.renderers

import me.nukmuk.sheepy.AnimationParticle
import me.nukmuk.sheepy.Frame
import me.nukmuk.sheepy.RenderType
import me.nukmuk.sheepy.utils.ColorUtil
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.entity.EntityType
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.joml.Vector3f
import java.util.*

object TextDisplayRenderer : IEntityRenderer {

    override val entityHandler = EntityHandler(this)

    override fun render(
        point: AnimationParticle,
        entityIndexInReservedArray: Int,
        connection: ServerGamePacketListenerImpl,
        frame: Frame,
        player: CraftPlayer,
        maxParticles: Int
    ) {
        if (!entityHandler.aliveEntityIndices.contains(entityIndexInReservedArray)) {
            // on spawn
            connection.send(
                ClientboundAddEntityPacket(
                    entityHandler.reservedEntityIds[entityIndexInReservedArray], UUID.randomUUID(),
                    frame.animation.location.x,
                    frame.animation.location.y,
                    frame.animation.location.z,
                    0.0f,
                    0.0f,
                    EntityType.TEXT_DISPLAY,
                    0,
                    EntityHandler.zeroVec,
                    0.0
                )
            )

            connection.send(
                ClientboundSetEntityDataPacket(
                    entityHandler.reservedEntityIds[entityIndexInReservedArray],
                    listOf(
                        SynchedEntityData.DataValue(
                            8, // Interpolation delay
                            EntityDataSerializers.INT,
                            10
                        ),
                        SynchedEntityData.DataValue(
                            9, // Transformation interpolation duration
                            EntityDataSerializers.INT,
                            1
                        ),
                        SynchedEntityData.DataValue(
                            15, // Billboard Constraints (0 = FIXED, 1 = VERTICAL, 2 = HORIZONTAL, 3 = CENTER)
                            EntityDataSerializers.BYTE,
                            0
                        ),
                        SynchedEntityData.DataValue(
                            23, // Text
                            EntityDataSerializers.COMPONENT,
                            Component.literal(frame.animation.textForTextRenderer)
                            //                                                .withColor(point.color.asRGB())
                        ),
                    )
                )
            )

            entityHandler.aliveEntityIndices.add(entityIndexInReservedArray)
            entityHandler.playersWhoPacketsHaveBeenSentTo.add(player.uniqueId)
        }


        // every frame
        connection.send(
            ClientboundSetEntityDataPacket(
                entityHandler.reservedEntityIds[entityIndexInReservedArray],
                listOf(
                    SynchedEntityData.DataValue(
                        25, // Background color
                        EntityDataSerializers.INT,
                        point.color.setAlpha(255).asARGB()
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

                    )
            )
        )
    }

}