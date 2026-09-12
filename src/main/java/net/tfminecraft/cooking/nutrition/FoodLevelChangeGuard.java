package net.tfminecraft.cooking.nutrition;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import net.tfminecraft.cooking.Cooking;

public final class FoodLevelChangeGuard implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (NutritionDisplayService.isSyncing(player)) {
            return;
        }
        event.setCancelled(true);
        Bukkit.getScheduler().runTask(Cooking.plugin, () -> NutritionDisplayService.syncFromPlayer(player));
    }
}
