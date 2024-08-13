package me.nukmuk.sheepy

import org.bukkit.ChatColor

object Config {
    const val FILE_EXTENSION = "shny"
    val VAR_COLOR = ChatColor.GRAY
    val PRIMARY_COLOR = ChatColor.RESET
    val ERROR_COLOR = ChatColor.RED

    object Strings {
        val NO_PERMISSION = "${ERROR_COLOR}You don't have permissions to this command"
    }
}