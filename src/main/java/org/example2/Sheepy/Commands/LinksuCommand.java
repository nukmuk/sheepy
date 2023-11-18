package org.example2.Sheepy.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.example2.Sheepy.LinksuJump;
import org.jetbrains.annotations.NotNull;

public class LinksuCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length > 0) {
            switch (args[0]) {
                case "limit":
                    LinksuJump.unlimitedAnimations = !LinksuJump.unlimitedAnimations;
                    sender.sendMessage(ChatColor.GREEN + "[linksujump] toggled unlimited animations to " + LinksuJump.unlimitedAnimations);
                    return true;

                case "reload":
                    LinksuJump.getPlugin().reloadConfig();
                    sender.sendMessage(ChatColor.GREEN + "[linksujump] reloading config failed successfully");
                    LinksuJump.getPlugin().onEnable();
                    return true;
            }
        }
        return false;
    }
}
