package org.example2.Sheepy;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Sheepy extends JavaPlugin {

    public static Map<String, List<List<float[]>>> anims = new HashMap<>();

    @Override
    public void onEnable() {
        getCommand("test").setExecutor(new TestCommand());
        anims.put("len", ModelLoader.loadAnim("len"));
//        getLogger().info("models loaded " + anims.size() + " anims");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
