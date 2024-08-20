package me.nukmuk.sheepy.renderers

import me.nukmuk.sheepy.AnimationParticle
import me.nukmuk.sheepy.Frame
import me.nukmuk.sheepy.Sheepy
import net.minecraft.server.network.ServerGamePacketListenerImpl
import org.bukkit.craftbukkit.entity.CraftPlayer

interface IEntityRenderer {
    fun render(
        point: AnimationParticle,
        entityIndexInReservedArray: Int,
        connection: ServerGamePacketListenerImpl,
        frame: Frame,
        player: CraftPlayer,
        maxParticles: Int
    )
    val entityHandler: EntityHandler
    fun playFrames(frames: List<Frame>, maxParticles: Int, plugin: Sheepy) {
        entityHandler.playFrames(frames, maxParticles, plugin)
    }
}