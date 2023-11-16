package org.example2.Sheepy;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class ThrowEvent implements Listener {

    private boolean flying;

    @EventHandler
    public void onPlayerMove(ProjectileLaunchEvent e) {

        if (flying) return;

        flying = true;
        BukkitTask task = AnimUtils.playRandomFile(e.getEntity(), 200, true);
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
