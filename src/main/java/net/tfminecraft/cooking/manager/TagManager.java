package net.tfminecraft.cooking.manager;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import net.tfminecraft.cooking.item.FoodItem;
import net.tfminecraft.cooking.utils.ItemUpdater;
import net.tfminecraft.cooking.utils.Keys;
import net.tfminecraft.tfmccore.itemscan.ItemScanHandler;

public class TagManager implements ItemScanHandler {

    @Override
    public boolean matches(ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR || !stack.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = stack.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(Keys.FOOD_ID, PersistentDataType.STRING);
    }

    @Override
    public void update(Player player, Inventory inventory, int slot, ItemStack stack) {
        FoodItem food = FoodItem.fromItem(stack);
        if (food == null) {
            return;
        }
        ItemStack updated = ItemUpdater.updateItem(stack, food, null);
        if (updated != null && inventory != null && slot >= 0) {
            inventory.setItem(slot, updated);
        }
    }
}
