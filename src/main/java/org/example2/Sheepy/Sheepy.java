package org.example2.Sheepy;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.example2.Sheepy.Commands.LinksuCommand;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Sheepy extends JavaPlugin {

    private Map<String, List<List<float[]>>> jumpAnims = new HashMap<>();
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


        // load all animations
//        assert animFolders != null;
//        for (File folder : animFolders) {
//            anims.put(folder.getName(), AnimLoader.loadAnim(folder.getName()));
//        }
//        anims.put("len", AnimLoader.loadAnim("len"));
//        getLogger().info("loaded " + anims.size() + " anims");
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
