package org.example2.Sheepy;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public final class Sheepy extends JavaPlugin {

    public static Map<String, List<List<float[]>>> anims = new HashMap<>();
    public static List<String> animFileNames = new ArrayList<>();

    @Override
    public void onEnable() {
//        Objects.requireNonNull(getCommand("test")).setExecutor(new PlayCommand());
        Objects.requireNonNull(getCommand("s")).setExecutor(new StopCommand());
//        Objects.requireNonNull(getCommand("load")).setExecutor(new LoadCommand());
        Objects.requireNonNull(getCommand("stream")).setExecutor(new StreamBytesCommand());

        animFileNames = getAnimFileNames();

        Objects.requireNonNull(getCommand("stream")).setTabCompleter(new StreamTabCompleter());


        // load all animations
//        assert animFolders != null;
//        for (File folder : animFolders) {
//            anims.put(folder.getName(), AnimLoader.loadAnim(folder.getName()));
//        }
//        anims.put("len", AnimLoader.loadAnim("len"));
//        getLogger().info("loaded " + anims.size() + " anims");
    }

    private List<String> getAnimFileNames() {
        List<File> animFiles = List.of(Objects.requireNonNull(getDataFolder().listFiles()));
        String extension = ".shny";
        animFiles = animFiles.stream()
                .filter(File::isFile)
                .filter(file -> file.getName().endsWith(extension))
                .toList();

        return animFiles.stream()
                .map(file -> file.getName().substring(0, file.getName().length() - extension.length()))
                .toList();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
