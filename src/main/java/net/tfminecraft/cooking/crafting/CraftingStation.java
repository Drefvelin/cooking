package net.tfminecraft.cooking.crafting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import me.Plugins.TLibs.TLibs;
import net.tfminecraft.cooking.item.FoodItem;
import net.tfminecraft.cooking.item.tag.TagTrack;
import net.tfminecraft.cooking.loader.TrackLoader;
import net.tfminecraft.cooking.utils.FoodParser;
import net.tfminecraft.cooking.utils.ItemBuilder;
import net.tfminecraft.events.FurnitureBreakEvent;
import net.tfminecraft.events.FurnitureInteractEvent;
import net.tfminecraft.events.FurnitureSlotItemAddEvent;
import net.tfminecraft.events.FurnitureSlotItemTakeEvent;
import net.tfminecraft.furniture.Furniture;
import net.tfminecraft.furniture.FurnitureSlot;

public class CraftingStation {
    private Furniture f;
    private String id;
    private String furniture;
    private boolean singleOrigin;
    private int maxSlots;
    private String tool;
    private CraftingRecipe currentRecipe;
    private List<CraftingRecipe> recipes = new ArrayList<>();
    private HashMap<String, ItemStack> slots = new HashMap<>();

    public CraftingStation(String key, ConfigurationSection config) {
        this.id = key;
        this.furniture = config.getString("furniture", "none");
        this.singleOrigin = config.getBoolean("single-origin", false);
        this.maxSlots = config.getInt("max-slots", 0);
        this.tool = config.getString("tool", "none");

        if(config.isConfigurationSection("recipes")) {
            for (String recipeKey : config.getConfigurationSection("recipes").getKeys(false)) {
                ConfigurationSection recipeSection = config.getConfigurationSection("recipes").getConfigurationSection(recipeKey);
                recipes.add(new CraftingRecipe(recipeKey, recipeSection));
            }
        }
    }

    public CraftingStation(Furniture f, CraftingStation station) {
        this.f = f;
        this.id = station.id;
        this.furniture = station.furniture;
        this.singleOrigin = station.singleOrigin;
        this.maxSlots = station.maxSlots;
        this.tool = station.tool;
        this.currentRecipe = station.currentRecipe;
        this.slots = new HashMap<>(station.slots);
        this.recipes = new ArrayList<>(station.recipes);
    }

    public Furniture getFurniture() {
        return f;
    }

    public String getId() {
        return id;
    }

    public String getBlockId() {
        return furniture;
    }

    public boolean isSingleOrigin() {
        return singleOrigin;
    }

    public int getMaxSlots() {
        return maxSlots;
    }

    public HashMap<String, ItemStack> getSlots() {
        return slots;
    }

    public boolean hasRecipe() {
        return currentRecipe != null;
    }

    public boolean hasFreeSlots() {
        int free = 0;
        for(FurnitureSlot fs : f.getType().getSlots().values()) {
            if(!slots.containsKey(fs.getId()) && fs.getId().contains("input")) {
                free++;
            }
        }
        return free != 0;
    }

    public boolean canAddItem(ItemStack i) {
        if(!hasFreeSlots()) return false;
        if(currentRecipe != null && !currentRecipe.canAdd(i)) return false;
        if(slots.size() >= maxSlots) return false;

        if(!singleOrigin) return true;

        FoodItem fi = FoodItem.fromItem(i);
        if(fi == null) return true;

        for(ItemStack stack : slots.values()) {
            FoodItem fs = FoodItem.fromItem(stack);
            if(fs != null && !fs.getOrigin().equalsIgnoreCase(fi.getOrigin())) {
                return false;
            }
        }
        return true;
    }

    public void add(ItemStack item, String slotId) {
        slots.put(slotId, item);

        if(currentRecipe == null) {
            for(CraftingRecipe cr : recipes) {
                if(cr.canAdd(item)) {
                    currentRecipe = cr;
                    break;
                }
            }
        }
    }

    public void addItem(FurnitureSlotItemAddEvent e) {
        ItemStack item = e.getItem();
        if(!canAddItem(item)) {
            e.setCancelled(true);
            return;
        }
        add(item, e.getSlot().getId());
        FoodItem fi = FoodItem.fromItem(item);
        if(fi != null) {
            e.setDisplayData(fi.getModelData().getDisplayData());
        }
    }

    public void removeItem(FurnitureSlotItemTakeEvent e) {
        slots.remove(e.getSlot().getId());
        if(slots.isEmpty()) {
            currentRecipe = null;
        }
    }

    public void craft() {
        craft(null);
    }

    public void craft(Player p) {
        if(currentRecipe == null) return;
        if(slots.isEmpty()) return;
        if(slots.size() < currentRecipe.getRatio()) return;

        String origin = "none";
        int age = 0;
        int ageables = 0;

        int outputAmount = slots.size() / currentRecipe.getRatio();
        int used = outputAmount * currentRecipe.getRatio();

        for(String slotId : new ArrayList<>(slots.keySet())) {
            if(used <= 0) break;

            FoodItem fi = FoodItem.fromItem(slots.get(slotId));
            if(fi != null) {
                TagTrack track = fi.getTagTrack("freshness");
                if(track != null) {
                    age += track.getValue();
                    ageables++;
                }
                if(isSingleOrigin()) origin = fi.getOrigin();
            }

            slots.remove(slotId);

            FurnitureSlot slot = f.getActiveSlot(slotId).orElse(null);
            if(slot != null) {
                slot.clearModel();
                f.removeActiveSlot(slotId);
            }

            used--;
        }

        int finalage = ageables == 0 ? -1 : age / ageables;

        String data = new String(currentRecipe.getOutput());
        if(isSingleOrigin() && !origin.equalsIgnoreCase("none")) {
            data = data.replace("{origin}", origin);
        }

        FoodItem item = FoodParser.parse(data).template;

        if(currentRecipe.isProcessed()) {
            item.addOrModifyTrack(TrackLoader.getByString("processed"));
        }

        if(finalage != -1) {
            TagTrack fresh = new TagTrack(TrackLoader.getByString("freshness"));
            fresh.setValue(finalage);
            item.addOrModifyTrack(fresh);
        }

        f.getLoc().getWorld().playSound(f.getLoc(), Sound.BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES, 1f, 1f);

        for(Map.Entry<String, ItemStack> entry : slots.entrySet()) {
            FurnitureSlot slot = f.getActiveSlot(entry.getKey()).orElse(null);
            ItemStack remaining = slot.getCurrentItem();
            if(remaining == null) continue;

            if(slot != null) slot.clearModel();

            f.getLoc().getWorld().dropItem(
                f.getLoc(),
                remaining
            ).setVelocity(new Vector(Math.random()*0.2-0.1, 0.1, Math.random()*0.2-0.1));
        }
        slots.clear();
        currentRecipe = null;

        ItemStack output = ItemBuilder.buildSingle(item, null);
        output.setAmount(outputAmount);

        boolean onBoard = false;
        for(FurnitureSlot slot : f.getType().getSlots().values()) {
            if(f.hasActiveSlot(slot.getId())) continue;
            if(!slot.getId().contains("input")) continue;
            slot.forceModel(output);
            f.addActiveSlot(slot);
            onBoard = true;
            slot.applyDisplayData(item.getModelData().getDisplayData());

            // Attempt to match new currentRecipe based on the output item
            CraftingRecipe newRecipe = null;
            for (CraftingRecipe cr : recipes) {
                if (cr.canAdd(output)) {
                    newRecipe = cr;
                    break;
                }
            }
            slots.put(slot.getId(), output);

            // Assign new recipe or clear it
            currentRecipe = newRecipe;
            break;
        }

        if(!onBoard) {
            f.getLoc().getWorld().dropItem(
                f.getLoc(),
                output
            ).setVelocity(new Vector(Math.random()*0.2-0.1, 0.1, Math.random()*0.2-0.1));
        }
    }

    public void remove(FurnitureBreakEvent e) {
        if(!e.hasPlayer()) return;

        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItemInMainHand();

        if(tool.equalsIgnoreCase("none")) return;

        if(TLibs.getItemAPI().getChecker().checkItemWithPath(item, tool)) {
            if(slots.isEmpty()) return;
            e.setCancelled(true);
            craft(p);
        }
    }

    public void interact(FurnitureInteractEvent e) {

    }
}
