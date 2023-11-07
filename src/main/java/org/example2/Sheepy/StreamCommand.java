package org.example2.Sheepy;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.example2.Sheepy.AnimLoader.convertToParticle;
import static org.example2.Sheepy.AnimLoader.plugin;

public class StreamCommand implements CommandExecutor {

    static class Animation {
        private boolean dontLoad = false;
        private BlockingQueue<List<float[]>> frames;

        private Animation(BlockingQueue<List<float[]>> frames) {
            this.frames = frames;
        }
    }

    public enum ParticleType {
        REDSTONE,
        multiple_dust,
        SPELL_MOB,
        SPELL_MOB_AMBIENT,
        DUST_TRANSITION,
    }

    static final List<Animation> animations = new ArrayList<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // args: <filename> [particle scale] [animation scale] [particle type 1-4]

        if (!(sender instanceof Player) && !(sender instanceof BlockCommandSender)) {
            sender.sendMessage(ChatColor.RED + "only players can use this command - " + sender.getClass());
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("please specify file");
            return true;
        }

        stopParticleTasks();

        String fileName = args[0].replaceAll("[^a-zA-Z0-9]", "") + ".csv";
        ParticleType particleType = args.length > 3 ? ParticleType.valueOf(args[3]) : ParticleType.REDSTONE;


        // particle attrib: float
        // particle:        float[]
        // frame:           List<float[]>
        // animation:       Queue<List<float[]>>

        BlockingQueue<List<float[]>> frames = new ArrayBlockingQueue<>(1); // smaller numbers seem to have better performance
        Location loc = PlayCommand.getLocation(sender);
        Animation animation = new Animation(frames);
        animations.add(animation);

        // particle spawner
        new BukkitRunnable() {

            @Override
            public void run() {

                sender.sendActionBar(Component.text(ChatColor.GRAY + "frames in queue: " + frames.size()));
                List<float[]> frame = frames.poll();
                if (frame == null) {
                    if (animation.dontLoad) {
                        sender.sendActionBar(Component.text(ChatColor.GREEN + "end :)"));
                        animation.frames.clear();
                        animations.remove(animation);
                        this.cancel();
                        return;
                    }
                    sender.sendActionBar(Component.text(ChatColor.RED + "lagaa"));
                    return;
                }
                playFrame(frame, args, loc, particleType);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // file reader
        new BukkitRunnable() {
            @Override
            public void run() {

                File pluginFolder = plugin.getDataFolder();
                File animFile = new File(pluginFolder, fileName);

                plugin.getLogger().info("streaming: " + fileName);

                try (BufferedReader br = new BufferedReader(new FileReader(animFile))) {

                    List<float[]> frame = new ArrayList<>();

                    while (!animation.dontLoad) {

                        String line = br.readLine();
                        if (line == null) {
                            animation.dontLoad = true;
                            return;
                        }

                        // add frame to frames
                        if (line.startsWith("f")) {
                            frames.put(frame);
                            frame = new ArrayList<>();
                            continue;
                        }

                        float[] particle = convertToParticle(line);
                        frame.add(particle);
                    }
                } catch (Exception e) {
                    // send message to minecraft console
                    sender.sendMessage(ChatColor.RED + "file not found " + e.getClass());
                }
            }
        }.runTaskAsynchronously(plugin);

        return false;
    }

    static void playFrame(List<float[]> frame, @NotNull String[] args, Location loc, ParticleType particleType) {
        for (float[] point : frame) {

            World world = loc.getWorld();

            Color color = Color.fromRGB((int) point[3], (int) point[4], (int) point[5]);
            float custom_pscale = args.length > 1 ? Float.parseFloat(args[1]) : 1f;
            float pscale = point.length > 6 ? point[6] * custom_pscale : custom_pscale;
            Particle.DustOptions dustOptions = new Particle.DustOptions(color, pscale);

            float scale = args.length > 2 ? Float.parseFloat(args[2]) : 1f;

            Vector pointPos = new Vector(point[0], point[1], point[2]).multiply(scale);
            Vector pos = pointPos.add(loc.toVector());

            switch (particleType){
                case REDSTONE:
                    world.spawnParticle(Particle.REDSTONE, pos.toLocation(world), 1, 0, 0, 0, 0, dustOptions, true);
                    break;

                case multiple_dust:
                    // spawn ~16k particles total every tick so previous frames disappear
                    int particlesPerTick = 8000;
                    int total = args.length > 4 ? Integer.parseInt(args[4]) : particlesPerTick;
                    int amount = Math.floorDiv(total, frame.size());
                    world.spawnParticle(Particle.REDSTONE, pos.toLocation(world), amount, 0, 0, 0, 1, dustOptions, true);
                    break;

                case SPELL_MOB:
                    world.spawnParticle(Particle.SPELL_MOB, pos.toLocation(world), 0, 1-color.getRed(), 1-color.getGreen(), 1-color.getBlue(), 1, null, true);
                    break;

                case SPELL_MOB_AMBIENT:
                    world.spawnParticle(Particle.SPELL_MOB_AMBIENT, pos.toLocation(world), 0, 1-color.getRed(), 1-color.getGreen(), 1-color.getBlue(), 1, null, true);
                    break;

                case DUST_TRANSITION:
                    Particle.DustTransition dustTransition = new Particle.DustTransition(color, color, pscale);
                    world.spawnParticle(Particle.DUST_COLOR_TRANSITION, pos.toLocation(world), 0, dustTransition);
                    break;
            }




        }
    }

    static void playFrame(List<float[]> frame, @NotNull String[] args, Location loc) {
        playFrame(frame, args, loc, ParticleType.REDSTONE);
    }

        static void stopParticleTasks() {
        animations.forEach(animation -> {
            animation.dontLoad = true;
            animation.frames.clear();
        });
        animations.clear();
    }
}
