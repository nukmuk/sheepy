package me.nukmuk.sheepy.commands

import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.booleanArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.floatArgument
import dev.jorel.commandapi.kotlindsl.integerArgument
import dev.jorel.commandapi.kotlindsl.itemStackArgument
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.stringArgument
import dev.jorel.commandapi.kotlindsl.subcommand
import me.nukmuk.sheepy.AnimationsPlayer
import me.nukmuk.sheepy.Config
import me.nukmuk.sheepy.Sheepy
import me.nukmuk.sheepy.Utils
import org.bukkit.ChatColor
import org.bukkit.inventory.ItemStack
import kotlin.Boolean

class Sheepy2Command(plugin: Sheepy) {

    private val files = subcommand("files") {
        anyExecutor { sender, args ->
            val files = Utils.getAnimsInFolder(plugin)
            Utils.sendMessage(
                sender,
                if (!files.isEmpty()) files.joinToString("${ChatColor.GRAY}, ${ChatColor.RESET}") { file -> file.name } else "Folder empty")
        }
    }

    private val stream = subcommand("stream") {

        stringArgument("fileName") {
            replaceSuggestions(
                ArgumentSuggestions.strings { Utils.animsInFolder.map { it.nameWithoutExtension }.toTypedArray() }
            )
        }

        withAliases("st")
        stringArgument("animationName", optional = true)
        booleanArgument("repeat", optional = true)
        playerExecutor { player, args ->

            val fileName = args["fileName"] as String
            var animationName = (args["animationName"] ?: fileName) as String
            val repeat = (args["repeat"] ?: false) as Boolean

            if (AnimationsPlayer.animationNames().contains(animationName)) {
                Utils.sendMessage(
                    player,
                    "Animation with name ${Config.VAR_COLOR}${animationName} ${Config.PRIMARY_COLOR}already exists"
                )
                return@playerExecutor
            }

            val files = Utils.getAnimsInFolder(plugin)

            val file = files.find { it.nameWithoutExtension == fileName }

            if (file == null) {
                Utils.sendMessage(player, "File ${Config.VAR_COLOR}$fileName ${Config.PRIMARY_COLOR}not found")
                return@playerExecutor
            }

            var msg = "Created animationm with file ${Config.VAR_COLOR}${file.name}"
            if (repeat) msg += "${Config.PRIMARY_COLOR}, repeat ${Config.VAR_COLOR}on"

            Utils.sendMessage(player, msg)

            val animation = AnimationsPlayer.createAnimation(
                animationName,
                file,
                player.getTargetBlock(null, 10).location.add(0.0, 1.0, 0.0)
            )

            animation.repeat = repeat

            animation.start()
        }
    }

    private val remove = subcommand("remove") {
        withAliases("rm")
        stringArgument("animationName")
        anyExecutor { sender, args ->
            val animationName = args["animationName"] as String

            val removedAnimation = AnimationsPlayer.getAnimation(animationName)
            removedAnimation?.remove()
            if (removedAnimation == null) {
                Utils.sendMessage(sender, "No animation ${Config.VAR_COLOR}${animationName}")
                return@anyExecutor
            }
            Utils.sendMessage(sender, "Removed and stopped ${Config.VAR_COLOR}${removedAnimation.name}")
        }
    }

    private val clear = subcommand("clear") {
        anyExecutor { sender, args ->
            Utils.sendMessage(
                sender,
                "Removing ${Config.VAR_COLOR}${AnimationsPlayer.animationNames().size} ${Config.PRIMARY_COLOR}animations"
            )
            AnimationsPlayer.clearAnimations()
        }
    }

    private val pause = subcommand("pause") {
        withAliases("stop")
        stringArgument(
            "animationName",
        )
        anyExecutor { sender, args ->
            val animationName = args["animationName"] as String

            val animation = AnimationsPlayer.getAnimation(animationName)
            if (animation == null) {
                Utils.sendMessage(sender, "No animation ${Config.VAR_COLOR}$animationName")
                return@anyExecutor
            }

            animation.stop()
            Utils.sendMessage(sender, "Paused ${Config.VAR_COLOR}${animation.name}")

        }
    }

    private val list = subcommand("list") {
        withAliases("ls")
        anyExecutor { sender, args ->
            Utils.sendMessage(sender, "Animations: ${Config.VAR_COLOR}${AnimationsPlayer.animationNames()}")
        }
    }

    private val globalMaxParticlesPerTick = subcommand("globalmax") {
        withAliases("gmax")
        integerArgument("amount", optional = true)
        anyExecutor { sender, args ->
            val amount = args["amount"] as? Int
            if (amount == null) {
                Utils.sendMessage(
                    sender,
                    "Current max particles per tick: ${Config.VAR_COLOR}${AnimationsPlayer.maxParticlesPerTick}"
                )
                return@anyExecutor
            }
            AnimationsPlayer.maxParticlesPerTick = amount.toInt()
            Utils.sendMessage(
                sender,
                "Set max particles per tick to ${Config.VAR_COLOR}${AnimationsPlayer.maxParticlesPerTick}"
            )
        }
    }

    private val particleScale = subcommand("particlescale") {
        withAliases("pscale")
        stringArgument("animationName") {
            replaceSuggestions(
                ArgumentSuggestions.strings { AnimationsPlayer.animationNames().toTypedArray() }
            )
        }
        floatArgument("particleScale", optional = true) {
            replaceSuggestions(ArgumentSuggestions.strings({ info ->
                val animationName = info.previousArgs["animationName"] as String
                val animation = AnimationsPlayer.getAnimation(animationName)
                if (animation == null) return@strings arrayListOf<String>().toTypedArray()
                return@strings arrayListOf<String>(animation.particleScale.toString()).toTypedArray()
            }))
        }
        anyExecutor { sender, args ->
            val animationName = args["animationName"] as String
            val particleScale = args["particleScale"] as? Float
            val animation = AnimationsPlayer.getAnimation(animationName)

            if (animation == null) {
                Utils.sendMessage(sender, "No animation ${Config.VAR_COLOR}${animationName}")
                return@anyExecutor
            }

            if (particleScale == null) {
                Utils.sendMessage(
                    sender,
                    "Current particle scale: ${Config.VAR_COLOR}${animation.particleScale}"
                )
                return@anyExecutor
            }
            animation.particleScale = particleScale
            Utils.sendMessage(
                sender,
                "Set particle scale to ${Config.VAR_COLOR}${animation.particleScale} ${Config.PRIMARY_COLOR}for ${Config.VAR_COLOR}${animation.name}"
            )
        }
    }

    fun register() {
        commandAPICommand("sheepy2") {
            withAliases("sh2", "s2")
            subcommand(files)
            subcommand(stream)
            subcommand(remove)
            subcommand(clear)
            subcommand(pause)
            subcommand(list)
            subcommand(globalMaxParticlesPerTick)
            subcommand(particleScale)

            literalArgument("give")
            itemStackArgument("item")
            integerArgument("amount", optional = true)
            playerExecutor { player, args ->
                val itemStack: ItemStack = args["item"] as ItemStack
                val amount: Int = args.getOptional("amount").orElse(1) as Int
                itemStack.amount = amount
                player.inventory.addItem(itemStack)
            }
        }
    }
}