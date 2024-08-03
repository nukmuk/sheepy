package me.nukmuk.sheepy

import org.bukkit.ChatColor
import org.bukkit.entity.Player

object Utils {
    fun sendMessage(player: Player, message: String) {
        player.sendMessage("${ChatColor.RED}[Sheepy] ${ChatColor.RESET}$message")
    }
}