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
        // Plugin startup logic
        CommandAPI.onEnable()
//        getCommand("sheepy")?.setExecutor(LegacySheepyCommand(this))
//        getCommand("misc")?.setExecutor(MiscCommand(this))
        AnimationsManager.initialize(this)
        SheepyCommand(this).register()
        AnimationsManager.getAnimsInFolder(this)
        PacketEntityHandler.initializeAllEntityRenderers(this)
        server.pluginManager.registerEvents(PlayerListener(this), this)
        TextDisplayRenderer.initializeTextDisplaysEntityHandler(this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
        AnimationsManager.clearAnimations()
        CommandAPI.onDisable()
    }
}
