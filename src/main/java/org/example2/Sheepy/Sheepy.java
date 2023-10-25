package org.example2.Sheepy;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class Sheepy extends JavaPlugin {

    public static List<List<float[]>> models = new ArrayList<>();

    @Override
    public void onEnable() {
        getCommand("test").setExecutor(new TestCommand());
        for (int i = 0; i < 80; i++) {
            models.add(ModelLoader.loadModel(new File(getDataFolder(), "len" + i + ".csv")));
        }
        //log
        getLogger().info("models loaded " + models.size() + " models");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
