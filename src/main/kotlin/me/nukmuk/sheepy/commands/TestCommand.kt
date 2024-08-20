package me.nukmuk.sheepy.commands

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.subcommand
import me.nukmuk.sheepy.utils.ColorUtil
import me.nukmuk.sheepy.Sheepy
import me.nukmuk.sheepy.utils.Utils

class TestCommand(plugin: Sheepy) {
    val test = subcommand("test") {
        anyExecutor { sender, args ->
            val result = ColorUtil.uniqueBlockColors
            Utils.sendMessage(sender, "$result")
        }
    }
}