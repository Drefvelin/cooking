package net.tfminecraft.cooking.cooking;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.Plugins.TLibs.TLibs;
import me.Plugins.TLibs.Objects.API.SubAPI.StringFormatter;
import net.tfminecraft.cooking.cache.ItemCache;
import net.tfminecraft.cooking.enums.Method;
import net.tfminecraft.cooking.item.FoodItem;
import net.tfminecraft.cooking.item.tag.TagTrack;
import net.tfminecraft.cooking.loader.TrackLoader;
import net.tfminecraft.cooking.utils.DisplayUtils;
import net.tfminecraft.cooking.utils.FoodParser;
import net.tfminecraft.cooking.utils.ItemBuilder;
import net.tfminecraft.events.FurnitureInteractEvent;
import net.tfminecraft.furniture.Furniture;
import net.tfminecraft.furniture.FurnitureSlot;

public class SauceReference extends CookingReference {

    public SauceReference(Furniture f, Method m) {
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
            if (method == Method.SAUCEPAN) {
                f.getLoc().getWorld().spawnParticle(
                        Particle.CAMPFIRE_COSY_SMOKE,
                        f.getLoc(),
                        0,
                        0, 0.1, 0,
                        0.05
                );
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

    public boolean canAdd(ItemStack i) {

        if (!secondaries.containsKey("liquid")) return false;
        if (i == null || i.getType() == Material.AIR) return false;

        // Convert item to FoodItem
        FoodItem fi = FoodItem.fromItem(i);
        if (fi == null) return false;

        String category = fi.getCategory().toLowerCase();

        // Garnish or spice logic
        if (category.equals("garnish") || category.equals("spice")) {
            return !full();
        }

        // Alcohol allowed for now
        if (category.equals("alcohol")) return true;

        // Unknown category
        return false;
    }

    public boolean full() {
        int count = 0;
        for (FoodItem item : slots.values()) {
            String cat = item.getCategory().toLowerCase();
            if (cat.equals("garnish") || cat.equals("spice")) {
                count++;
            }
        }
        return count >= 2;
    }

    public void scoop(Player p, ItemStack ladle) {
        if (!full()) return;

        // ---------- BASE STRING ----------
        String base = "sauce(type=sauce;origin=Mixed;quality=1-5;tags=processed.0:warmth.0)";

        // ---------- BUILD BASE ----------
        FoodItem sauce = FoodParser.parse(base).template;

        // ---------- COUNT INGREDIENT TYPES ----------
        int seasoningCount = 0;
        int flourCount = 0;
        boolean hasMilk = false;

        for (FoodItem fi : slots.values()) {
            String category = fi.getCategory().toLowerCase();

            if (category.equals("salt") || category.equals("pepper"))
                seasoningCount++;

            if (category.equals("flour"))
                flourCount++;
        }

        // Milk = creamy → if any colour is pure white
        for (String c : colours) {
            if (c.equalsIgnoreCase("ffffff")) {
                hasMilk = true;
                break;
            }
        }

        // ---------- SEASONING TRACK ----------
        if (seasoningCount == 1) {
            TagTrack t = new TagTrack(TrackLoader.getByString("seasoning"));
            t.setValue(0); // seasoned
            sauce.addOrModifyTrack(t);
        } else if (seasoningCount >= 2) {
            TagTrack t = new TagTrack(TrackLoader.getByString("seasoning"));
            t.setValue(1); // well_seasoned
            sauce.addOrModifyTrack(t);
        }

        // ---------- THICKNESS TRACK ----------
        TagTrack thickTrack = new TagTrack(TrackLoader.getByString("sauce_thickness"));
        if (flourCount >= 1) {
            thickTrack.setValue(1); // thick
        } else {
            thickTrack.setValue(0); // thin
        }
        sauce.addOrModifyTrack(thickTrack);

        // ---------- CREAMINESS TRACK ----------
        if (hasMilk) {
            TagTrack creamyTrack = new TagTrack(TrackLoader.getByString("sauce_creamy"));
            creamyTrack.setValue(0); // creamy only has value 0
            sauce.addOrModifyTrack(creamyTrack);
        }

        // ---------- COOKING STAGE TRACK ----------
        TagTrack cookedTrack = new TagTrack(TrackLoader.getByString("sauce_cooked"));
        cookedTrack.setValue(1); // 1 = simmered (your choice)
        sauce.addOrModifyTrack(cookedTrack);

        // ---------- BUILD RESULT ----------
        ItemStack output = ItemBuilder.buildSingle(sauce, ladle);

        String displayName = sauce.getName();
        displayName = displayName.replace("{colour}", DisplayUtils.getMergedColour(colours));
        displayName = displayName.replace("{ingredients}", getName("Sauce"));
        ItemMeta m = output.getItemMeta();
        m.setDisplayName(StringFormatter.formatHex(displayName));
        output.setItemMeta(m);

        // ---------- GIVE TO PLAYER ----------
        p.getInventory().setItemInMainHand(output);
        p.swingMainHand();
        f.getLoc().getWorld().playSound(f.getLoc(), Sound.ITEM_BUCKET_FILL, 1f, 2f); //TODO SOUND
        // ---------- CLEAR ----------
        clear();
    }


    @Override
    public void interact(FurnitureInteractEvent e) {
        Player p = e.getPlayer();
        if(isEmpty() && p.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
            return;
        }
        ItemStack item = p.getInventory().getItemInMainHand();
        if(ItemCache.isLadle(item)) {
            scoop(p, item);
        }
        if(ItemCache.isLiquid(item) && !secondaries.containsKey("liquid")) {
            FurnitureSlot slot = f.getType().getSlot("liquid");
            if(slot == null) return;
            f.addActiveSlot(slot);
            secondaries.put("liquid", -1);
            slot.forceModel(TLibs.getItemAPI().getCreator().getItemFromPath(ItemCache.getLiquidModel(item)));
            addColour(ItemCache.getColour(item));
            p.swingMainHand();
            updateModel();
            danger = 0;
            f.getLoc().getWorld().playSound(f.getLoc(), Sound.ITEM_BUCKET_FILL, 1f, 2f); //TODO SOUND
        }
        if(canAdd(item)) {
            for(String slot : f.getType().getSlots().keySet()) {
                if(add(slot, item)) {
                    updateModel();
                    p.swingMainHand();
                    break;
                }
            }
        }
    }
    
    public void updateModel() {
        int newModel = getModel();
        FurnitureSlot slot = f.getType().getSlot("liquid");
        if(slot == null) return;
        ItemStack item = slot.getCurrentItem();
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(newModel);
        item.setItemMeta(meta);
        slot.forceModel(item);
    }
}
