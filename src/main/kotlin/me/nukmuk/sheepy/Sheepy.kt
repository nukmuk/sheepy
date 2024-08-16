package me.nukmuk.sheepy

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import me.nukmuk.sheepy.commands.Sheepy2Command
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
        AnimationsPlayer.initialize(this)
        Sheepy2Command(this).register()
        Utils.getAnimsInFolder(this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
        AnimationsPlayer.clearAnimations()
        CommandAPI.onDisable()
    }
}
