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

public class AnimLoader {

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
            LinksuJump.getPlugin().getLogger().info("error reading csv file");
            LinksuJump.getPlugin().getLogger().info(e.toString());
        }
        return particles;
    }
}