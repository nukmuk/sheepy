package me.nukmuk.sheepy

import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import org.bukkit.Particle
import org.bukkit.craftbukkit.entity.CraftPlayer
import java.util.UUID

enum class RenderType {
    PARTICLE,
    BLOCK_DISPLAY
}

object FrameRenderer {
    fun playFrameWithParticles(frame: Frame, maxParticles: Int) {
        val total = frame.animationParticles.size
        val particleScale = frame.animation.particleScale

        val divider: Int = if (maxParticles == 0) 0 else total / maxParticles

        val scaleMultiplier = 1 + Math.clamp(divider / 30.0f, 0.0f, 2.0f)

        frame.animationParticles.forEachIndexed { idx, p ->
            if (p == null) return
            if (divider != 0 && idx % divider != 0) return@forEachIndexed
            frame.animation.world.spawnParticle(
                Particle.DUST,
                p.x.toDouble(),
                p.y.toDouble(),
                p.z.toDouble(),
                1,
                0.0,
                0.0,
                0.0,
                0.0,
                Particle.DustOptions(p.color, p.scale.toFloat() / 255 * particleScale * scaleMultiplier * 5),
                true
            )
        }
    }

    fun playFrameWithBlockDisplays(frame: Frame, maxParticles: Int, plugin: Sheepy) {
//        val total = frame.animationParticles.size
        val particleScale = frame.animation.particleScale

//        val divider: Int = if (maxParticles == 0) 0 else total / maxParticles

//        val scaleMultiplier = 1 + Math.clamp(divider / 30.0f, 0.0f, 2.0f)


        plugin.server.onlinePlayers.forEachIndexed { playerIndex, player ->
            val craftPlayer = player as CraftPlayer
            val connection = craftPlayer.handle.connection
            frame.animationParticles.forEachIndexed { particleIndex, point ->
                if (point == null) return
//            if (divider != 0 && idx % divider != 0) return@forEachIndexed

                if (particleIndex > maxParticles) return@forEachIndexed
//                if (particleIndex > 1) return@forEachIndexed

//                Utils.sendMessage(
//                    player,
//                    "Sending packet :) index: $particleIndex entityId: ${AnimationsManager.reservedEntityIds[particleIndex]}"
//                )


                connection.sendPacket(
                    ClientboundAddEntityPacket(
                        AnimationsManager.reservedEntityIds[particleIndex], UUID.randomUUID(),
                        point.x.toDouble(),
                        point.y.toDouble(),
                        point.z.toDouble(),
                        0.0f,
                        0.0f,
                        EntityType.BLOCK_DISPLAY,
                        0,
                        zeroVec,
                        0.0
                    )
                )


                val metasCreated = listOf(
                    SynchedEntityData.DataValue(
                        23,
                        EntityDataSerializers.BLOCK_STATE,
                        Blocks.DIAMOND_BLOCK.defaultBlockState()
                    )
                )


                connection.send(
                    ClientboundSetEntityDataPacket(
                        AnimationsManager.reservedEntityIds[particleIndex],
                        metasCreated
                    )
                )

            }
        }
    }

    val zeroVec = Vec3(0.0, 0.0, 0.0)
    val zeroUUID = UUID(0, 0)
}