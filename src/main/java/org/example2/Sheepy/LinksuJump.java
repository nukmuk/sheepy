package org.example2.Sheepy;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.example2.Sheepy.Commands.LinksuCommand;

import java.util.Objects;

public final class LinksuJump extends JavaPlugin {

    private final FileConfiguration config = getConfig();
    private static LinksuJump plugin;

    @Override
    public void onEnable() {
        plugin = this;
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

    public static LinksuJump getPlugin() {
        return plugin;
    }

    public static void reloadPlugin() {
        plugin.reloadConfig();
        JumpListener.reloadConfig();
    }
}
