package org.example2.Sheepy;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class StopCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {


        // get pending tasks
        List<BukkitTask> tasks = Bukkit.getScheduler().getPendingTasks();
        sender.sendMessage(tasks.size() + " tasks: " + tasks);
        sender.sendMessage(Arrays.toString(tasks.stream().map(BukkitTask::getTaskId).toArray()));
        StreamBytesCommand.stopParticleTasks();
        return true;
    }
}
