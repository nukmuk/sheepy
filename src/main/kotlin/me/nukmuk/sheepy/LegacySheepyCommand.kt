package me.nukmuk.sheepy

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class LegacySheepyCommand(private val plugin: Sheepy) : CommandExecutor, TabCompleter {

    override fun onCommand(commandSender: CommandSender, command: Command, s: String, args: Array<String>): Boolean {

        if (commandSender !is Player) {
            commandSender.sendMessage("You must be a player to use this command.")
            return false
        }
        if (args.isEmpty()) return false
        val player: Player = commandSender

        if (!player.hasPermission("sheepy.use")) {
            Utils.sendMessage(player, Config.Strings.NO_PERMISSION)
            return true
        }

        val subcommand = args[0]

        if (subcommand == "test") {

//            for (i in 1..5000) {

//                player.world.spawnParticle(Particle.CLOUD, player.location, 1)

//                player.world.spawn(player.location.add(Vector(i * 0.1, 0.0, 5.0)), TextDisplay::class.java) { entity ->
//                    entity.text(Component.text("a", NamedTextColor.DARK_BLUE))
//                    entity.billboard = Display.Billboard.VERTICAL // pivot only around the vertical axis
//                    entity.backgroundColor = Color.RED // make the background red
//                    entity.isPersistent = false
//                }

//            }


            return true
        } else if (subcommand == "files") {
            val folderName = args.getOrNull(1) ?: ""
            val files = Utils.getAnimsInFolder(plugin)
            Utils.sendMessage(
                player,
                files.joinToString("${ChatColor.GRAY}, ${ChatColor.RESET}") { file -> file.name }
                    ?: "Folder ${folderName.replace(Regex("[^a-zA-Z0-9_-]"), "")} empty")
            return true
        } else if (subcommand == "stream" || subcommand == "st" || subcommand == "load") {

            val fileName = Utils.sanitizeString(args.getOrNull(1))
            var animationName = Utils.sanitizeString(args.getOrNull(2))
            val repeat: Boolean = args.getOrNull(3) == "true"

            if (fileName == null) {
                Utils.sendMessage(player, "Please provide filename")
                return false
            }

            if (animationName == null) animationName = fileName

            if (AnimationsPlayer.animationNames().contains(animationName)) {
                Utils.sendMessage(player, "Animation ${Config.VAR_COLOR}${animationName} already exists")
                return true
            }

            val files = Utils.getAnimsInFolder(plugin)

            val file = files?.find { it.nameWithoutExtension == fileName }

            if (file == null) {
                Utils.sendMessage(player, "File ${Config.VAR_COLOR}$fileName ${Config.PRIMARY_COLOR}not found")
                return true
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

            if (subcommand == "stream" || subcommand == "st") {
                animation.start()
            }

            return true
        } else if (subcommand == "remove" || subcommand == "rm") {
            val animationName = args.getOrNull(1)
            if (animationName == null) return false

            val removedAnimation = AnimationsPlayer.getAnimation(animationName)
            removedAnimation?.remove()
            if (removedAnimation != null) {
                Utils.sendMessage(player, "Removed and stopped ${Config.VAR_COLOR}${removedAnimation.name}")
            } else {
                Utils.sendMessage(player, "No animation ${Config.VAR_COLOR}${animationName}")
            }
            return true
        } else if (subcommand == "clear") {
            Utils.sendMessage(player, "stopping ${Config.VAR_COLOR}${AnimationsPlayer.animationNames().size}")
            AnimationsPlayer.clearAnimations()
            return true

        } else if (subcommand == "stop" || subcommand == "pause" || subcommand == "start") {
            val animationName = Utils.sanitizeString(args.getOrNull(1))

            if (animationName == null) return false
            val animation = AnimationsPlayer.getAnimation(animationName)
            if (animation == null) {
                Utils.sendMessage(player, "No animation ${Config.VAR_COLOR}$animationName")
                return true
            }

            if (subcommand == "stop" || subcommand == "pause") {
                animation.stop()
                Utils.sendMessage(player, "paused ${Config.VAR_COLOR}${animation.name}")
            } else {
                animation.start()
                Utils.sendMessage(player, "started ${Config.VAR_COLOR}${animation.name}")
            }

            return true
        } else if (subcommand == "step") {
            if (AnimationsPlayer.animationNames().isEmpty()) {
                Utils.sendMessage(player, "No animations found")
            } else {
                Utils.sendMessage(player, "Not implemented")
            }
            return true
        } else if (subcommand == "list" || subcommand == "ls") {
            Utils.sendMessage(player, "Animations: ${Config.VAR_COLOR}${AnimationsPlayer.animationNames()}")
            return true
        } else if (subcommand == "tasks") {
            Utils.sendMessage(player, "activeWorkers: ${Config.VAR_COLOR}${Bukkit.getScheduler().activeWorkers}")
            Utils.sendMessage(player, "pendingTasks: ${Config.VAR_COLOR}${Bukkit.getScheduler().pendingTasks}")
            return true
        } else if (subcommand == "max") {
            val newMax = args.getOrNull(1)?.toIntOrNull()
            if (newMax == null) {
                return false
            }
            AnimationsPlayer.maxParticlesPerTick = newMax
            return true
        } else if (subcommand == "pscale") {
            val animName = args.getOrNull(1).toString()
            val anim = AnimationsPlayer.getAnimation(animName)
            val newScale = args.getOrNull(2)?.toFloatOrNull()
            anim?.particleScale = newScale ?: anim.particleScale
            return true
        }
        return false
    }

    override fun onTabComplete(
        commandSender: CommandSender,
        command: Command,
        s: String,
        strings: Array<String>
    ): List<String> {
        return listOf()
    }
}
