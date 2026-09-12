package net.tfminecraft.cooking.cup;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import net.tfminecraft.cooking.Cooking;
import net.tfminecraft.cooking.cache.ItemCache;
import net.tfminecraft.cooking.item.FoodItem;
import net.tfminecraft.cooking.nutrition.NutritionService;

public final class DrinkConsumeListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDrink(PlayerItemConsumeEvent event) {
        ItemStack consumed = event.getItem();
        if (consumed == null) {
            return;
        }

        boolean water = ItemCache.isCupOfWater(consumed);
        boolean milk = ItemCache.isCupOfMilk(consumed);
        if (!water && !milk) {
            return;
        }

        Player player = event.getPlayer();

        if (milk) {
            FoodItem food = FoodItem.fromItem(consumed);
            if (food != null) {
                food.updateAge();
                NutritionService.tryApplyEat(player, food);
            }
        }

        int amountBefore = consumed.getAmount();
        Bukkit.getScheduler().runTask(Cooking.plugin, () -> replaceWithEmptyCup(player, amountBefore));
    }

    private static void replaceWithEmptyCup(Player player, int amountBefore) {
        if (amountBefore > 1) {
            return;
        }

        ItemStack empty = CupItems.emptyCup();
        ItemStack main = player.getInventory().getItemInMainHand();
        ItemStack off = player.getInventory().getItemInOffHand();

        if (main.getType() == Material.GLASS_BOTTLE || main.getType() == Material.AIR) {
            player.getInventory().setItemInMainHand(empty);
        } else if (off.getType() == Material.GLASS_BOTTLE) {
            player.getInventory().setItemInOffHand(empty);
        } else if (!ItemCache.isCupOfWater(main) && !ItemCache.isCupOfMilk(main)) {
            player.getInventory().setItemInMainHand(empty);
        }
    }
}
