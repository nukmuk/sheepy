package org.example2.Sheepy;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModelLoader {

    static Plugin plugin = Sheepy.getPlugin(Sheepy.class);

    //create laoder function
    public static List<float[]> loadModel(File file) {
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
                String[] values = line.split(",");
                float x = Float.parseFloat(values[0]);
                float y = Float.parseFloat(values[1]) - 14;
                float z = Float.parseFloat(values[2]) + 0;
                float r = Float.parseFloat(values[3]) * 255;
                float g = Float.parseFloat(values[4]) * 255;
                float b = Float.parseFloat(values[5]) * 255;
//                float s = Float.parseFloat(values[6]);

                // add particle to particles
                float[] particle = {x, y, z, r, g, b, 1};
//                plugin.getLogger().info("particle " + Arrays.toString(particle));
                particles.add(particle);
            }
        } catch (Exception e) {
            // send message to minecraft console
            plugin.getLogger().info("error reading csv file");
            plugin.getLogger().info(e.toString());
        }
        return particles;
    }
}