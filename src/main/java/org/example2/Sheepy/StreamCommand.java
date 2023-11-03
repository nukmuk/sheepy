package org.example2.Sheepy;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.example2.Sheepy.AnimLoader.convertToParticle;
import static org.example2.Sheepy.AnimLoader.plugin;

public class StreamCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player) && !(sender instanceof BlockCommandSender)) {
            sender.sendMessage(ChatColor.RED + "only players can use this command - " + sender.getClass());
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("please specify file");
            return true;
        }

        Bukkit.getScheduler().cancelTasks(plugin);

        String fileName = args[0].replaceAll("[^a-zA-Z0-9]", "") + ".csv";

        File pluginFolder = plugin.getDataFolder();
        File animFile = new File(pluginFolder, fileName);

        plugin.getLogger().info("streaming: " + fileName);
        BlockingQueue<List<float[]>> frames = new ArrayBlockingQueue<>(1); // smaller numbers seem to have better performance

        // particle attrib: float
        // particle:        float[]
        // frame:           List<float[]>
        // animation:       Queue<List<float[]>>

        new BukkitRunnable() {
            @Override
            public void run() {

                List<float[]> frame = new ArrayList<>();

                try {
                    BufferedReader br = new BufferedReader(new FileReader(animFile));
                    String line;
                    Location loc = PlayCommand.getLocation(sender);
                    BukkitTask spawnParticles = new BukkitRunnable() {

                        @Override
                        public void run() {
                            sender.sendActionBar(Component.text("frames in queue: " + frames.size()));
                            List<float[]> frame = frames.poll();
                            if (frame == null) {
                                try {
                                    boolean ignored = br.ready();
                                } catch (IOException e) {
                                    sender.sendActionBar(Component.text(ChatColor.GREEN + "end :)"));
                                    this.cancel();
                                    return;
                                }
                                sender.sendActionBar(Component.text(ChatColor.RED + "lagaa"));
                                return;
                            }
                            playFrame(frame, args, loc);
                        }

                    }.runTaskTimer(plugin, 0L, 1L);

                    while ((line = br.readLine()) != null && !spawnParticles.isCancelled()) {

                        // add frame to frames
                        if (line.startsWith("f")) {
                            frames.put(frame);
                            frame = new ArrayList<>();

//                            if (frames.size() % 500 == 0) sender.sendMessage(Component.text("loaded frame " + frames.size()));

                            continue;
                        }

                        float[] particle = convertToParticle(line);
                        frame.add(particle);

                    }
                    br.close();
                } catch (Exception e) {
                    // send message to minecraft console
                    sender.sendMessage(ChatColor.RED + "file not found");
                }

            }
        }.runTaskAsynchronously(plugin);

        return false;
    }

    static void playFrame(List<float[]> frame, @NotNull String[] args, Location loc) {
        for (float[] point : frame) {

            World world = loc.getWorld();

            Color color = Color.fromRGB((int) point[3], (int) point[4], (int) point[5]);
            float pscale = args.length > 1 ? Float.parseFloat(args[1]) : 1f;
            Particle.DustOptions dustOptions = new Particle.DustOptions(color, pscale);

            float scale = args.length > 2 ? Float.parseFloat(args[2]) : 1f;

            Vector pointPos = new Vector(point[0], point[1], point[2]).multiply(scale);
            Vector pos = pointPos.add(loc.toVector());

            world.spawnParticle(Particle.REDSTONE, pos.toLocation(world), 1, 0, 0, 0, dustOptions);
        }
    }
}
