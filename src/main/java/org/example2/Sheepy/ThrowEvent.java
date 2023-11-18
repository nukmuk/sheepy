package org.example2.Sheepy;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class ThrowEvent implements Listener {

    private boolean flying;

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent e) {

        if (!(e.getEntity() instanceof Arrow) && !(e.getEntity() instanceof Egg)) return;
        if (!(e.getEntity().getShooter() instanceof Player) && !(e.getEntity().getShooter() instanceof BlockProjectileSource)) return;
        if (e.getEntity() instanceof Player) if (!(((Player) e.getEntity().getShooter()).hasPermission("linksujump.linksu"))) return;
        if (!LinksuJump.unlimitedAnimations) if (flying) return;

        flying = true;
//        new BukkitRunnable() {
//
//            @Override
//            public void run() {
//                flying = true;
//            }
//        }.runTaskLater(LinksuJump.getPlugin(), 1L);
        BukkitTask task = AnimUtils.playRandomFile(e.getEntity(), 100, true);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!e.getEntity().isValid() || e.getEntity().isOnGround()) {
                    task.cancel();
                    this.cancel();
                    flying = false;
                }
            }
        }.runTaskTimerAsynchronously(LinksuJump.getPlugin(), 0L, 1L);
    }
}
