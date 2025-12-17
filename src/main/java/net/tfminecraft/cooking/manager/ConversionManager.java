package net.tfminecraft.cooking.manager;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import me.Plugins.TLibs.Events.ItemPulseEvent;
import net.tfminecraft.cooking.item.FoodItem;
import net.tfminecraft.cooking.loader.ConversionLoader;
import net.tfminecraft.cooking.utils.FoodParser;
import net.tfminecraft.cooking.utils.InventoryAdder;
import net.tfminecraft.cooking.utils.ItemBuilder;

public class ConversionManager implements Listener{
    @EventHandler
    public void pickup(EntityPickupItemEvent e) {
        ItemStack item = e.getItem().getItemStack();
        if (FoodItem.fromItem(item) != null) return;
        if (!(e.getEntity() instanceof Player)) return;

        Player p = (Player) e.getEntity();
        String result = ConversionLoader.getByItem(item);

        if (result != null) {
            e.setCancelled(true);
            e.getItem().remove();

            ItemStack stack = ItemBuilder.buildSingleString(result, item);
            stack.setAmount(item.getAmount());

            ItemStack leftover = InventoryAdder.addItem(p, stack);

            if (leftover == null) {
                p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
            } else {
                p.getWorld().dropItemNaturally(p.getLocation(), leftover);
            }
        }
    }


    @EventHandler
    public void pulse(ItemPulseEvent e){
        ItemStack item = e.getItem();
        if(FoodItem.fromItem(item) != null) return;
        String result = ConversionLoader.getByItem(item);
        if(result!=null){
            ItemStack stack = ItemBuilder.buildSingleString(result, item);
            stack.setAmount(item.getAmount());
            e.setItem(stack);
        }
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent e) {
        Inventory top = e.getInventory();

        // --- Only process VANILLA inventories ---
        InventoryHolder holder = top.getHolder();
        if (holder != null &&
            !(holder instanceof org.bukkit.block.BlockState) &&  // chests, barrels, etc
            !(holder instanceof org.bukkit.entity.Entity) &&     // horses, etc
            !(holder instanceof Player)) {                       // player crafting
            return;
        }

        // --- original logic continues here ---
        Player p = (Player) e.getPlayer();
        Inventory bottom = p.getInventory();

        int topSize = top.getSize();
        int total = topSize + bottom.getSize();

        for (int i = total - 1; i >= 0; i--) {
            ItemStack a = getSlot(i, top, bottom, topSize);
            if (a == null) continue;

            FoodItem fa = FoodItem.fromItem(a);
            if (fa == null) continue;

            for (int j = 0; j < total; j++) {
                if (i == j) continue;

                ItemStack b = getSlot(j, top, bottom, topSize);
                if (b == null) continue;

                FoodItem fb = FoodItem.fromItem(b);
                if (fb == null) continue;

                if (InventoryAdder.equalsFood(fa, fb)) {
                    ItemStack clone = b.clone();
                    clone.setAmount(a.getAmount());
                    setSlot(i, clone, top, bottom, topSize);
                    break;
                }
            }
        }
    }


    private ItemStack getSlot(int index, Inventory top, Inventory bottom, int topSize) {
        return index < topSize ? top.getItem(index) : bottom.getItem(index - topSize);
    }

    private void setSlot(int index, ItemStack item, Inventory top, Inventory bottom, int topSize) {
        if (index < topSize) top.setItem(index, item);
        else bottom.setItem(index - topSize, item);
    }

}

