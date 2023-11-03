package org.example2.Sheepy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.example2.Sheepy.Sheepy.anims;

public class PlayCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player) && !(sender instanceof BlockCommandSender)) {
            sender.sendMessage(ChatColor.RED + "only players can use this command - " + sender.getClass());
            return true;
        }


        // create getLocation function


        final Location loc = getLocation(sender);

        assert loc != null;
        World world = loc.getWorld();

        Plugin plugin = Sheepy.getPlugin(Sheepy.class);
        List<List<float[]>> anim = args.length > 0 ? anims.get(args[0]) : anims.entrySet().iterator().next().getValue();

        sender.sendMessage("playing " + anim.size() + " frames");

        // cancel all tasks
        for (BukkitTask task : Bukkit.getScheduler().getPendingTasks()) {
            task.cancel();
        }

        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count >= anim.size() - 1) {
                    sender.sendMessage("done");
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
                StreamCommand.playFrame(frame, args, loc);

//                sender.sendMessage("" + count);
                count++;
            }
        }.runTaskTimer(plugin, 0L, 1L);


        Component msg = Component.text("Hello, ")
                .append(Component.text(sender.getName(), TextColor.color(0xFF00FF)))
                .append(Component.text("! How are you today?"));
        sender.sendMessage(msg);

        return false;
    }

    static Location getLocation(CommandSender sender) {
        Vector offset = new Vector(0.5, 1, 0.5);
        if (sender instanceof Player player) {
            return player.getTargetBlock(null, 100).getLocation().add(offset);
        } else if (sender instanceof BlockCommandSender blockCommandSender) {
            return blockCommandSender.getBlock().getLocation().add(offset);
        }
        return null;
    }
}
