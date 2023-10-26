package org.example2.Sheepy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.bukkit.Bukkit.getScheduler;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

public class StopCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // get pending tasks
        List<BukkitTask> tasks = getScheduler().getPendingTasks();
        sender.sendMessage("cancelling " + tasks.size() + " tasks");

        getScheduler().cancelTasks(getPlugin(Sheepy.class));
        return true;
    }
}
