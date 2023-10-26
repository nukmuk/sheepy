package org.example2.Sheepy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class LoadCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            String animName = args[0];
            Sheepy.anims.put(animName, AnimLoader.loadAnimSingleFile(animName + ".csv"));
            sender.sendMessage("loaded animation: " + animName);
            return true;
        } catch (Exception ignored) {
            sender.sendMessage("failed to load");
        }
        return false;
    }
}
