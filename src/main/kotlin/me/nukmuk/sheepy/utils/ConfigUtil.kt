package me.nukmuk.sheepy.utils

import me.nukmuk.sheepy.Sheepy

object ConfigUtil {
    fun save(plugin: Sheepy) {
        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            plugin.saveConfig()
        })
    }
}