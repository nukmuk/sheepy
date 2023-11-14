package org.example2.Sheepy;

import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.getLogger;

public class AnimLoader {

    public static List<List<float[]>> loadAnim(String folderName) {


        // get plugin data folder and get the files inside it
        File pluginFolder = Sheepy.getPlugin().getDataFolder();
        File animFolder = new File(pluginFolder, folderName);

        File[] files = animFolder.listFiles();

        // create list of frames
        Sheepy.getPlugin().getLogger().info("loading anim " + folderName);
        List<List<float[]>> frames = new ArrayList<>();

        // loop over files
        for (int i = 1; i < files.length; i++) {
            if (i % 1000 == 0) {
                getLogger().info("loaded frame " + i);
            }
            // load frame
            List<float[]> frame = loadFrame(new File(animFolder, i + ".csv"));
            // add frame to frames
            frames.add(frame);
        }

        Sheepy.getPlugin().getLogger().info("loaded " + files.length + " frames");

        return frames;
    }

    public static List<List<float[]>> loadAnimSingleFile(String fileName) {


        // get plugin data folder and get the files inside it
        File pluginFolder = Sheepy.getPlugin().getDataFolder();
        File animFile = new File(pluginFolder, fileName);

        // create list of frames
        Sheepy.getPlugin().getLogger().info("loading anim " + fileName);
        List<List<float[]>> frames = new ArrayList<>();

        List<float[]> particles = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(animFile));
            String line;
//            int counter = 0;

            while ((line = br.readLine()) != null) {

                // add frame to frames
                if (line.startsWith("f")) {
                    frames.add(particles);
                    particles = new ArrayList<>();

                    if (frames.size() % 500 == 0) {
                        getLogger().info("loaded frame " + frames.size());
                    }

                    continue;
                }

//                counter++;
                float[] particle = convertToParticle(line);
                particles.add(particle);
            }
            br.close();
        } catch (Exception e) {
            // send message to minecraft console
            Sheepy.getPlugin().getLogger().info("error reading csv file");
            Sheepy.getPlugin().getLogger().info(e.toString());
        }
        return frames;
    }

    public static Location getLocation(CommandSender sender) {
        Vector offset = new Vector(0.5, 1, 0.5);
        if (sender instanceof Player player) {
            return player.getTargetBlock(null, 64).getLocation().add(offset);
        } else if (sender instanceof BlockCommandSender blockCommandSender) {
            return blockCommandSender.getBlock().getLocation().add(offset);
        }
        return null;
    }

    static float[] convertToParticle(String line) {
        String[] values = line.split(",");
        float x = Float.parseFloat(values[0]);
        float y = Float.parseFloat(values[1]);
        float z = Float.parseFloat(values[2]);
        float r = Float.parseFloat(values[3]) * 255;
        float g = Float.parseFloat(values[4]) * 255;
        float b = Float.parseFloat(values[5]) * 255;
        float s = values.length > 6 ? Float.parseFloat(values[6]) : 1.0f;

        return new float[]{x, y, z, r, g, b, s};
    }

    //create laoder function
    public static List<float[]> loadFrame(File file) {
        List<float[]> particles = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            int counter = 0;


            while ((line = br.readLine()) != null) {
                line = line.replace("(", "").replace(")", "").replace("\"", "");

                // don't read all lines
//                if (counter % 200 != 0) {
//                    counter++;
//                    continue;
//                }

                // skip reading first line
                if (counter == 0) {
                    counter++;
                    continue;
                }

                counter++;
                float[] particle = convertToParticle(line);
                particles.add(particle);
            }
            br.close();
        } catch (Exception e) {
            // send message to minecraft console
            Sheepy.getPlugin().getLogger().info("error reading csv file");
            Sheepy.getPlugin().getLogger().info(e.toString());
        }
        return particles;
    }
}