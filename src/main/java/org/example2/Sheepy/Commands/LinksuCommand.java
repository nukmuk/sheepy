package org.example2.Sheepy.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.example2.Sheepy.Sheepy;
import org.jetbrains.annotations.NotNull;

public class LinksuCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length >= 1 && args[0].equals("reload")) {
            Sheepy.reloadPlugin();
            sender.sendMessage(ChatColor.GREEN + "[linksujump] config reloaded");
            return true;
        }
        return false;
    }
}
