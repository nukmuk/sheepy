package org.example2.Sheepy;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class JumpListener implements Listener {

    static FileConfiguration config = Sheepy.getPlugin().getConfig();
    private static float jumpStrength;
    private static long jumpDelay;
    private static float jumpAngle;
    private static float pitchRandomness;
    private static float blockOffset;
    private final List<Player> flying = new ArrayList<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (e.getTo().clone().add(0, -blockOffset, 0).getBlock().getType() != Material.SLIME_BLOCK) return;
        if (flying.contains(p)) return;

        flying.add(p);

        new BukkitRunnable() {

            @Override
            public void run() {
                flying.remove(p);
            }
        }.runTaskLaterAsynchronously(Sheepy.getPlugin(), jumpDelay);

        Vector vel = p.getVelocity();
        float yaw = (float) ((e.getTo().getYaw() + 90) * (Math.PI / 180));
        float pitch = (90 - Math.abs(e.getTo().getPitch())) / 90;
        Vector dir = new Vector(
                Math.cos(yaw) * pitch * Math.cos(jumpAngle),
                Math.sin(jumpAngle),
                Math.sin(yaw) * pitch * Math.cos(jumpAngle))
                .normalize().multiply(jumpStrength);
        vel.add(dir);
        p.setVelocity(dir);
        p.getWorld().playSound(p, Sound.ENTITY_GHAST_SHOOT, 1f, (float) (1 - pitchRandomness / 2 + Math.random() * pitchRandomness));

        AnimUtils.playRandomFile(p);
    }

    public static void reloadConfig() {
        jumpStrength = (float) config.getInt("jump-strength") / 5;
        jumpDelay = config.getLong("jump-delay");
        jumpAngle = (float) ((float) config.getInt("jump-angle") * Math.PI / 2 / 90);
        pitchRandomness = (float) config.getDouble("pitch-randomness");
        blockOffset = (float) config.getDouble("block-offset");
    }
}
