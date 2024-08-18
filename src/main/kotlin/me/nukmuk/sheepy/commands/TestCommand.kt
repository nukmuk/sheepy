package me.nukmuk.sheepy.commands

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.subcommand
import me.nukmuk.sheepy.ColorUtils
import me.nukmuk.sheepy.Sheepy
import me.nukmuk.sheepy.Utils
import org.bukkit.Material

class TestCommand(plugin: Sheepy) {
    val test = subcommand("test") {
        anyExecutor { sender, args ->
            val result = ColorUtils.uniqueBlockColors
            Utils.sendMessage(sender, "$result")
        }
    }
}