package net.tfminecraft.cooking.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.Plugins.TLibs.TLibs;
import net.tfminecraft.cooking.Cooking;
import net.tfminecraft.cooking.cache.FurnitureCache;
import net.tfminecraft.cooking.cache.ItemCache;
import net.tfminecraft.cooking.cooking.CookingReference;
import net.tfminecraft.cooking.cooking.FryingReference;
import net.tfminecraft.cooking.cooking.PotReference;
import net.tfminecraft.cooking.cooking.SauceReference;
import net.tfminecraft.cooking.enums.Method;
import net.tfminecraft.cooking.item.FoodItem;
import net.tfminecraft.events.FurnitureBreakEvent;
import net.tfminecraft.events.FurnitureInteractEvent;
import net.tfminecraft.events.FurnitureSlotItemAddEvent;
import net.tfminecraft.events.FurnitureSlotItemTakeEvent;
import net.tfminecraft.furniture.Furniture;

public class CookingManager implements Listener {

    private Map<UUID, CookingReference> stations = new HashMap<>();

    public void start() {
        tickCycle();
    }

    public void tickCycle() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for(Map.Entry<UUID, CookingReference> entry : stations.entrySet()) {
                    entry.getValue().tick();
                }
            }
        }.runTaskTimer(Cooking.plugin, 20L, 20L);
    }

    @EventHandler
    public void empty(PlayerInteractEvent e) {
        if(!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItemInMainHand();
        if(item == null) return;
        if(!e.getClickedBlock().getType().equals(Material.CAULDRON)) return;
        FoodItem fi = FoodItem.fromItem(item);
        if(fi == null) return;
        if(fi.getCategory().equalsIgnoreCase("sauce") || fi.getCategory().equalsIgnoreCase("soup")) {
            e.setCancelled(true);
            p.getInventory().setItemInMainHand(TLibs.getItemAPI().getCreator().getItemFromPath(ItemCache.ladle));
        }
    }
    @EventHandler
    public void interact(FurnitureInteractEvent e) {
        Furniture f = e.getFurniture();
        if(stations.containsKey(f.getEntityId())) {
            CookingReference ref = stations.get(f.getEntityId());
            ref.interact(e);
        } else {
            Method m = FurnitureCache.getByFurniture(f);
            if(m == Method.NONE) return;
            CookingReference ref = null;
            if(m == Method.FRYING_PAN) {
                ref = new FryingReference(f, m);
            } else if(m == Method.SAUCEPAN) {
                ref = new SauceReference(f, m);
            } else if(m == Method.POT) {
                ref = new PotReference(f, m);
            } else {
                ref = new CookingReference(f, m);
            }
            stations.put(f.getEntityId(), ref);
            ref.interact(e);
        }
    }

    @EventHandler
    public void breakEvent(FurnitureBreakEvent e) {
        Furniture f = e.getFurniture();
        if(stations.containsKey(f.getEntityId())) {
            CookingReference ref = stations.remove(f.getEntityId());
            if(ref instanceof SauceReference) ref.clear();
            if(ref instanceof PotReference) ref.clear();
            else ref.remove();
        }
    }

    @EventHandler
    public void addItem(FurnitureSlotItemAddEvent e) {
        Furniture f = e.getFurniture();
        if(stations.containsKey(f.getEntityId())) {
            CookingReference ref = stations.get(f.getEntityId());
            ref.slotAdd(e);
        }
    }

    @EventHandler
    public void takeItem(FurnitureSlotItemTakeEvent e) {
        Furniture f = e.getFurniture();
        if(stations.containsKey(f.getEntityId())) {
            CookingReference ref = stations.get(f.getEntityId());
            ref.slotRemove(e);
        }
    }
}
