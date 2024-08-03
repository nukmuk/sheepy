package me.nukmuk.sheepy

import org.bukkit.Particle
import org.bukkit.command.Command
import org.bukkit.command.CommandException
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.io.File

class SheepyCommand(private val plugin: Sheepy) : CommandExecutor, TabCompleter {

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
            val loader = AnimLoader(plugin)
            val folderName = args.getOrNull(1) ?: ""
            val files = loader.listAnims(folderName)?.filter { file -> file.name.endsWith(".shny") }
            Utils.sendMessage(player, files?.joinToString(" ") { file -> file.name } ?: "Folder $folderName empty")
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
