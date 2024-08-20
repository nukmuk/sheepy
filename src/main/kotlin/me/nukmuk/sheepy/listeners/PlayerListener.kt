package me.nukmuk.sheepy.listeners

import me.nukmuk.sheepy.Sheepy
import me.nukmuk.sheepy.renderers.EntityHandler
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent

class PlayerListener(private val plugin: Sheepy) : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        plugin.logger.info("Running EntityRenderer cleanup on player join")
        EntityHandler.cleanEntityRenderers(plugin)
    }

    @EventHandler
    fun onPlayerChangedWorld(event: PlayerChangedWorldEvent) {
        plugin.logger.info("Running EntityRenderer cleanup on player world changed")
        EntityHandler.cleanEntityRenderers(plugin)
    }
}