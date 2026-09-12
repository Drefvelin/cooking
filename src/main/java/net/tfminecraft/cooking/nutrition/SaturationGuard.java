package net.tfminecraft.cooking.nutrition;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import net.tfminecraft.cooking.Cooking;

public final class SaturationGuard implements Listener {

    private static int taskId = -1;

    public static void start() {
        if (taskId != -1) {
            return;
        }
        taskId = Bukkit.getScheduler().runTaskTimer(Cooking.plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getSaturation() != 0f) {
                    player.setSaturation(0f);
                }
            }
        }, 40L, 40L).getTaskId();
    }

    public static void stop() {
        if (taskId == -1) {
            return;
        }
        Bukkit.getScheduler().cancelTask(taskId);
        taskId = -1;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().setSaturation(0f);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        Bukkit.getScheduler().runTask(Cooking.plugin, () -> player.setSaturation(0f));
    }
}
