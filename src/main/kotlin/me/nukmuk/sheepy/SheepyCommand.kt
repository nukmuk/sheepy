package me.nukmuk.sheepy

import com.destroystokyo.paper.MaterialSetTag
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files

class SheepyCommand(private val plugin: Sheepy) : CommandExecutor, TabCompleter {

    val loader: AnimLoader = AnimLoader(plugin)
    val animations = ArrayList<Animation>()

    override fun onCommand(commandSender: CommandSender, command: Command, s: String, args: Array<String>): Boolean {

        if (commandSender !is Player) {
            commandSender.sendMessage("You must be a player to use this command.")
            return false
        }
        if (args.isEmpty()) return false
        val player: Player = commandSender


        val subcommand = args[0]

        if (subcommand == "test") {
            try {

            } catch (error: Exception) {
                plugin.logger.info(error.message)
            }
            for (i in 1..10000) {
                player.spawnParticle(
                    Particle.CLOUD,
                    player.location.add(Vector(i * 0.003, 0.0, 0.0)),
                    0,
                    -1.0,
                    0.0,
                    0.0,
                    i.toDouble() / 10000,
                    null,
                    false
                )
            }
            return true
        } else if (subcommand == "list") {
            val folderName = args.getOrNull(1) ?: ""
            val files = loader.getAnimsInFolder(folderName)
            Utils.sendMessage(
                player,
                files?.joinToString("${ChatColor.GRAY}, ${ChatColor.RESET}") { file -> file.name }
                    ?: "Folder ${folderName.replace(Regex("[^a-zA-Z0-9_-]"), "")} empty")
            return true
        } else if (subcommand == "stream" || subcommand == "st") {

            val fileName = args.getOrNull(1) ?: ""

            val files = loader.getAnimsInFolder()

            val file = files?.find { it.nameWithoutExtension == fileName }

            if (file == null) {
                Utils.sendMessage(player, "File $file not found")
                return true
            }

            Utils.sendMessage(player, "Streaming file $file")

            val animation = Animation(file, player, plugin, player.getTargetBlock(null, 10).location.add(0.0, 1.0, 0.0))
            animation.start()
            animations.add(animation)

        } else if (subcommand == "stop") {
            Utils.sendMessage(player, "stopping ${animations.size}")
            animations.forEach { it.stop(); animations.remove(it) }
            Utils.sendMessage(player, "stopped, now running ${animations.size}")
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
