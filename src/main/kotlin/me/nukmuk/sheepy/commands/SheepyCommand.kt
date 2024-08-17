package me.nukmuk.sheepy.commands

import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.booleanArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.floatArgument
import dev.jorel.commandapi.kotlindsl.integerArgument
import dev.jorel.commandapi.kotlindsl.multiLiteralArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.stringArgument
import dev.jorel.commandapi.kotlindsl.subcommand
import me.nukmuk.sheepy.AnimationsManager
import me.nukmuk.sheepy.Config
import me.nukmuk.sheepy.RenderType
import me.nukmuk.sheepy.Sheepy
import me.nukmuk.sheepy.Utils
import org.bukkit.ChatColor
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.command.CommandSender
import kotlin.Boolean
import kotlin.math.roundToInt

class SheepyCommand(plugin: Sheepy) {

    fun register() {
        commandAPICommand("sheepy") {
            withAliases("sh")
            withPermission("sheepy.use")
            subcommand(files)
            subcommand(create)
            subcommand(remove)
            subcommand(clear)
            subcommand(pause)
            subcommand(resume)
            subcommand(list)
            subcommand(globalMaxParticlesPerTick)
            subcommand(particleScale)
            subcommand(animationScale)
            subcommand(scale)
            subcommand(rotation)
            subcommand(debug)
            subcommand(renderType)
        }
    }

    private val files = subcommand("files") {
        anyExecutor { sender, args ->
            val files = Utils.getAnimsInFolder(plugin)
            Utils.sendMessage(
                sender,
                if (!files.isEmpty()) files.joinToString("<gray>, ${Config.PRIMARY_COLOR}") { file -> file.name } else "Folder empty")
        }
    }

    private val create = subcommand("create") {
        withAliases("c")
        withAliases("st")

        stringArgument("fileName") {
            replaceSuggestions(
                ArgumentSuggestions.strings { Utils.animsInFolder.map { it.nameWithoutExtension }.toTypedArray() }
            )
        }

        stringArgument("animationName", optional = true) {
            replaceSuggestions(ArgumentSuggestions.strings { info -> arrayOf(info.previousArgs()["fileName"].toString()) })
        }
        booleanArgument("repeat", optional = true)
        floatArgument("scale", optional = true)
        playerExecutor { player, args ->

            val fileName = args["fileName"] as String
            var animationName = (args["animationName"] ?: fileName) as String
            val repeat = (args["repeat"] ?: false) as Boolean
            val scale = (args["scale"]) as? Float

            if (AnimationsManager.animationNames().contains(animationName)) {
                Utils.sendMessage(
                    player,
                    "Animation with name ${Config.VAR_COLOR}${animationName} ${Config.PRIMARY_COLOR}already exists"
                )
                return@playerExecutor
            }

            val range = 64.0
            val targetPosition = player.rayTraceBlocks(64.0, FluidCollisionMode.ALWAYS)?.hitPosition
            if (targetPosition == null) {
                Utils.sendMessage(player, "Please look at a block (range $range blocks)")
                return@playerExecutor
            }
            val targetLocation = Location(player.world, targetPosition.x, targetPosition.y, targetPosition.z)

            val files = Utils.getAnimsInFolder(plugin)

            val file = files.find { it.nameWithoutExtension == fileName }

            if (file == null) {
                Utils.sendMessage(player, "File ${Config.VAR_COLOR}$fileName ${Config.PRIMARY_COLOR}not found")
                return@playerExecutor
            }

            var msg = "Created animationm with file ${Config.VAR_COLOR}${file.name}"
            if (repeat) msg += "${Config.PRIMARY_COLOR}, repeat ${Config.VAR_COLOR}on"


            val animation = AnimationsManager.createAnimation(
                animationName,
                file,
                targetLocation,
            )

            animation.repeat = repeat

            if (scale != null) {
                animation.animationScale = scale
                animation.particleScale = scale
                msg += "${Config.PRIMARY_COLOR}, scale ${Config.VAR_COLOR}$scale"
            }

            animation.start()
            Utils.sendMessage(player, msg)
        }
    }

    private val remove = subcommand("remove") {
        withAliases("rm")
        stringArgument("animationName") {
            replaceSuggestions(currentAnimationsSuggestion())
        }
        anyExecutor { sender, args ->
            val animationName = args["animationName"] as String

            val removedAnimation = AnimationsManager.getAnimation(animationName)
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
                "Removing ${Config.VAR_COLOR}${AnimationsManager.animationNames().size} ${Config.PRIMARY_COLOR}animations"
            )
            AnimationsManager.clearAnimations()
        }
    }

    private val pause = subcommand("pause") {
        withAliases("stop")
        stringArgument("animationName") {
            replaceSuggestions(currentAnimationsSuggestion())
        }
        anyExecutor { sender, args ->
            val animationName = args["animationName"] as String

            val animation = AnimationsManager.getAnimation(animationName)
            if (animation == null) {
                Utils.sendMessage(sender, "No animation ${Config.VAR_COLOR}$animationName")
                return@anyExecutor
            }

            animation.stop()
            Utils.sendMessage(sender, "Pausing ${Config.VAR_COLOR}${animation.name}")

        }
    }
    private val resume = subcommand("resume") {
        withAliases("start")
        stringArgument("animationName") {
            replaceSuggestions(currentAnimationsSuggestion())
        }
        anyExecutor { sender, args ->
            val animationName = args["animationName"] as String

            val animation = AnimationsManager.getAnimation(animationName)
            if (animation == null) {
                Utils.sendMessage(sender, "No animation ${Config.VAR_COLOR}$animationName")
                return@anyExecutor
            }

            animation.start()
            Utils.sendMessage(sender, "Resuming ${Config.VAR_COLOR}${animation.name}")

        }
    }

    private val list = subcommand("list") {
        withAliases("ls")
        anyExecutor { sender, args ->
            Utils.sendMessage(sender, "Animations: ${Config.VAR_COLOR}${AnimationsManager.animationNames()}")
        }
    }

    private val globalMaxParticlesPerTick = subcommand("globalmax") {
        withAliases("gmax")
        integerArgument("amount", optional = true)
        anyExecutor { sender, args ->
            val newMaxAmount = args["amount"] as? Int
            if (newMaxAmount == null) {
                Utils.sendMessage(
                    sender,
                    "Current max particles per tick: ${Config.VAR_COLOR}${AnimationsManager.maxParticlesPerTick}"
                )
                return@anyExecutor
            }
            AnimationsManager.maxParticlesPerTick = newMaxAmount.toInt()
            Utils.sendMessage(
                sender,
                "Set max particles per tick to ${Config.VAR_COLOR}${AnimationsManager.maxParticlesPerTick}"
            )
        }
    }

    private val particleScale = subcommand("particlescale") {
        withAliases("pscale")
        stringArgument("animationName") {
            replaceSuggestions(currentAnimationsSuggestion())
        }
        floatArgument("particleScale", optional = true) {
            replaceSuggestions(ArgumentSuggestions.strings({ info ->
                val animationName = info.previousArgs["animationName"] as String
                val animation = AnimationsManager.getAnimation(animationName)
                if (animation == null) return@strings arrayOf()
                return@strings arrayOf(animation.particleScale.toString())
            }))
        }
        anyExecutor { sender, args ->
            val animationName = args["animationName"] as String
            val newParticleScale = args["particleScale"] as? Float
            val animation = AnimationsManager.getAnimation(animationName)

            if (animation == null) {
                Utils.sendMessage(sender, "No animation ${Config.VAR_COLOR}${animationName}")
                return@anyExecutor
            }

            if (newParticleScale == null) {
                Utils.sendMessage(
                    sender,
                    "Current particle scale: ${Config.VAR_COLOR}${animation.particleScale}"
                )
                return@anyExecutor
            }
            animation.particleScale = newParticleScale
            Utils.sendMessage(
                sender,
                "Set particle scale to ${Config.VAR_COLOR}${animation.particleScale} ${Config.PRIMARY_COLOR}for ${Config.VAR_COLOR}${animation.name}"
            )
        }
    }

    private val animationScale = subcommand("animationscale") {
        withAliases("ascale")
        stringArgument("animationName") {
            replaceSuggestions(currentAnimationsSuggestion())
        }
        floatArgument("animationScale", optional = true) {
            replaceSuggestions(ArgumentSuggestions.strings({ info ->
                val animationName = info.previousArgs["animationName"] as String
                val animation = AnimationsManager.getAnimation(animationName)
                if (animation == null) return@strings arrayOf()
                return@strings arrayOf(animation.animationScale.toString())
            }))
        }
        anyExecutor { sender, args ->
            val animationName = args["animationName"] as String
            val newAnimationScale = args["animationScale"] as? Float
            val animation = AnimationsManager.getAnimation(animationName)

            if (animation == null) {
                Utils.sendMessage(sender, "No animation ${Config.VAR_COLOR}${animationName}")
                return@anyExecutor
            }

            if (newAnimationScale == null) {
                Utils.sendMessage(
                    sender,
                    "Current animation scale: ${Config.VAR_COLOR}${animation.animationScale}"
                )
                return@anyExecutor
            }
            animation.animationScale = newAnimationScale
            Utils.sendMessage(
                sender,
                "Set animation scale to ${Config.VAR_COLOR}${animation.animationScale} ${Config.PRIMARY_COLOR}for ${Config.VAR_COLOR}${animation.name}"
            )
        }
    }

    private val scale = subcommand("scale") {
        stringArgument("animationName") {
            replaceSuggestions(currentAnimationsSuggestion())
        }
        floatArgument("scale", optional = true) {
            replaceSuggestions(ArgumentSuggestions.strings({ info ->
                val animationName = info.previousArgs["animationName"] as String
                val animation = AnimationsManager.getAnimation(animationName)
                if (animation == null) return@strings arrayOf()
                return@strings arrayOf(animation.animationScale.toString(), animation.particleScale.toString())
            }))
        }
        anyExecutor { sender, args ->
            val animationName = args["animationName"] as String
            val newScale = args["scale"] as? Float
            val animation = AnimationsManager.getAnimation(animationName)

            if (animation == null) {
                Utils.sendMessage(sender, "No animation ${Config.VAR_COLOR}${animationName}")
                return@anyExecutor
            }

            if (newScale == null) {
                Utils.sendMessage(
                    sender,
                    "Current animation scale: ${Config.VAR_COLOR}${animation.animationScale} ${Config.PRIMARY_COLOR}and particle scale: ${Config.VAR_COLOR}${animation.particleScale}"
                )
                return@anyExecutor
            }
            animation.animationScale = newScale
            animation.particleScale = newScale
            Utils.sendMessage(
                sender,
                "Set animation and particle scale to ${Config.VAR_COLOR}${newScale} ${Config.PRIMARY_COLOR}for ${Config.VAR_COLOR}${animation.name}"
            )
        }
    }

    private val debug = subcommand("debug") {
        playerExecutor { player, args ->
            if (!AnimationsManager.debugPlayers.contains(player.uniqueId)) {
                AnimationsManager.debugPlayers.add(player.uniqueId)
                Utils.sendMessage(player, "Debug mode on")
                return@playerExecutor
            }
            AnimationsManager.debugPlayers.remove(player.uniqueId)
            Utils.sendMessage(player, "Debug mode off")
        }
    }

    private val rotation = subcommand("rotation") {
        stringArgument("animationName") {
            replaceSuggestions(currentAnimationsSuggestion())
        }
        floatArgument("rotation", optional = true, min = 0f, max = 360f) {
            replaceSuggestions(ArgumentSuggestions.strings({ info ->
                val animationName = info.previousArgs["animationName"] as String
                val animation = AnimationsManager.getAnimation(animationName)
                if (animation == null) return@strings arrayOf()
                return@strings arrayOf(Utils.toDegrees(animation.animationRotation).roundToInt().toString())
            }))
        }
        anyExecutor { sender, args ->
            val animationName = args["animationName"] as String
            val newRotation = args["rotation"] as? Float
            val animation = AnimationsManager.getAnimation(animationName)

            if (animation == null) {
                Utils.sendMessage(sender, "No animation ${Config.VAR_COLOR}${animationName}")
                return@anyExecutor
            }

            if (newRotation == null) {
                Utils.sendMessage(
                    sender,
                    "Current animation rotation: ${Config.VAR_COLOR}${Utils.toDegrees(animation.animationRotation)}°"
                )
                return@anyExecutor
            }
            animation.animationRotation = org.joml.Math.toRadians(newRotation)
            Utils.sendMessage(
                sender,
                "Set animation rotation to ${Config.VAR_COLOR}${newRotation}° ${Config.PRIMARY_COLOR}for ${Config.VAR_COLOR}${animation.name}"
            )
        }
    }

    private val renderType = subcommand("rendertype") {
        stringArgument("animationName") {
            replaceSuggestions(currentAnimationsSuggestion())
        }
        multiLiteralArgument(
            "renderType",
            optional = true,
            literals = RenderType.entries.map { it.toString() }.toTypedArray()
        )
        anyExecutor { sender, args ->
            val animationName = args["animationName"] as String
            val newRenderType = RenderType.entries.find { it.name == args["renderType"] }
            val animation = AnimationsManager.getAnimation(animationName)

            Utils.sendMessage(sender, "arg: $newRenderType")

            if (animation == null) {
                Utils.sendMessage(sender, "No animation ${Config.VAR_COLOR}${animationName}")
                return@anyExecutor
            }

            if (newRenderType == null) {
                Utils.sendMessage(
                    sender,
                    "Current animation render type: ${Config.VAR_COLOR}${animation.renderType}"
                )
                return@anyExecutor
            }

            if (newRenderType == RenderType.BLOCK_DISPLAY) {
                val value = AnimationsManager.reservedEntityIds[1]
                Utils.sendMessage(sender, "Value is $value")
                if (value == 0) {
                    AnimationsManager.initializeEntityIds()
                }
            }

            animation.renderType = newRenderType
            Utils.sendMessage(
                sender,
                "Set render type to ${Config.VAR_COLOR}${newRenderType} ${Config.PRIMARY_COLOR}for animation ${Config.VAR_COLOR}${animation.name}"
            )
        }
    }


    private fun currentAnimationsSuggestion(): ArgumentSuggestions<CommandSender>? {
        return ArgumentSuggestions.strings { AnimationsManager.animationNames().toTypedArray() }
    }
}