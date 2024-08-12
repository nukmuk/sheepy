package me.nukmuk.sheepy

import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundAnimatePacket
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player

class SheepyCommand(private val plugin: Sheepy) : CommandExecutor, TabCompleter {

    val animations = HashMap<String, Animation>()

    override fun onCommand(commandSender: CommandSender, command: Command, s: String, args: Array<String>): Boolean {

        if (commandSender !is Player) {
            commandSender.sendMessage("You must be a player to use this command.")
            return false
        }
        if (args.isEmpty()) return false
        val player: Player = commandSender


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
            val files = Utils.getAnimsInFolder(plugin, folderName)
            Utils.sendMessage(
                player,
                files?.joinToString("${ChatColor.GRAY}, ${ChatColor.RESET}") { file -> file.name }
                    ?: "Folder ${folderName.replace(Regex("[^a-zA-Z0-9_-]"), "")} empty")
            return true
        } else if (subcommand == "stream" || subcommand == "st" || subcommand == "load") {

            val fileName = Utils.sanitizeString(args.getOrNull(1))
            val repeat: Boolean = args.getOrNull(2) == "true"

            if (fileName == null) {
                Utils.sendMessage(player, "Please provide filename")
                return false
            }

            if (animations.containsKey(fileName)) {
                Utils.sendMessage(player, "Animation ${Config.VAR_COLOR}${fileName} already exists")
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

            val animation =
                Animation(file, player, plugin, player.getTargetBlock(null, 10).location.add(0.0, 1.0, 0.0), animations)

            animation.repeat = repeat

            if (subcommand == "stream" || subcommand == "st") {
                animation.start()
            }

            animations.put(animation.name, animation)

            return true
        } else if (subcommand == "remove" || subcommand == "rm") {
            val animationName = args.getOrNull(1)
            if (animationName == null) return false

            val removedAnimation = animations.remove(animationName)
            if (removedAnimation != null) {
                removedAnimation.remove()
                Utils.sendMessage(player, "Removed and stopped ${Config.VAR_COLOR}${removedAnimation.name}")
            } else {
                Utils.sendMessage(player, "No animation ${Config.VAR_COLOR}${animationName}")
            }
            return true
        } else if (subcommand == "clear") {
            Utils.sendMessage(player, "stopping ${Config.VAR_COLOR}${animations.size}")
            animations.forEach { it.value.remove() }
            animations.clear()
            Utils.sendMessage(player, "removed, now running ${Config.VAR_COLOR}${animations.size}")
            return true

        } else if (subcommand == "stop" || subcommand == "pause" || subcommand == "start") {
            val animationName = Utils.sanitizeString(args.getOrNull(1))

            if (animationName == null) return false
            val animation = animations[animationName]
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
            if (animations.isEmpty) {
                Utils.sendMessage(player, "No animations found")
            } else {
                animations.forEach { it.value.stepFrame() }
            }
            return true
        } else if (subcommand == "list" || subcommand == "ls") {
            Utils.sendMessage(player, "Animations: ${Config.VAR_COLOR}${animations.keys}")
            return true
        } else if (subcommand == "tasks") {
            Utils.sendMessage(player, "activeWorkers: ${Config.VAR_COLOR}${Bukkit.getScheduler().activeWorkers}")
            Utils.sendMessage(player, "pendingTasks: ${Config.VAR_COLOR}${Bukkit.getScheduler().pendingTasks}")
            return true
        } else if (subcommand == "max") {
            val animName = args.getOrNull(1)
            val anim = animations[animName]
            val newMax = args.getOrNull(2)?.toIntOrNull()
            anim?.maxParticlesPerFrame = newMax ?: anim.maxParticlesPerFrame
            return true
        } else if (subcommand == "pscale") {
            val animName = args.getOrNull(1)
            val anim = animations[animName]
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
