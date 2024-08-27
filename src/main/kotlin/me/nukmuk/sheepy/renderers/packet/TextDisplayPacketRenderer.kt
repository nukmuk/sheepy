package me.nukmuk.sheepy.renderers.packet

import me.nukmuk.sheepy.AnimationParticle
import me.nukmuk.sheepy.Frame
import me.nukmuk.sheepy.RandomRotation
import me.nukmuk.sheepy.Sheepy
import me.nukmuk.sheepy.TextMode
import me.nukmuk.sheepy.utils.PacketUtil
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.EntityType
import org.joml.AxisAngle4f
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object TextDisplayPacketRenderer : IEntityRenderer {

    override val packetEntityHandler = PacketEntityHandler(this)

    override fun render(
        point: AnimationParticle,
        entityIndexInReservedArray: Int,
        frame: Frame,
        maxParticles: Int,
        plugin: Sheepy
    ) {
        var blockScale = BlockDisplayPacketRenderer.getBlockScale(maxParticles, frame, point) * 8
        val randomRotation = when (frame.animation.randomRotationMode) {
            RandomRotation.FULL -> randomQuaternion()
            RandomRotation.YAW -> Quaternionf(AxisAngle4f(Math.random().toFloat() * Math.PI.toFloat() * 2, 0f, 1f, 0f))
            RandomRotation.NONE -> Quaternionf(1f, 0f, 0f, 0f)
        }
        val onSpawn = mutableListOf<SynchedEntityData.DataValue<*>>(
            SynchedEntityData.DataValue(
                13, // Rotation left
                EntityDataSerializers.QUATERNION,
                randomRotation
            ),
            SynchedEntityData.DataValue(
                8, // Interpolation delay
                EntityDataSerializers.INT,
                0
            ),
            SynchedEntityData.DataValue(
                9, // Transformation interpolation duration
                EntityDataSerializers.INT,
                1
            ),
//            SynchedEntityData.DataValue(
//                15, // Billboard Constraints (0 = FIXED, 1 = VERTICAL, 2 = HORIZONTAL, 3 = CENTER)
//                EntityDataSerializers.BYTE,
//                0
//            ),
        )
        val everyFrame = mutableListOf<SynchedEntityData.DataValue<*>>(
            SynchedEntityData.DataValue(
                11, // Translation
                EntityDataSerializers.VECTOR3,
                Vector3f(
                    (point.x - frame.animation.location.x).toFloat(),
                    (point.y - frame.animation.location.y).toFloat(),
                    (point.z - frame.animation.location.z).toFloat()
                )
            ),
//            SynchedEntityData.DataValue(
//                8, // Interpolation delay
//                EntityDataSerializers.INT,
//                0
//            ),
//            SynchedEntityData.DataValue(
//                9, // Transformation interpolation duration
//                EntityDataSerializers.INT,
//                1
//            ),
        )
        if (frame.animation.textMode == TextMode.TEXT) {
            onSpawn.add(
                SynchedEntityData.DataValue(
                    25, // Background color
                    EntityDataSerializers.INT,
                    point.color.setAlpha(0).asARGB()
                )
            )
            everyFrame.add(
                SynchedEntityData.DataValue(
                    23, // Text
                    EntityDataSerializers.COMPONENT,
                    Component.literal(frame.animation.textForTextRenderer).withColor(point.color.asRGB())
                ),
            )
        } else if (frame.animation.textMode == TextMode.BACKGROUND) {
//            onSpawn.add()
            everyFrame.add(
                SynchedEntityData.DataValue(
                    25, // Background color
                    EntityDataSerializers.INT,
                    point.color.setAlpha(255).asARGB()
                )
            )
            blockScale *= 5
        }

        // add entity scale later so blockScale can be modified in the if statement before
        everyFrame.add(
            SynchedEntityData.DataValue(
                12, // Scale
                EntityDataSerializers.VECTOR3, Vector3f(
                    blockScale,
                    blockScale,
                    blockScale,
                )
            )
        )

        if (!packetEntityHandler.aliveEntityIndices.contains(entityIndexInReservedArray)) {
            // on spawn
            val spawnPacket = ClientboundAddEntityPacket(
                packetEntityHandler.reservedEntityIds[entityIndexInReservedArray], UUID.randomUUID(),
                frame.animation.location.x,
                frame.animation.location.y,
                frame.animation.location.z,
                0.0f,
                0f,
                EntityType.TEXT_DISPLAY,
                0,
                PacketEntityHandler.zeroVec,
                0.0
            )

            val dataPacket = ClientboundSetEntityDataPacket(
                packetEntityHandler.reservedEntityIds[entityIndexInReservedArray],
                onSpawn
            )
            PacketUtil.sendPacketsToAllPlayers(plugin, spawnPacket, dataPacket)


            packetEntityHandler.aliveEntityIndices.add(entityIndexInReservedArray)
        }

        // every frame
        val dataPacket = ClientboundSetEntityDataPacket(
            packetEntityHandler.reservedEntityIds[entityIndexInReservedArray],
            everyFrame
        )
        PacketUtil.sendPacketsToAllPlayers(plugin, dataPacket)
    }

    fun randomQuaternion(): Quaternionf {
        val u1 = Math.random().toFloat()
        val u2 = Math.random().toFloat()
        val u3 = Math.random().toFloat()

        val sqrt1MinusU1 = sqrt(1 - u1)
        val sqrtU1 = sqrt(u1)

        val twoPI = 2 * PI.toFloat()

        return Quaternionf(
            sqrt1MinusU1 * sin(twoPI * u2),
            sqrt1MinusU1 * cos(twoPI * u2),
            sqrtU1 * sin(twoPI * u3),
            sqrtU1 * cos(twoPI * u3)
        )
    }
}
