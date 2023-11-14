package org.example2.Sheepy.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.example2.Sheepy.Sheepy;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class StreamTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return switch (args.length) {
            case 1 -> getCsvFileNames();
            case 2, 3 -> List.of("0.5", "1.0", "2.0");
            case 4 -> Arrays.stream(StreamBytesCommand.ParticleType.values()).map(Enum::toString).collect(Collectors.toList());
            case 5 -> {
                if (args[3].equals(StreamBytesCommand.ParticleType.multiple_dust.toString())) yield List.of("4000", "8000", "16000");
                else yield new ArrayList<>();
            }
            default -> new ArrayList<>();
        };
    }

    private List<String> getCsvFileNames() {
        List<File> animFiles = List.of(Objects.requireNonNull(Sheepy.getPlugin().getDataFolder().listFiles()));
        animFiles = animFiles.stream()
                .filter(File::isFile)
                .toList();

        return animFiles.stream()
                .map(file -> file.getName().substring(0, file.getName().length() - 4))
                .toList();
    }
}
