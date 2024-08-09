package me.nukmuk.sheepy

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class SheepyCommand(private val plugin: Sheepy) : CommandExecutor, TabCompleter {

    val loader: AnimLoader = AnimLoader(plugin)
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
            /*
            for (i in 1..10000) {
                player.world.spawnParticle(
                    Particle.CLOUD,
                    player.location.add(Vector(i * 0.003, 2.0, 0.0)),
                    0,
                    0.0,
                    0.0,
                    0.0,
                )
            }
             */
            val sch = Bukkit.getScheduler()
            Utils.sendMessage(player, "active workers ${sch.activeWorkers}, pending ${sch.pendingTasks}")

            return true
        } else if (subcommand == "files") {
            val folderName = args.getOrNull(1) ?: ""
            val files = loader.getAnimsInFolder(folderName)
            Utils.sendMessage(
                player,
                files?.joinToString("${ChatColor.GRAY}, ${ChatColor.RESET}") { file -> file.name }
                    ?: "Folder ${folderName.replace(Regex("[^a-zA-Z0-9_-]"), "")} empty")
            return true
        } else if (subcommand == "stream" || subcommand == "st" || subcommand == "load") {

            val fileName = Utils.sanitizeString(args.getOrNull(1))

            if (fileName == null) {
                Utils.sendMessage(player, "Please provide filename")
                return false
            }

            if (animations.containsKey(fileName)) {
                Utils.sendMessage(player, "Animation ${Config.VAR_COLOR}${fileName} already exists")
                return true
            }

            val files = loader.getAnimsInFolder()

            val file = files?.find { it.nameWithoutExtension == fileName }

            if (file == null) {
                Utils.sendMessage(player, "File ${Config.VAR_COLOR}$fileName ${Config.PRIMARY_COLOR}not found")
                return true
            }

            Utils.sendMessage(player, "Streaming file ${Config.VAR_COLOR}$file")

            val animation =
                Animation(file, player, plugin, player.getTargetBlock(null, 10).location.add(0.0, 1.0, 0.0), animations)

            if (subcommand == "stream" || subcommand == "st") {
                animation.start()
            }

            animations.put(animation.name, animation)

            return true
        } else if (subcommand == "stop") {
            Utils.sendMessage(player, "stopping ${Config.VAR_COLOR}${animations.size}")
            animations.forEach { it.value.stop() }
            animations.clear()
            Utils.sendMessage(player, "removed, now running ${Config.VAR_COLOR}${animations.size}")
            return true
        } else if (subcommand == "step") {
            if (animations.isEmpty) {
                Utils.sendMessage(player, "No animations found")
            } else {
                animations.forEach { it.value.step() }
            }
            return true
        } else if (subcommand == "list" || subcommand == "ls") {
            Utils.sendMessage(player, "Animations: ${Config.VAR_COLOR}${animations}")
            return true
        } else if (subcommand == "tasks") {
            Utils.sendMessage(player, "activeWorkers: ${Config.VAR_COLOR}${Bukkit.getScheduler().activeWorkers}")
            Utils.sendMessage(player, "pendingTasks: ${Config.VAR_COLOR}${Bukkit.getScheduler().pendingTasks}")
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
