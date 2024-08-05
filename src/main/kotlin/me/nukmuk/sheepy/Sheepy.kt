package me.nukmuk.sheepy

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable


class Sheepy : JavaPlugin() {

    override fun onEnable() {
        // Plugin startup logic
        getCommand("sheepy")?.setExecutor(SheepyCommand(this));

    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

}
