package org.example2.Sheepy;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Sheepy extends JavaPlugin {

    public static Map<String, List<List<float[]>>> anims = new HashMap<>();

    @Override
    public void onEnable() {
        Objects.requireNonNull(getCommand("test")).setExecutor(new TestCommand());
        // get plugin data folder
        File[] animFolders = getDataFolder().listFiles();


        // load all animations
//        assert animFolders != null;
//        for (File folder : animFolders) {
//            anims.put(folder.getName(), AnimLoader.loadAnim(folder.getName()));
//        }
//        anims.put("len", AnimLoader.loadAnim("len"));joo
        anims.put("test", AnimLoader.loadAnimSingleFile("test.csv"));
        getLogger().info("loaded " + anims.size() + " anims");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
