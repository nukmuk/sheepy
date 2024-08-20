package me.nukmuk.sheepy.renderers

import me.nukmuk.sheepy.AnimationParticle
import me.nukmuk.sheepy.Frame
import me.nukmuk.sheepy.RenderType
import net.minecraft.server.network.ServerGamePacketListenerImpl
import org.bukkit.craftbukkit.entity.CraftPlayer

interface IEntityRenderer {
    fun render(
        frameRenderType: RenderType,
        point: AnimationParticle,
        entityIndexInReservedArray: Int,
        connection: ServerGamePacketListenerImpl,
        frame: Frame,
        player: CraftPlayer,
    )

    val entityHandler: EntityHandler
}