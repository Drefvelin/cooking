package net.tfminecraft.cooking.manager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import net.tfminecraft.cooking.Cooking;
import net.tfminecraft.cooking.item.FoodItem;
import net.tfminecraft.cooking.utils.ItemUpdater;

import java.util.List;

public class TagManager implements Listener {

    private final int PLAYERS_PER_TICK = 10;
    private int index = 0;

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        Inventory inv = event.getInventory();
        if (inv != null) {
            scanInventory((Player) event.getPlayer(), inv);
        }

        // Also scan the player's personal inventory
        scanInventory((Player) event.getPlayer(), event.getPlayer().getInventory());
    }

    public void start() {
        new BukkitRunnable() {

            @Override
            public void run() {

                List<? extends Player> players = Bukkit.getOnlinePlayers().stream().toList();
                if (players.isEmpty()) return;

                int processed = 0;

                while (processed < PLAYERS_PER_TICK && index < players.size()) {
                    Player player = players.get(index++);
                    scanPlayer(player);

                    processed++;
                }

                if (index >= players.size())
                    index = 0;
            }

        }.runTaskTimer(Cooking.plugin, 1L, 1L);
    }

    private void scanPlayer(Player player) {
        // Player inventory
        scanInventory(player, player.getInventory());

        // Top inventory (chest, furnace, etc)
        Inventory open = player.getOpenInventory().getTopInventory();
        if (open != null) {
            scanInventory(player, open);
        }
    }

    private void scanInventory(Player player, Inventory inventory) {
        if (inventory == null) return;
        InventoryHolder holder = inventory.getHolder();
        if (holder != null &&
            !(holder instanceof org.bukkit.block.BlockState) &&  // chests, barrels, etc
            !(holder instanceof org.bukkit.entity.Entity) &&     // horses, etc
            !(holder instanceof Player)) {                       // player crafting
            return;
        }

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);

            if (item == null || item.getType() == Material.AIR) continue;

            FoodItem fi = FoodItem.fromItem(item);
            if (fi == null) return;

            ItemUpdater.updateItem(item, fi, null);
        }
    }
}
