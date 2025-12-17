package net.tfminecraft.cooking.cooking;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import me.Plugins.TLibs.TLibs;
import me.Plugins.TLibs.Objects.API.SubAPI.StringFormatter;
import net.tfminecraft.cooking.Cooking;
import net.tfminecraft.cooking.cache.ItemCache;
import net.tfminecraft.cooking.enums.Method;
import net.tfminecraft.cooking.enums.Tag;
import net.tfminecraft.cooking.item.FoodItem;
import net.tfminecraft.cooking.item.data.CookData;
import net.tfminecraft.cooking.item.tag.TagTrack;
import net.tfminecraft.cooking.loader.TrackLoader;
import net.tfminecraft.cooking.utils.DisplayUtils;
import net.tfminecraft.cooking.utils.Encoder;
import net.tfminecraft.cooking.utils.FoodParser;
import net.tfminecraft.cooking.utils.InventoryAdder;
import net.tfminecraft.cooking.utils.ItemBuilder;
import net.tfminecraft.cooking.utils.ItemUpdater;
import net.tfminecraft.cooking.utils.Keys;
import net.tfminecraft.events.FurnitureInteractEvent;
import net.tfminecraft.furniture.Furniture;
import net.tfminecraft.furniture.FurnitureSlot;
import net.tfminecraft.furniture.data.DisplayData;

public class PotReference extends CookingReference {
    private int temperature = 0;          // 0–20
    private final int MAX_TEMPERATURE = 20;

    public PotReference(Furniture f, Method m) {
        super(f, m);
    }

    @Override
    public void tick() {
        super.tick();
        // Heat water over time
        if (secondaries.containsKey("liquid")) {
            if (temperature < MAX_TEMPERATURE) {
                temperature++;
            }
        }

        handleCookingSlots();

        // Particle task
        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                handleParticles();
                i++;
                if (i == 10) this.cancel();
            }
        }.runTaskTimer(Cooking.plugin, 0L, 2L);
    }

    public FoodItem getMain() {
        for(FurnitureSlot slot : f.getActiveSlots().values()) {
            ItemStack stack = slot.getCurrentItem();
            if(stack == null) continue;
            FoodItem item = FoodItem.fromItem(stack);
            if(item == null) continue;
            return item;
        }
        return null;
    }

    public void setMain(FoodItem fi) {
        for(FurnitureSlot slot : f.getActiveSlots().values()) {
            ItemStack stack = slot.getCurrentItem();
            if(stack == null) continue;
            FoodItem item = FoodItem.fromItem(stack);
            if(item == null) continue;
            slot.forceModel(ItemUpdater.applyItemUpdate(stack, fi, f.getId()));
        }
    }
    
    private void handleParticles() {
        if (!slots.isEmpty() || !secondaries.isEmpty()) {
            if (method == Method.POT) {
                if(f.getActiveSlot("liquid").isEmpty()) return;
                Entity display = Bukkit.getEntity(f.getActiveSlot("liquid").get().getDisplayStandId());
                if (display == null) return;

                Location loc = display.getLocation();

                // Temperature-based bubbling chance
                double boilChance = (double) temperature / MAX_TEMPERATURE;
                if (Math.random() > boilChance) return;

                // Generate 3–7 bubble bursts
                int bursts = 3 + (int)(Math.random() * 5);

                for (int i = 0; i < bursts; i++) {

                    double x = (Math.random() * 0.4) - 0.2;
                    double z = (Math.random() * 0.4) - 0.2;

                    Location p = loc.clone().add(x, -0.2, z);

                    loc.getWorld().spawnParticle(
                        Particle.BUBBLE_POP,
                        p,
                        1,
                        0, 0.05, 0,
                        0.05
                    );

                    if (Math.random() < 0.25) {
                        loc.getWorld().spawnParticle(
                            Particle.SPLASH,
                            p.clone().add(0, 0.05, 0),
                            2,
                            0.02, 0.02, 0.02,
                            0.02
                        );
                    }
                }

                // (keep your booming sound variants)
                double soundRoll = Math.random();
                if (soundRoll < 0.5) {
                    loc.getWorld().playSound(loc, Sound.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, 0.4f, 1.2f);
                } else if (soundRoll < 0.7) {
                    loc.getWorld().playSound(loc, Sound.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, 0.6f, 1.1f);
                } else if (soundRoll < 0.75) {
                    loc.getWorld().playSound(loc, Sound.BLOCK_LAVA_POP, 0.3f, 2.2f);
                }
            }
        }
    }

    public boolean isSoup() {
        for(FoodItem item : slots.values()) {
            if(item.hasTag(Tag.MASHED)) return true;
        }
        return false;
    }

    public boolean isBoiling() {
        return temperature >= MAX_TEMPERATURE;
    }

    public void scoop(Player p, ItemStack ladle) {
        if (!isSoup()) return;

        // ---------- BASE STRING ----------
        String base = "soup(type=soup;origin=Mixed;quality=1-5;tags=processed.0:warmth.0)";

        // ---------- BUILD BASE ----------
        FoodItem soup = FoodParser.parse(base).template;

        // ---------- COUNT INGREDIENT TYPES ----------
        int seasoningCount = 0;

        for (FoodItem fi : slots.values()) {
            String category = fi.getCategory().toLowerCase();

            if (category.equals("salt") || category.equals("pepper"))
                seasoningCount++;
        }

        // ---------- SEASONING TRACK ----------
        if (seasoningCount == 1) {
            TagTrack t = new TagTrack(TrackLoader.getByString("seasoning"));
            t.setValue(0); // seasoned
            soup.addOrModifyTrack(t);
        } else if (seasoningCount >= 2) {
            TagTrack t = new TagTrack(TrackLoader.getByString("seasoning"));
            t.setValue(1); // well_seasoned
            soup.addOrModifyTrack(t);
        }
        FoodItem mi = getMain();
        if(mi != null && mi.hasTagTrack("soup_thickness")) {
            soup.addOrModifyTrack(mi.getTagTrack("soup_thickness"));
            setMain(mi);
        }

        // ---------- BUILD RESULT ----------
        ItemStack output = ItemBuilder.buildSingle(soup, ladle);

        String displayName = soup.getName();
        displayName = displayName.replace("{colour}", DisplayUtils.getMergedColour(colours));
        displayName = displayName.replace("{ingredients}", getName("Soup"));
        ItemMeta m = output.getItemMeta();
        m.setDisplayName(StringFormatter.formatHex(displayName));
        m.getPersistentDataContainer().set(Keys.SLOT_DATA, PersistentDataType.STRING, Encoder.getEncodedSlots(f));
        output.setItemMeta(m);

        // ---------- GIVE TO PLAYER ----------
        p.getInventory().setItemInMainHand(output);
        p.swingMainHand();
        f.getLoc().getWorld().playSound(f.getLoc(), Sound.ITEM_BUCKET_FILL, 1f, 1f); //TODO SOUND
        // ---------- CLEAR ----------
        clear();
    }

    private void handleCookingSlots() {
        FoodItem main = getMain();
        if(!isEmpty() && isSoup() && main != null) {
            TagTrack track = main.getTagTrack("soup_thickness");
            Bukkit.getPlayer("drefvelin").sendMessage("adding time");
            if(track != null) {
                Bukkit.getPlayer("drefvelin").sendMessage("time "+track.getValue());
                track.setValue(track.getValue()+1);
            } else {
                main.addOrModifyTrack(new TagTrack(TrackLoader.getByString("soup_thickness")));
            }
            setMain(main);
        }
        for (Map.Entry<String, FoodItem> entry : slots.entrySet()) {
            String slot = entry.getKey();
            FoodItem item = entry.getValue();
            CookData data = item.getCookData();
            if (!item.canBeCooked()) continue;
            if (isSoup() && data.isBeingCooked()) {
                data.stop();
                continue;
            }
            if (!data.isBeingCooked()) {
                data.start(Method.POT);
            }
            boolean progressed = data.tick();
            if (!progressed) continue;
            applySlotUpdate(slot, item);
        }
    }

    public boolean canAdd(Player p, ItemStack i) {
        if(!isBoiling()) {
            p.sendMessage("§cWater isn't boiling yet...");
            return false;
        }
        if (!secondaries.containsKey("liquid")) return false;
        if(!slots.isEmpty() && !isSoup()) return false;
        if (i == null || i.getType() == Material.AIR) return false;

        // Convert item to FoodItem
        FoodItem fi = FoodItem.fromItem(i);
        if (fi == null) return false;

        String cat = fi.getCategory();
        if((cat.equalsIgnoreCase("spice") && !hasSlot("spice")) 
            || (cat.equalsIgnoreCase("salt") && !hasSlot("salt")) 
            || (cat.equalsIgnoreCase("pepper") && !hasSlot("pepper"))) return true;

        if(!fi.canBeCooked()) return false;
        
        CookData data = fi.getCookData();
        if(data.hasMethod(method)) return true;

        // Unknown category
        return false;
    }

    public void take(Player p) {
        if(isSoup()) return;
        for(String key : slots.keySet()) {
            if(!f.hasActiveSlot(key)) continue;
            FurnitureSlot slot = f.getActiveSlot(key).get();
            if(slot.getCurrentItem() == null) continue;
            ItemStack stack = slot.getCurrentItem();
            stack = ItemUpdater.applyItemUpdate(stack, slots.get(key), null);
            p.getInventory().setItemInMainHand(stack);
            f.getLoc().getWorld().playSound(f.getLoc(), Sound.ITEM_BUCKET_FILL, 1f, 1f); //TODO SOUND
            slot.clearModel();
            slots.clear();
            break;
        }
    }

    public void mash(Player p) {
        boolean found = false;
        for(Map.Entry<String, FoodItem> entry : slots.entrySet()) {
            FoodItem item = entry.getValue();
            if(!item.getCategory().equalsIgnoreCase("vegetable")) continue;
            item.addOrModifyTrack(new TagTrack(TrackLoader.getByString("mashed")));
            updateModel();
            DisplayData mashed = new DisplayData();
            mashed.setxScale(0);
            mashed.setyScale(0);
            mashed.setzScale(0);
            mashed.setyPos(-0.4f);
            FurnitureSlot slot = f.getActiveSlot(entry.getKey()).get();
            if(slot == null) continue;
            slot.applyDisplayData(mashed);
            found = true;
        }
        if(found) {
            p.swingMainHand();
            f.getLoc().getWorld().playSound(f.getLoc(), Sound.ITEM_BUCKET_FILL, 1f, 1f); //TODO SOUND
        }
    }

    @Override
    public void interact(FurnitureInteractEvent e) {
        Player p = e.getPlayer();
        if(p.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
            if(isEmpty()) return;
            take(p);
            return;
        }
        ItemStack item = p.getInventory().getItemInMainHand();
        if(ItemCache.isLadle(item)) {
            scoop(p, item);
            return;
        }
        if(ItemCache.isMasher(item)) {
            mash(p);
            return;
        }
        if(ItemCache.isWater(item) && !secondaries.containsKey("liquid")) {
            FurnitureSlot slot = f.getType().getSlot("liquid");
            if(slot == null) return;
            f.addActiveSlot(slot);
            secondaries.put("liquid", -1);
            slot.forceModel(TLibs.getItemAPI().getCreator().getItemFromPath(ItemCache.getLiquidModel(item)));
            addColour(ItemCache.getColour(item));
            p.swingMainHand();
            updateModel();
            danger = 0;
            temperature = 0;
            f.getLoc().getWorld().playSound(f.getLoc(), Sound.ITEM_BUCKET_FILL, 1f, 1f); //TODO SOUND
            return;
        }
        if(canAdd(p, item)) {
            for(String slot : f.getType().getSlots().keySet()) {
                if(add(slot, item)) {
                    updateModel();
                    p.swingMainHand();
                    FoodItem main = getMain();
                    if(main != null && slots.size() > 1) {
                        TagTrack track = main.getTagTrack("soup_thickness");
                        if(track != null) {
                            track.setValue(track.getValue()+600);
                        }
                        setMain(main);
                    }
                    break;
                }
            }
        }
    }
    
    public void updateModel() {
        if(!isSoup()) return;
        int newModel = getModel();
        FurnitureSlot slot = f.getType().getSlot("liquid");
        if(slot == null) return;
        ItemStack item = slot.getCurrentItem();
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(newModel);
        item.setItemMeta(meta);
        slot.forceModel(item);
    }
    
    @Override
    public void clear() {
        if(isSoup()) super.clear();
        else super.remove();
    }
}
