package net.tfminecraft.cooking.cooking;



import java.util.ArrayList;
import java.util.List;
import java.util.Map;



import org.bukkit.Material;

import org.bukkit.Particle;

import org.bukkit.Sound;

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

import net.tfminecraft.cooking.quality.CompositionContext;

import net.tfminecraft.cooking.quality.CompositionFreshnessApplier;

import net.tfminecraft.cooking.quality.CompositionQualityResolver;

import net.tfminecraft.cooking.quality.CompositionResult;

import net.tfminecraft.cooking.utils.DisplayUtils;

import net.tfminecraft.cooking.utils.FoodParser;

import net.tfminecraft.cooking.utils.ItemBuilder;

import net.tfminecraft.cooking.utils.StationAddonRules;

import net.tfminecraft.events.FurnitureInteractEvent;

import net.tfminecraft.furniture.Furniture;

import net.tfminecraft.furniture.PlacedSlot;



public class SauceReference extends CookingReference {

    private FoodItem liquidSource;

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



    public boolean canAdd(Player player, ItemStack i) {

        if (!secondaries.containsKey("liquid")) return false;

        if (i == null || i.getType() == Material.AIR) return false;



        FoodItem fi = FoodItem.fromItem(i);

        if (fi == null) return false;



        String category = fi.getCategory().toLowerCase();



        if (StationAddonRules.isSeasoningCategory(category)) {

            return !hasSlot(category);

        }



        if (StationAddonRules.isSweetenerCategory(category)) {

            return StationAddonRules.canAcceptSweetener(slots, fi, player);

        }



        if (StationAddonRules.isAddonCategory(category)) {

            return StationAddonRules.canAcceptAddon(slots, fi, player);

        }



        if (category.equals("alcohol")) return true;



        return false;

    }



    public void scoop(Player p, ItemStack ladle) {

        if (!secondaries.containsKey("liquid")) return;



        String base = "sauce(type=sauce;origin=Mixed;tags=processed.0:warmth.0)";

        FoodItem sauce = FoodParser.parse(base).template;



        int flourCount = 0;

        boolean hasMilk = false;



        for (FoodItem fi : slots.values()) {

            String category = fi.getCategory().toLowerCase();

            if (category.equals("flour")) {

                flourCount++;

            }

        }



        for (String c : colours) {

            if (c.equalsIgnoreCase("ffffff")) {

                hasMilk = true;

                break;

            }

        }



        StationAddonRules.applySeasoningTag(sauce, slots);

        StationAddonRules.applyAddonTags(sauce, slots);

        StationAddonRules.applySweetTag(sauce, slots);



        TagTrack thickTrack = new TagTrack(TrackLoader.getByString("sauce_thickness"));

        if (flourCount >= 1) {

            thickTrack.setValue(1);

        } else {

            thickTrack.setValue(0);

        }

        sauce.addOrModifyTrack(thickTrack);



        if (hasMilk) {

            TagTrack creamyTrack = new TagTrack(TrackLoader.getByString("sauce_creamy"));

            creamyTrack.setValue(0);

            sauce.addOrModifyTrack(creamyTrack);

        }



        TagTrack cookedTrack = new TagTrack(TrackLoader.getByString("sauce_cooked"));

        cookedTrack.setValue(1);

        sauce.addOrModifyTrack(cookedTrack);



        List<FoodItem> inputs = new ArrayList<>();
        if (liquidSource != null) {
            inputs.add(liquidSource);
        }
        inputs.addAll(slots.values());

        CompositionResult composed = CompositionQualityResolver.compose(p, inputs, CompositionContext.SAUCE_SCOOP);
        CompositionFreshnessApplier.applyTracks(sauce, composed.getFreshnessTracks());
        int quality = composed.getFinalQuality();

        ItemStack output = ItemBuilder.buildSingleWithQuality(sauce, ladle, quality);



        String displayName = applyNameTemplate(sauce, DisplayUtils.getMergedColour(colours), "Sauce");

        ItemMeta m = output.getItemMeta();

        m.setDisplayName(StringFormatter.formatHex(displayName));

        output.setItemMeta(m);



        p.getInventory().setItemInMainHand(output);

        p.swingMainHand();

        f.getLoc().getWorld().playSound(f.getLoc(), Sound.ITEM_BUCKET_FILL, 1f, 2f);

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

            if (f.getType() == null || f.getType().getSlot("liquid") == null) return;

            FoodItem poured = FoodItem.fromItem(item);
            if (poured != null) {
                liquidSource = new FoodItem(poured);
            }

            PlacedSlot slot = f.getOrCreatePlacedSlot("liquid");

            secondaries.put("liquid", -1);

            slot.forceModel(TLibs.getItemAPI().getCreator().getItemFromPath(ItemCache.getLiquidModel(item)));

            addColour(ItemCache.getColour(item));

            p.swingMainHand();

            updateModel();

            danger = 0;

            f.getLoc().getWorld().playSound(f.getLoc(), Sound.ITEM_BUCKET_FILL, 1f, 2f);

        }

        if(canAdd(p, item)) {

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

        String path = getLiquidItemPath();

        if (f.getType() == null || f.getType().getSlot("liquid") == null) return;

        f.getOrCreatePlacedSlot("liquid").forceModel(TLibs.getItemAPI().getCreator().getItemFromPath(path));

    }

    @Override
    public void clear() {
        liquidSource = null;
        super.clear();
    }

}


