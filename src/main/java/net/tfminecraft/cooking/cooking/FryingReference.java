package net.tfminecraft.cooking.cooking;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.Plugins.TLibs.TLibs;
import net.tfminecraft.cooking.cache.ItemCache;
import net.tfminecraft.cooking.enums.Method;
import net.tfminecraft.cooking.item.FoodItem;
import net.tfminecraft.events.FurnitureInteractEvent;
import net.tfminecraft.furniture.Furniture;
import net.tfminecraft.furniture.FurnitureSlot;

public class FryingReference extends CookingReference {

    public FryingReference(Furniture f, Method m) {
        super(f, m);
    }

    @Override
    public void tick() {
        super.tick();
        handleCookingSlots();
        handleParticlesAndDanger();
    }
    
    private void handleParticlesAndDanger() {
        if (!slots.isEmpty() || !secondaries.isEmpty()) {
            if (method == Method.FRYING_PAN) {
                f.getLoc().getWorld().spawnParticle(
                        Particle.CAMPFIRE_COSY_SMOKE,
                        f.getLoc(),
                        0,
                        0, 0.1, 0,
                        0.05
                );
                if (!secondaries.containsKey("butter")) danger++;
                if (danger >= 10) burnAllSlots();
            }
        }
    }

    private void handleCookingSlots() {
        for (Map.Entry<String, FoodItem> entry : slots.entrySet()) {
            String slot = entry.getKey();
            FoodItem item = entry.getValue();
            if (!item.canBeCooked()) continue;
            if (!item.getCookData().tick()) continue;
            applySlotUpdate(slot, item);
        }
    }

    private void burnAllSlots() {
        for (Map.Entry<String, FoodItem> entry : slots.entrySet()) {
            String slot = entry.getKey();
            FoodItem item = entry.getValue();
            if (!item.canBeCooked()) continue;
            item.getCookData().setCurrentTime(item.getCookData().getParameters().get(method).getBurnTime());
            super.applySlotUpdate(slot, item);
        }
    }

    @Override
    public void interact(FurnitureInteractEvent e) {
        Player p = e.getPlayer();
        if(isEmpty() && p.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
            return;
        }
        ItemStack item = p.getInventory().getItemInMainHand();
        if(ItemCache.isButter(item) && !secondaries.containsKey("butter")) {
            FurnitureSlot slot = f.getType().getSlot("butter");
            if(slot == null) return;
            f.addActiveSlot(slot);
            secondaries.put("butter", 30);
            slot.forceModel(TLibs.getItemAPI().getCreator().getItemFromPath(ItemCache.butterModel));
            p.swingMainHand();
            danger = 0;
            f.getLoc().getWorld().playSound(f.getLoc(), Sound.BLOCK_LAVA_EXTINGUISH, 1f, 1f); //TODO SOUND
        }
    }
}
