package net.tfminecraft.cooking.manager;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import me.Plugins.TLibs.Events.ItemPulseEvent;
import net.tfminecraft.cooking.item.FoodItem;
import net.tfminecraft.cooking.utils.ItemUpdater;

public class TagManager implements Listener {

    @EventHandler
    public void onOpen(ItemPulseEvent event) {
        ItemStack is = event.getItem();
        if (is == null) return;

        FoodItem fi = FoodItem.fromItem(is);
        if (fi == null) return;

        // Update item, patching if needed
        ItemStack updated = ItemUpdater.updateItem(is, fi, null);

        if (updated != null) {
            event.setItem(updated);
        }
    }
}
