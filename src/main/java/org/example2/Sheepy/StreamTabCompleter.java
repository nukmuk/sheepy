package org.example2.Sheepy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StreamTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return switch (args.length) {
            case 1 -> Sheepy.animFileNames;
            case 2, 3 -> List.of("0.5", "1.0", "2.0");
            case 4 -> Arrays.stream(StreamCommand.ParticleType.values()).map(Enum::toString).collect(Collectors.toList());
            case 5 -> {
                if (args[3].equals(StreamCommand.ParticleType.multiple_dust.toString())) yield List.of("4000", "8000", "16000");
                else yield new ArrayList<>();
            }
            default -> new ArrayList<>();
        };
    }
}
