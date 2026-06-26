package net.tfminecraft.cooking.cooking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

import net.tfminecraft.cooking.enums.Method;
import net.tfminecraft.cooking.enums.Tag;
import net.tfminecraft.cooking.item.FoodItem;
import net.tfminecraft.cooking.item.data.CookData;
import net.tfminecraft.cooking.item.tag.TagTrack;
import net.tfminecraft.cooking.loader.TrackLoader;
import net.tfminecraft.cooking.utils.DisplayUtils;
import net.tfminecraft.cooking.utils.ItemUpdater;
import net.tfminecraft.events.FurnitureInteractEvent;
import net.tfminecraft.events.FurnitureSlotItemAddEvent;
import net.tfminecraft.events.FurnitureSlotItemTakeEvent;
import net.tfminecraft.furniture.Furniture;
import net.tfminecraft.furniture.FurnitureSlot;
import net.tfminecraft.furniture.data.DisplayData;
import net.tfminecraft.cooking.cache.CategoryDictionary;
import net.tfminecraft.cooking.cache.ItemCache;

public class CookingReference {
    protected Method method;
    protected Furniture f;
    protected Map<String, FoodItem> slots = new HashMap<>();
    protected Map<String, Integer> secondaries = new HashMap<>();
    protected List<String> colours = new ArrayList<>();
    protected int danger = 0;

    public CookingReference(Furniture f, Method m) {
        this.method = m;
        this.f = f;
    }

    //SETTERS AND GETTERS

    public Method getMethod() {
        return method;
    }

    public Furniture getFurniture() {
        return f;
    }

    public Map<String, FoodItem> getSlots() {
        return slots;
    }

    public FoodItem getSlot(String key) {
        return slots.get(key);
    }

    public Map<String, Integer> getSecondaries() {
        return secondaries;
    }

    public Integer getSecondary(String key) {
        return secondaries.get(key);
    }

    //more complicated shit
    public boolean isEmpty() {
        return slots.isEmpty() && secondaries.isEmpty();
    }

    public void remove() {
        for (Map.Entry<String, Integer> entry : secondaries.entrySet()) {
            removeSecondary(entry);
        }
        secondaries.clear();
    }


    public void removeSecondary(Map.Entry<String, Integer> entry) {
        String key = entry.getKey();
        if(!f.hasActiveSlot(key)) return;
        f.getActiveSlot(key).get().clearModel();
    }

    public void tick() {
        handleSecondaries();
    }

    protected void applySlotUpdate(String slot, FoodItem item) {
        if (!f.hasActiveSlot(slot)) return;
        ItemStack stack = f.getActiveSlot(slot).get().getCurrentItem();
        stack = ItemUpdater.applyItemUpdate(stack, item, f.getId());
        if (stack == null) return;
        f.getActiveSlot(slot).get().forceModel(stack);
        FoodItem updated = FoodItem.fromItem(stack);
        if(updated != null) {
            DisplayData display = updated.getModelData().getDisplayData();
            f.getActiveSlot(slot).get().applyDisplayData(display);
        }
    }


    private void handleSecondaries() {
        for (Iterator<Map.Entry<String, Integer>> it = secondaries.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, Integer> entry = it.next();
            if (entry.getValue() == -1) continue;
            int v = entry.getValue() - 1;
            entry.setValue(v);
            if (v == 0) {
                removeSecondary(entry);
                it.remove();
            }
        }
    }

    public boolean add(String slot, ItemStack item) {
        if(f.hasActiveSlot(slot)) return false;
        if(!slot.contains("input")) return false;
        FoodItem fi = FoodItem.fromItem(item);
        if(fi == null) return false;
        slots.put(slot, fi);
        FurnitureSlot fslot = f.getType().getSlot(slot);
        if(fslot == null) return false;
        ItemStack model = new ItemStack(item);
        model = ItemUpdater.applyItemUpdate(model, fi, f.getId());
        model.setAmount(1);
        fslot.forceModel(model);
        f.addActiveSlot(fslot);
        addColour(ItemCache.getColour(item));
        f.getLoc().getWorld().playSound(f.getLoc(), Sound.ITEM_BUCKET_FILL, 1f, 1f); //TODO SOUND
        item.setAmount(item.getAmount()-1);
        return true;
    }

    public boolean hasSlot(String key) {
        for(FoodItem item : slots.values()) {
            if(item.getCategory().equals(key)) return true;
        }
        return false;
    }

    public void interact(FurnitureInteractEvent e) { }

    public void slotAdd(FurnitureSlotItemAddEvent e) {
        ItemStack item = e.getItem();
        FoodItem fi = FoodItem.fromItem(item);
        if(!fi.canBeCooked()) {
            e.setCancelled(true);
            return;
        }
        CookData data = fi.getCookData();
        if(!data.hasMethod(method)) {
            e.setCancelled(true);
            return;
        }
        if(!fi.hasTag(Tag.RAW)) {
            e.setCancelled(true);
            return;
        }
        fi.getCookData().start(method);
        e.setDisplayData(fi.getModelData().getDisplayData());
        slots.put(e.getSlot().getId(), fi);
        f.getLoc().getWorld().playSound(f.getLoc(), Sound.BLOCK_LAVA_EXTINGUISH, 1f, 1f); //TODO SOUND
    }

    public void slotRemove(FurnitureSlotItemTakeEvent e) {
        FoodItem fi = slots.remove(e.getSlot().getId());
        ItemStack item = e.getItem();
        if(fi != null) {
            if(!fi.canBeCooked()) {
                e.setCancelled(true);
                return;
            }
            CookData data = fi.getCookData();
            if(!data.hasMethod(method)) {
                e.setCancelled(true);
                return;
            }
            if(data.getCurrentTime() < 5) return;
            TagTrack hot = TrackLoader.getByString("warmth");
            if(hot == null) return;
            fi.addOrModifyTrack(hot);
            item = ItemUpdater.applyItemUpdate(item, fi, f.getId());
            if(item == null) return;
            e.setItem(item);
        }
    }

    //clear
    public void clear() {
        for(Map.Entry<String, FoodItem> entry : slots.entrySet()) {
            String slot = entry.getKey();
            if(!f.hasActiveSlot(slot)) continue;
            f.getActiveSlot(slot).get().clearModel();
        }
        for (Map.Entry<String, Integer> entry : secondaries.entrySet()) {
            removeSecondary(entry);
        }
        secondaries.clear();
        slots.clear();
        colours.clear();
        danger = 0;
    }

    //Colours
    public List<String> getColours() {
        return colours;
    }

    public String getLiquidItemPath() {
        return CategoryDictionary.getSauceItemPath(DisplayUtils.getMergedColour(colours), 0);
    }

    public void addColour(String hex) {
        if(!colours.contains(hex)) {
            colours.add(hex);
        }
    }

    public void removeColour(String hex) {
        colours.remove(hex);
    }

    //Ingredients
    //Ingredients
    public List<String> getIngredients() {
        List<String> important = new ArrayList<>();
        List<String> unimportant = new ArrayList<>();

        for (FoodItem fi : slots.values()) {
            if (fi == null) continue;

            String origin = fi.getOrigin();
            if (origin == null) continue;

            origin = origin.trim();
            if (origin.equalsIgnoreCase("none")) continue;
            if (origin.equalsIgnoreCase("mixed")) continue;

            String category = fi.getCategory().toLowerCase();

            boolean isUnimportant = CategoryDictionary.UNIMPORTANT_CATEGORIES.contains(category);

            // Add unique only
            if (isUnimportant) {
                if (!unimportant.contains(origin)) unimportant.add(origin);
            } else {
                if (!important.contains(origin)) important.add(origin);
            }
        }

        // Combine: important first, unimportant last
        List<String> result = new ArrayList<>(important);
        result.addAll(unimportant);

        return result;
    }

    public String getName(String type) {
        List<String> ingredients = getIngredients();

        if (ingredients.isEmpty()) {
            return "Mixed " + type;
        }

        if (ingredients.size() == 1) {
            return ingredients.get(0) + " " + type;
        }

        if (ingredients.size() == 2) {
            return ingredients.get(0) + " and " + ingredients.get(1) + " " + type;
        }

        // 3 or more
        String first = ingredients.get(0);
        String second = ingredients.get(1);
        String last = ingredients.get(2); // only take first 3 max

        return first + ", " + second + " and " + last + " " + type;
    }
}
