package me.nukmuk.sheepy

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import me.nukmuk.sheepy.commands.SheepyCommand
import me.nukmuk.sheepy.renderers.packet.PacketEntityHandler
import me.nukmuk.sheepy.listeners.PlayerListener
import me.nukmuk.sheepy.renderers.TextDisplayRenderer
import org.bukkit.plugin.java.JavaPlugin


class Sheepy : JavaPlugin() {

    override fun onLoad() {
        CommandAPI.onLoad(CommandAPIBukkitConfig(this).usePluginNamespace())
    }

    override fun onEnable() {
        instance = this
        CommandAPI.onEnable()
        saveResource("config.yml", false)
//        getCommand("misc")?.setExecutor(MiscCommand(this))
        AnimationsManager.initialize(this)
        SheepyCommand(this).register()
        AnimationsManager.getAnimsInFolder(this)
        PacketEntityHandler.initializeAllEntityRenderers(this)
        server.pluginManager.registerEvents(PlayerListener(this), this)
        TextDisplayRenderer.initializeTextDisplaysEntityHandler(this)
    }

    override fun onDisable() {
        AnimationsManager.clearAnimations()
        CommandAPI.onDisable()
    }

    companion object {
        // already using dependency injection, so maybe bad idea ?
        lateinit var instance: Sheepy
    }
}

fun l(string: String) {
    Sheepy.instance.logger.info(string)
}