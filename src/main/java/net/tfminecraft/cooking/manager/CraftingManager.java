package net.tfminecraft.cooking.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

import me.Plugins.TLibs.TLibs;
import net.tfminecraft.InteractibleFurniture;
import net.tfminecraft.cooking.Cooking;
import net.tfminecraft.cooking.cache.FurnitureCache;
import net.tfminecraft.cooking.cache.ItemCache;
import net.tfminecraft.cooking.crafting.CraftingStation;
import net.tfminecraft.cooking.loader.CraftingStationLoader;
import net.tfminecraft.events.FurnitureBreakEvent;
import net.tfminecraft.events.FurnitureInteractEvent;
import net.tfminecraft.events.FurniturePlaceEvent;
import net.tfminecraft.events.FurnitureSlotItemAddEvent;
import net.tfminecraft.events.FurnitureSlotItemTakeEvent;
import net.tfminecraft.furniture.Furniture;
import net.tfminecraft.furniture.FurnitureSlot;

public class CraftingManager implements Listener {

    public Map<UUID, CraftingStation> stations = new HashMap<>();
    private final Map<UUID, Long> churnCooldown = new HashMap<>();

    private boolean isOnCooldown(Furniture f) {
        Long until = churnCooldown.get(f.getEntityId());
        if (until == null) return false;
        return System.currentTimeMillis() < until;
    }

    private void startCooldown(Furniture f, long millis) {
        churnCooldown.put(f.getEntityId(), System.currentTimeMillis() + millis);
    }


    @EventHandler
    public void furnitureInteract(FurnitureSlotItemAddEvent e) {
        Furniture f = e.getFurniture();
        Player p = e.getPlayer();
        if(FurnitureCache.isButterChurn(f)) {
            p.getInventory().setItemInMainHand(new ItemStack(Material.MILK_BUCKET, 1));
            p.updateInventory();
            f.getLoc().getWorld().playSound(f.getLoc(), Sound.ITEM_BUCKET_FILL, 1f, 1f); //TODO SOUND
            return;
        }

        if (stations.containsKey(f.getEntityId())) {
            CraftingStation station = stations.get(f.getEntityId());
            station.addItem(e);
            return;
        }

        for (CraftingStation s : CraftingStationLoader.get()) {
            if (s.getBlockId().equalsIgnoreCase(f.getType().getId())) {
                CraftingStation newStation = new CraftingStation(f, s);
                stations.put(f.getEntityId(), newStation);
                newStation.addItem(e);
                return;
            }
        }
    }

    @EventHandler
    public void furnitureInteract(FurnitureSlotItemTakeEvent e) {

        Furniture f = e.getFurniture();
        if(FurnitureCache.isButterChurn(f)) {
            e.setCancelled(true);
            return;
        }
        if (stations.containsKey(f.getEntityId())) {
            CraftingStation station = stations.get(f.getEntityId());
            station.removeItem(e);
            return;
        }
    }

    @EventHandler
    public void furnitureInteract(FurnitureInteractEvent e) {
        Player p = e.getPlayer();
        Furniture f = e.getFurniture();
        if(FurnitureCache.isButterChurn(f)) {
            if(f.getActiveSlots().size() == 1 && e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
                FurnitureSlot slot = f.getType().getSlot("stick");
                if(slot == null) return;
                if(!f.hasActiveSlot(slot.getId())) return;
                slot.clearModel();
            } else if(f.getActiveSlots().size() > 1) {
                Furniture carried = InteractibleFurniture.getInstance().getFurnitureManager().getByCarrier(p);
                if(carried != null) p.sendMessage("carried "+carried.getId());
                if(carried != null && FurnitureCache.isButterPlate(carried)) {
                    for(FurnitureSlot slot : carried.getType().getSlots().values()) {
                        slot.forceModel(TLibs.getItemAPI().getCreator().getItemFromPath(ItemCache.butterPieceModel));
                        carried.addActiveSlot(slot);
                        slot.followParentTransform((ItemDisplay) Bukkit.getEntity(f.getEntityId()));
                    }
                    f.getLoc().getWorld().playSound(f.getLoc(), Sound.ENTITY_ITEM_FRAME_ADD_ITEM, 1f, 1f);
                    return;
                }
                FurnitureSlot slot = f.getType().getSlot("stick");
                if(slot == null) return;
                ItemDisplay display = (ItemDisplay) Bukkit.getEntity(slot.getDisplayStandId());
                if (display == null) return;

                // Check cooldown
                if (isOnCooldown(f)) {
                    // Optional feedback:
                    // e.getPlayer().sendActionBar(Component.text("§cChurn is still moving!"));
                    return;
                }

                // Start animation + cooldown
                playStickChurnAnimation(display);
                startCooldown(f, 20 * 50L); // duration*2 ticks * 50ms per tick

                f.getLoc().getWorld().playSound(f.getLoc(), Sound.ENTITY_COW_MILK, 1f, 1f);
            }
        }
        if (stations.containsKey(f.getEntityId())) {
            CraftingStation station = stations.get(f.getEntityId());
            station.interact(e);
        }
    }

    @EventHandler
    public void furnitureBreak(FurnitureBreakEvent e) {
        Furniture f = e.getFurniture();
        if(FurnitureCache.isButterChurn(f)) {
            churnCooldown.remove(f.getEntityId());
            for(FurnitureSlot slot : new ArrayList<>(f.getActiveSlots().values())) {
                slot.clearModel();
            }
        }
        if (stations.containsKey(f.getEntityId())) {
            CraftingStation station = stations.get(f.getEntityId());
            station.remove(e);
            if (e.isCancelled()) return;
            stations.remove(f.getEntityId());
        }
    }

    @EventHandler
    public void furniturePlace(FurniturePlaceEvent e) {
        Furniture f = e.getFurniture();
        if(FurnitureCache.isButterChurn(f)) {
            FurnitureSlot slot = f.getType().getSlot("stick");
            if(slot == null) return;
            slot.forceModel(new ItemStack(Material.STICK, 1));
            f.addActiveSlot(slot);
        }
    }

    private void playStickChurnAnimation(ItemDisplay display) {
        final Transformation start = display.getTransformation();

        // Target transformation: moved down by 0.5 blocks
        final Transformation target = new Transformation(
                new Vector3f(
                        start.getTranslation().x(),
                        start.getTranslation().y() - 0.2f,
                        start.getTranslation().z()
                ),
                start.getLeftRotation(),
                start.getScale(),
                start.getRightRotation()
        );

        new BukkitRunnable() {
            int tick = 0;
            final int duration = 6; // 1 second down, 1 second up

            @Override
            public void run() {
                if (display.isDead()) {
                    cancel();
                    return;
                }

                float t = tick / (float) duration;

                // First half (0 → 20): move down 0 → 1
                // Second half (20 → 40): move up 1 → 0
                if (tick > duration) {
                    t = 1f - (t - 1f);
                }

                // Interpolate translation only
                Vector3f a = start.getTranslation();
                Vector3f b = target.getTranslation();

                Vector3f interpolated = new Vector3f(
                        a.x() + (b.x() - a.x()) * t,
                        a.y() + (b.y() - a.y()) * t,
                        a.z() + (b.z() - a.z()) * t
                );

                // Apply frame
                Transformation frame = new Transformation(
                        interpolated,
                        start.getLeftRotation(),
                        start.getScale(),
                        start.getRightRotation()
                );

                display.setTransformation(frame);

                tick++;
                if (tick > duration * 2) cancel(); // full 2-second cycle
            }
        }.runTaskTimer(Cooking.plugin, 0L, 1L);
    }
}
