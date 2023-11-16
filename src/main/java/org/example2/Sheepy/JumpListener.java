package org.example2.Sheepy;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class JumpListener implements Listener {

    static FileConfiguration config = LinksuJump.getPlugin().getConfig();
    private final List<Player> flying = new ArrayList<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        float blockOffset = (float) config.getDouble("block-offset");
        boolean below = (e.getTo().clone().add(0, -blockOffset, 0).getBlock().getType() == Material.SLIME_BLOCK);
        boolean above = (e.getTo().clone().add(0, 2+blockOffset, 0).getBlock().getType() == Material.SLIME_BLOCK);
        if (!below && !above) return;
        if (flying.contains(p)) return;

        flying.add(p);

        long jumpDelay = config.getLong("jump-delay");
        new BukkitRunnable() {

            @Override
            public void run() {
                flying.remove(p);
            }
        }.runTaskLaterAsynchronously(LinksuJump.getPlugin(), jumpDelay);

        // particle trail
        new BukkitRunnable() {
            private int counter;
            private int max = config.getInt("particle-ticks");
            private final Color[] colors = new Color[]{
                    Color.fromRGB(141, 251, 0),
                    Color.fromRGB(255, 120, 0),
                    Color.fromRGB(0, 0, 0),

            };

            @Override
            public void run() {
                if (counter >= max) {
                    this.cancel();
                    return;
                }
//                java.awt.Color color = java.awt.Color.getHSBColor((float) counter / max, (float) (max - counter) /max, 1);
//                Color color1 = Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue());
                Color color1 = colors[counter % colors.length];
                Particle.DustOptions dustOptions = new Particle.DustOptions(color1, 1);
                p.getWorld().spawnParticle(Particle.REDSTONE, p.getLocation(), (int) Math.pow(max - counter, 2)/max, 0.5, 0.5, 0.5, dustOptions);
                counter++;
            }
        }.runTaskTimerAsynchronously(LinksuJump.getPlugin(), 0L, 1L);

        float jumpStrength = (float) config.getInt("jump-strength") / 5;
        float jumpAngle = (float) ((float) config.getInt("jump-angle") * Math.PI / 2 / 90);
        float pitchRandomness = (float) config.getDouble("pitch-randomness");

        Vector vel = p.getVelocity();
        float yaw = (float) ((e.getTo().getYaw() + 90) * (Math.PI / 180));
        float pitch = (90 - Math.abs(e.getTo().getPitch())) / 90;

        int updownmultiplier = 1;
        if (above) updownmultiplier = -1;

        Vector dir = new Vector(
                Math.cos(yaw) * pitch * Math.cos(jumpAngle),
                Math.sin(jumpAngle) * updownmultiplier,
                Math.sin(yaw) * pitch * Math.cos(jumpAngle))
                .normalize().multiply(jumpStrength);
        vel.add(dir);
        p.setVelocity(dir);
        p.getWorld().playSound(p, Sound.ENTITY_GHAST_SHOOT, 1f, (float) (1 - pitchRandomness / 2 + Math.random() * pitchRandomness));

        BukkitTask task = AnimUtils.playRandomFile(p, Math.max(LinksuJump.getPlugin().getConfig().getInt("frame-repeats"), 1), false);
    }
}
