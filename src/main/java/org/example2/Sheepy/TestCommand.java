package org.example2.Sheepy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.example2.Sheepy.Sheepy.anims;

public class TestCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "only players can use this command");
            return true;
        }

        World world = player.getWorld();

        Plugin plugin = Sheepy.getPlugin(Sheepy.class);
        List<List<float[]>> anim = anims.get("len");

        player.sendMessage("anim loaded " + anim.size() + " frames");

        BukkitTask task = new BukkitRunnable() {
            int count = 0;
            final Location loc = player.getLocation();

            @Override
            public void run() {
                if (count >= anim.size() - 1) {
                    this.cancel();
                }

                /* rainbow


                java.awt.Color awtColor = java.awt.Color.getHSBColor(count / 100f, 1, 1);
                float[] colors = new float[3];
                awtColor.getRGBColorComponents(colors);
                for (int i = 0; i < colors.length; i++) {
                    colors[i] *= 255;
                }
                Color color = Color.fromRGB((int) colors[0], (int) colors[1], (int) colors[2]);

                Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1);
                world.spawnParticle(Particle.REDSTONE, player.getLocation(), 100, 1, 1, 1, dustOptions);
                 */

                // loop over particles in model
                List<float[]> frame = anim.get(count);
                for (float[] point : frame) {

                    Color color = Color.fromRGB((int) point[3], (int) point[4], (int) point[5]);
                    float pscale = args.length > 1 ? Float.parseFloat(args[1]) : 1f;
                    Particle.DustOptions dustOptions = new Particle.DustOptions(color, pscale);

                    world.spawnParticle(Particle.REDSTONE, loc.clone().add(point[0], point[1] + 10, point[2]), 1, 0, 0, 0, dustOptions);
                }

                player.sendMessage("" + count);
                count++;
            }
        }.runTaskTimer(plugin, 0L, 1L);


        Component msg = Component.text("Hello, ")
                .append(Component.text(sender.getName(), TextColor.color(0xFF00FF)))
                .append(Component.text("! How are you today?"));
        sender.sendMessage(msg);

        return false;
    }
}
