package me.nukmuk.sheepy.commands

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.subcommand
import me.nukmuk.sheepy.AnimationsManager
import me.nukmuk.sheepy.Sheepy
import me.nukmuk.sheepy.renderers.packet.PacketEntityHandler
import me.nukmuk.sheepy.utils.Utils

class TestCommand(plugin: Sheepy) {
    val test = subcommand("test") {
        withPermission("sheepy.test")
        anyExecutor { sender, args ->
//            val result = ColorUtil.uniqueBlockColors
            val result = PacketEntityHandler.cleanEntityRenderers(plugin)
            Utils.sendMessage(sender, "$result")
            AnimationsManager.animations.forEach {
                it.value.renderType = null
            }
        }
    }
}