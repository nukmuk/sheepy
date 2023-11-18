package org.example2.Sheepy.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.example2.Sheepy.LinksuJump;
import org.jetbrains.annotations.NotNull;

public class LimitCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        LinksuJump.unlimitedAnimations = !LinksuJump.unlimitedAnimations;
        sender.sendMessage("[linksujump] toggled unlimited animations to " + LinksuJump.unlimitedAnimations);
        return true;
    }
}
