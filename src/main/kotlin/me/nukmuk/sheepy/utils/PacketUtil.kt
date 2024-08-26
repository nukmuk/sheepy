package me.nukmuk.sheepy.utils

import me.nukmuk.sheepy.Sheepy
import org.bukkit.craftbukkit.entity.CraftPlayer

object PacketUtil {
    fun sendPacketsToAllPlayers(plugin: Sheepy, vararg packets: net.minecraft.network.protocol.Packet<*>) {
        for (player in plugin.server.onlinePlayers) {
            val connection = (player as CraftPlayer).handle.connection
            for (packet in packets) {
                connection.send(packet)
            }
        }
    }
}