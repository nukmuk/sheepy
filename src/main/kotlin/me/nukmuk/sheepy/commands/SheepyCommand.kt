package me.nukmuk.sheepy.commands

import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.*
import me.nukmuk.sheepy.*
import me.nukmuk.sheepy.renderers.packet.PacketEntityHandler
import me.nukmuk.sheepy.utils.Utils
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.command.CommandSender
import kotlin.math.roundToInt

class SheepyCommand(private val plugin: Sheepy) {

    fun register() {
        commandAPICommand("sheepy") {
            withAliases("sh")
            withPermission("sheepy.use")
//            withUsage("/sh create <name>")
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
            subcommand(tphere)
            subcommand(text)
            subcommand(textmode)
            subcommand(textrotationmode)
            subcommand(reload)

            subcommand(TestCommand(plugin).test)

            anyExecutor { sender, args ->
                sender.sendMessage(
                    Utils.mm.deserialize(
                        @Suppress("DEPRECATION")
                        "<dark_gray><st>        <reset> ${Config.PLUGIN_NAME_COLORS} <gray>${plugin.description.version} <dark_gray><st>        <reset>\n" +
                                "<reset>${Config.VAR_COLOR}To get started:\n" +
                                "${Config.VAR_COLOR}- ${Config.PRIMARY_COLOR}/sh create ${Config.VAR_COLOR}<animation>\n" +
                                "${Config.VAR_COLOR}- ${Config.PRIMARY_COLOR}/sh rendertype ${Config.VAR_COLOR}<animation> <type>\n" +
                                "${Config.VAR_COLOR}- ${Config.PRIMARY_COLOR}/sh scale ${Config.VAR_COLOR}<animation> <number>"
                    )
                )

            }

        }
    }

    private val files = subcommand("files") {
        anyExecutor { sender, args ->
            val files = AnimationsManager.getAnimsInFolder(plugin)
            Utils.sendMessage(
                sender,
                if (!files.isEmpty()) "All animations: ${Config.VAR_COLOR}" + files.joinToString("${Config.PRIMARY_COLOR}, ${Config.VAR_COLOR}") { file -> file.name } else "Folder empty")
        }
    }

    private val create = subcommand("create") {
//        withAliases("c", "st")

        stringArgument("fileName") {
            replaceSuggestions(ArgumentSuggestions.strings {
                AnimationsManager.animsInFolder.map { it.nameWithoutExtension }.toTypedArray()
            })
        }
        stringArgument("animationName", optional = true) {
            replaceSuggestions(ArgumentSuggestions.strings { info -> arrayOf(info.previousArgs()["fileName"].toString()) })
        }
        booleanArgument("repeat", optional = true)
        floatArgument("scale", optional = true) {
            replaceSuggestions(ArgumentSuggestions.strings("1.0"))
        }
        multiLiteralArgument(
            "renderType", optional = true, literals = RenderType.entries.map { it.toString() }.toTypedArray()
        )
        playerExecutor { player, args ->

            val fileName = args["fileName"] as String
            val animationName = (args["animationName"] ?: fileName) as String
            val repeat = (args["repeat"] ?: false) as Boolean
            val scale = (args["scale"]) as? Float
            val renderType = parseRenderType(args["renderType"] as String?)

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

            val files = AnimationsManager.getAnimsInFolder(plugin)

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
                repeat
            )

            if (scale != null) {
                animation.animationScale = scale
                animation.particleScale = scale
                msg += "${Config.PRIMARY_COLOR}, scale ${Config.VAR_COLOR}$scale"
            }

            if (renderType != null) {
                animation.renderType = renderType
                msg += "${Config.PRIMARY_COLOR}, render type ${Config.VAR_COLOR}$renderType"
            }

            animation.start()
            Utils.sendMessage(player, msg)
        }
    }

    private val remove = subcommand("remove") {
//        withAliases("rm")
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
//        withAliases("stop")
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
//        withAliases("start")
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
//        withAliases("ls")
        anyExecutor { sender, args ->
            Utils.sendMessage(sender, "Animations: ${Config.VAR_COLOR}${AnimationsManager.animationNames()}")
        }
    }

    private val globalMaxParticlesPerTick = subcommand("maxparticles") {
//        withAliases("gmax", "globalmax")
        integerArgument("amount", optional = true, min = 0, max = 16384) {
            replaceSuggestions(ArgumentSuggestions.strings {
                arrayOf(AnimationsManager.maxParticlesPerTick.toString())
            })
        }
        anyExecutor { sender, args ->
            val newMaxAmount = args["amount"] as? Int
            val maxParticlesString =
                { if (AnimationsManager.maxParticlesPerTick == 0) "UNLIMITED" else AnimationsManager.maxParticlesPerTick.toString() }
            if (newMaxAmount == null) {
                Utils.sendMessage(
                    sender, "Current max particles per tick: ${Config.VAR_COLOR}${maxParticlesString()}"
                )
                return@anyExecutor
            }
            AnimationsManager.maxParticlesPerTick = newMaxAmount.toInt()


            Utils.sendMessage(
                sender, "Set max particles per tick to ${Config.VAR_COLOR}${maxParticlesString()}"
            )
        }

    }

    private val particleScale = subcommand("particlescale") {
//        withAliases("pscale")
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
                    sender, "Current particle scale: ${Config.VAR_COLOR}${animation.particleScale}"
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
//        withAliases("ascale")
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
                    sender, "Current animation scale: ${Config.VAR_COLOR}${animation.animationScale}"
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
        floatArgument("rotationY", optional = true, min = 0f, max = 360f) {
            replaceSuggestions(ArgumentSuggestions.strings({ info ->
                val animationName = info.previousArgs["animationName"] as String
                val animation = AnimationsManager.getAnimation(animationName)
                if (animation == null) return@strings arrayOf()
                return@strings arrayOf(Utils.toDegrees(animation.animationRotationY).roundToInt().toString())
            }))
        }
        floatArgument("rotationX", optional = true, min = 0f, max = 360f) {
            replaceSuggestions(ArgumentSuggestions.strings({ info ->
                val animationName = info.previousArgs["animationName"] as String
                val animation = AnimationsManager.getAnimation(animationName)
                if (animation == null) return@strings arrayOf()
                return@strings arrayOf(Utils.toDegrees(animation.animationRotationX).roundToInt().toString())
            }))
        }
        floatArgument("rotationZ", optional = true, min = 0f, max = 360f) {
            replaceSuggestions(ArgumentSuggestions.strings({ info ->
                val animationName = info.previousArgs["animationName"] as String
                val animation = AnimationsManager.getAnimation(animationName)
                if (animation == null) return@strings arrayOf()
                return@strings arrayOf(Utils.toDegrees(animation.animationRotationZ).roundToInt().toString())
            }))
        }
        anyExecutor { sender, args ->
            val animationName = args["animationName"] as String
            val newRotationY = args["rotationY"] as? Float
            val newRotationX = args["rotationX"] as? Float
            val newRotationZ = args["rotationZ"] as? Float
            val animation = AnimationsManager.getAnimation(animationName)

            if (animation == null) {
                Utils.sendMessage(sender, "No animation ${Config.VAR_COLOR}${animationName}")
                return@anyExecutor
            }

            if (newRotationY == null) {
                Utils.sendMessage(
                    sender,
                    "Animation ${Config.VAR_COLOR}${animation.name} ${Config.PRIMARY_COLOR}current rotation: " +
                            "Y: ${Config.VAR_COLOR}${Utils.toDegrees(animation.animationRotationY)}° " +
                            "${Config.PRIMARY_COLOR}X: ${Config.VAR_COLOR}${Utils.toDegrees(animation.animationRotationY)}° " +
                            "${Config.PRIMARY_COLOR}Z: ${Config.VAR_COLOR}${Utils.toDegrees(animation.animationRotationY)}°"
                )
                return@anyExecutor
            }
            animation.animationRotationY = org.joml.Math.toRadians(newRotationY)
            if (newRotationX != null)
                animation.animationRotationX = org.joml.Math.toRadians(newRotationX)
            if (newRotationZ != null)
                animation.animationRotationZ = org.joml.Math.toRadians(newRotationZ)

            Utils.sendMessage(
                sender,
                "Set animation ${Config.VAR_COLOR}${animation.name} ${Config.PRIMARY_COLOR}rotation to " +
                        "Y: ${Config.VAR_COLOR}${animation.animationRotationY}° " +
                        "${Config.PRIMARY_COLOR}X: ${Config.VAR_COLOR}${animation.animationRotationX}° " +
                        "${Config.PRIMARY_COLOR}Z: ${Config.VAR_COLOR}${animation.animationRotationZ}°"
            )
        }
    }

    private val renderType = subcommand("rendertype") {
        stringArgument("animationName") {
            replaceSuggestions(currentAnimationsSuggestion())
        }
        multiLiteralArgument(
            "renderType", optional = true, literals = RenderType.entries.map { it.toString() }.toTypedArray()
        )
        anyExecutor { sender, args ->
            val animationName = args["animationName"] as String
            val newRenderType = parseRenderType(args["renderType"] as String?)
            val animation = AnimationsManager.getAnimation(animationName)

            if (animation == null) {
                Utils.sendMessage(sender, "No animation ${Config.VAR_COLOR}${animationName}")
                return@anyExecutor
            }

            if (newRenderType == null) {
                Utils.sendMessage(
                    sender, "Current animation render type: ${Config.VAR_COLOR}${animation.renderType}"
                )
                return@anyExecutor
            }

            animation.renderType = null

            plugin.logger.info("Running EntityCleaner after rendertype changed")
            PacketEntityHandler.cleanEntityRenderers(plugin)
            animation.renderType = newRenderType
            Utils.sendMessage(
                sender,
                "Set render type to ${Config.VAR_COLOR}${newRenderType} ${Config.PRIMARY_COLOR}for animation ${Config.VAR_COLOR}${animation.name}"
            )
        }
    }

    private val tphere = subcommand("tphere") {
        stringArgument("animationName") {
            replaceSuggestions(currentAnimationsSuggestion())
        }
        playerExecutor { sender, args ->
            val animationName = args["animationName"] as String
            val animation = AnimationsManager.getAnimation(animationName)

            if (animation == null) {
                Utils.sendMessage(sender, "No animation ${Config.VAR_COLOR}${animationName}")
                return@playerExecutor
            }

            animation.location = sender.location

            plugin.logger.info("Running EntityCleaner after ${animation.name} location changed")
            PacketEntityHandler.cleanEntityRenderers(plugin)
            Utils.sendMessage(
                sender,
                "Teleported ${Config.VAR_COLOR}${animation.name} ${Config.PRIMARY_COLOR}to you"
            )
        }
    }

    private val text = subcommand("text") {
        stringArgument("animationName") {
            replaceSuggestions(currentAnimationsSuggestion())
        }
        textArgument("text", optional = true) {
            replaceSuggestions(ArgumentSuggestions.strings({ info ->
                val animationName = info.previousArgs["animationName"] as String
                val animation = AnimationsManager.getAnimation(animationName)
                if (animation == null) return@strings arrayOf()
                return@strings arrayOf(animation.textForTextRenderer.toString())
            }))

        }
        anyExecutor { sender, args ->
            val animationName = args["animationName"] as String
            val newText = args["text"] as String?
            val animation = AnimationsManager.getAnimation(animationName)

            if (animation == null) {
                Utils.sendMessage(sender, "No animation ${Config.VAR_COLOR}${animationName}")
                return@anyExecutor
            }

            if (newText == null) {
                Utils.sendMessage(
                    sender,
                    "Animation ${Config.VAR_COLOR}${animation.name} ${Config.PRIMARY_COLOR}text is currently ${Config.VAR_COLOR}${animation.textMode}"
                )
                return@anyExecutor
            }

            animation.textForTextRenderer = newText

            plugin.logger.info("Running EntityCleaner after ${animation.name} text changed")
            PacketEntityHandler.cleanEntityRenderers(plugin)
            Utils.sendMessage(
                sender,
                "Set animation ${Config.VAR_COLOR}${animation.name} ${Config.PRIMARY_COLOR}text to ${Config.VAR_COLOR}${animation.textForTextRenderer}"
            )
        }
    }

    private val textmode = subcommand("textmode") {
        stringArgument("animationName") {
            replaceSuggestions(currentAnimationsSuggestion())
        }
        multiLiteralArgument(
            "textMode",
            literals = TextMode.entries.map { it.toString() }.toTypedArray(),
            optional = true
        )
        anyExecutor { sender, args ->
            val animationName = args["animationName"] as String
            val newTextMode = parseTextMode(args["textMode"] as String?)
            val animation = AnimationsManager.getAnimation(animationName)

            if (animation == null) {
                Utils.sendMessage(sender, "No animation ${Config.VAR_COLOR}${animationName}")
                return@anyExecutor
            }

            if (newTextMode == null) {
                Utils.sendMessage(
                    sender,
                    "Animation ${Config.VAR_COLOR}${animation.name} ${Config.PRIMARY_COLOR}text mode is currently ${Config.VAR_COLOR}${animation.textMode}"
                )
                return@anyExecutor
            }

            animation.textMode = newTextMode

            plugin.logger.info("Running EntityCleaner after ${animation.name} text mode changed")
            PacketEntityHandler.cleanEntityRenderers(plugin)
            Utils.sendMessage(
                sender,
                "Set animation ${Config.VAR_COLOR}${animation.name} ${Config.PRIMARY_COLOR}text mode to ${Config.VAR_COLOR}${animation.textMode}"
            )
        }
    }

    private val textrotationmode = subcommand("textrotation") {
        stringArgument("animationName") {
            replaceSuggestions(currentAnimationsSuggestion())
        }
        multiLiteralArgument(
            "textRotation",
            literals = RandomRotation.entries.map { it.toString() }.toTypedArray(),
            optional = true
        )
        anyExecutor { sender, args ->
            val animationName = args["animationName"] as String
            val newRotationMode = RandomRotation.entries.find { it.name == args["textRotation"] }
            val animation = AnimationsManager.getAnimation(animationName)

            if (animation == null) {
                Utils.sendMessage(sender, "No animation ${Config.VAR_COLOR}${animationName}")
                return@anyExecutor
            }

            if (newRotationMode == null) {
                Utils.sendMessage(
                    sender,
                    "Animation ${Config.VAR_COLOR}${animation.name} ${Config.PRIMARY_COLOR}text rotation mode is currently ${Config.VAR_COLOR}${animation.randomRotationMode}"
                )
                return@anyExecutor
            }

            animation.randomRotationMode = newRotationMode

            plugin.logger.info("Running EntityCleaner after ${animation.name} RandomRotation changed")
            PacketEntityHandler.cleanEntityRenderers(plugin)
            Utils.sendMessage(
                sender,
                "Set animation ${Config.VAR_COLOR}${animation.name} ${Config.PRIMARY_COLOR}text random rotation mode to ${Config.VAR_COLOR}${animation.randomRotationMode}"
            )
        }
    }
    private val reload = subcommand("reload") {
        anyExecutor { sender, args ->
            plugin.reloadConfig()
            Utils.sendMessage(sender, "Config reloaded")
        }
    }


    private fun currentAnimationsSuggestion(): ArgumentSuggestions<CommandSender>? {
        return ArgumentSuggestions.strings { AnimationsManager.animationNames().toTypedArray() }
    }
}
