package org.example2.Sheepy;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.example2.Sheepy.Commands.LinksuCommand;

import java.util.Objects;

public final class Sheepy extends JavaPlugin {

    private final FileConfiguration config = getConfig();
    private static Sheepy plugin;

    @Override
    public void onEnable() {
        plugin = this;
        config.addDefault("jump-strength", 10.0);
        config.options().copyDefaults(true);
        saveConfig();
        Objects.requireNonNull(getCommand("linksu")).setExecutor(new LinksuCommand());

        reloadPlugin();

        getServer().getPluginManager().registerEvents(new JumpListener(), this);

        AnimUtils.loadAnimations();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static Sheepy getPlugin() {
        return plugin;
    }

    public static void reloadPlugin() {
        plugin.reloadConfig();
        JumpListener.reloadConfig();
    }
}
