package net.tfminecraft.cooking.utils;

import net.tfminecraft.cooking.item.FoodItem;
import net.tfminecraft.cooking.loader.ModelLoader;
import net.tfminecraft.cooking.item.model.FoodModel;
import net.tfminecraft.cooking.item.model.ModelData;
import net.tfminecraft.cooking.item.tag.TagStep;
import net.tfminecraft.cooking.item.tag.TagTrack;
import net.tfminecraft.cooking.item.data.OverrideData;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import net.tfminecraft.cooking.cache.CategoryDictionary;
import net.tfminecraft.cooking.enums.Tag;

import me.Plugins.TLibs.Objects.API.SubAPI.StringFormatter;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ItemBuilder {

    public static List<ItemStack> build(FoodItem template, boolean unique, ItemStack base) {
        List<ItemStack> list = new ArrayList<>();
        int amount = template.getAmount();

        if (unique) {
            for (int i = 0; i < amount; i++)
                list.add(buildSingle(template, base));
            return list;
        }

        ItemStack stack = buildSingle(template, base);
        stack.setAmount(amount);
        list.add(stack);

        return list;
    }

    public static ItemStack buildSingle(FoodItem template, ItemStack base) {

        FoodItem item = new FoodItem(template);
        Map<String, Integer> indexMap = new HashMap<>();

        int q = ThreadLocalRandom.current().nextInt(item._parsedQualMin, item._parsedQualMax + 1);
        item.setQualityRange(q, q);

        item.setAmount(1);

        String origin = item.getOrigin();
        String displayName = item.getName();

        if (origin != null) {
            OverrideData od = item.getOverrides().get(origin.toUpperCase());
            if (od != null) {
                if (od.getName() != null) displayName = od.getName();
            }
        }
        if(displayName.contains("{inherit}") && base != null) displayName = displayName.replace("{inherit}", StringFormatter.getName(base));
        else if(displayName.contains("{inherit}")) displayName = displayName.replace("{inherit}", WordUtils.capitalize(origin != null ? origin : "unknown"));
        
        if(item.getModel() == null) item.setModel(new FoodModel(base));
        ModelData model = item.getModelData();

        ItemStack stack = model.apply(null, new ItemStack(Material.DIRT));
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(displayName);

        List<String> lore = new ArrayList<>();
        lore.add(CategoryDictionary.getName(item.getCategory()));

        // ORIGIN
        if(!template.hasTag(Tag.PROCESSED)) lore.add("§7Origin: " + (origin != null ? origin : "§fNone"));

        // QUALITY
        lore.add(buildStars(item.getQualityMin(), 5));

        // NUTRITION
        lore.add(StringFormatter.formatHex("#d4ad77Nutrition §f" + item.getFinalNutrition()));
        indexMap.put("nutrition", lore.size() - 1);

        // FOOD
        lore.add(StringFormatter.formatHex("#d4ad77Food §f" + item.getFinalFood()));
        indexMap.put("food", lore.size() - 1);

        if (!item.getIngredients().isEmpty()) {
            lore.add(StringFormatter.formatHex("#dbb072Ingredients:"));

            StringBuilder line = new StringBuilder("§7");
            int max = 20; // max characters per line **after formatting codes**

            for (String ing : item.getIngredients()) {
                // Add ingredient + comma + space
                String part = ing + ", ";

                // If line would exceed max length → push current line to lore
                if (line.length() + part.length() > max + 4) { // +4 accounts for "§7- "
                    lore.add(line.toString());
                    line = new StringBuilder("§7"); // new line but indented
                }

                line.append(part);
            }

            // Add final remaining line
            if (!line.toString().trim().equals("§7-"))
                lore.add(line.toString().replaceAll(", $", "")); // remove trailing comma
        }

        // SAUCE BLOCK (separate from ingredients)
        if (item.hasSauce()) {
            lore.add(" "); // spacer
            FoodItem sauce = item.getSauce();
            String sauceName = item.hasSauceName()
                    ? item.getSauceName()
                    : "Sauce";

            double sFood = sauce.getFinalFood();
            double sNut = sauce.getFinalNutrition();

            String stats = DisplayUtils.getSauceStatString(sFood, sNut);
            lore.add("§6" + sauceName + " §8(" + stats + "§8)");
            indexMap.put("sauce", lore.size() - 1);
        }



        lore.add(" ");
        boolean first = true;

        // TAG TRACKS
        for (TagTrack t : item.getTagTracks()) {
            TagStep step = t.getCurrentStep();
            String display = (step != null ? DisplayUtils.getDisplayString(step.getName(), step.getFoodMultiplier(), step.getNutritionMultiplier()) : "Unknown");
            lore.add(display);
            if(first) indexMap.put("tags", lore.size() - 1);
            first = false;
        }

        meta.setLore(lore);

        // ----------------------------
        // PDC
        // ----------------------------
        var pdc = meta.getPersistentDataContainer();

        pdc.set(Keys.FOOD_ID, PersistentDataType.STRING, item.getId());
        pdc.set(Keys.CATEGORY, PersistentDataType.STRING, item.getCategory());

        if (item.getOrigin() != null)
            pdc.set(Keys.ORIGIN, PersistentDataType.STRING, item.getOrigin());

        pdc.set(Keys.QUALITY, PersistentDataType.INTEGER, item.getQualityMin());

        pdc.set(Keys.LAST_UPDATE, PersistentDataType.LONG, System.currentTimeMillis());

        // TAG TRACK VALUES
        if (!item.getTagTracks().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            first = true;

            for (TagTrack t : item.getTagTracks()) {
                if (!first) sb.append(";");
                sb.append(t.getId()).append(".").append(t.getValue());
                first = false;
            }
            pdc.set(Keys.TAGS, PersistentDataType.STRING, sb.toString());
        }

        // SAUCE SAVE
        if (item.hasSauce()) {
            String nested = FoodParser.toString(item.getSauce(), 1);
            pdc.set(Keys.SAUCE, PersistentDataType.STRING, nested);
        }

        if (item.hasSauceName()) {
            pdc.set(Keys.SAUCE_NAME, PersistentDataType.STRING, item.getSauceName());
        }


        // STORE INDEX MAP
        {
            StringBuilder sb = new StringBuilder();
            first = true;

            for (var e : indexMap.entrySet()) {
                if (!first) sb.append(";");
                sb.append(e.getKey()).append(".").append(e.getValue());
                first = false;
            }

            pdc.set(Keys.LORE_INDEX_MAP, PersistentDataType.STRING, sb.toString());
        }

        // MODEL
        if (item.getModel() != null)
            pdc.set(Keys.MODEL, PersistentDataType.STRING, item.getModel().getId());

        stack.setItemMeta(meta);
        return stack;
    }

    private static String buildStars(int q, int max) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= max; i++)
            sb.append(i <= q ? "§6★" : "§7☆");
        return sb.toString();
    }

    public static void buildFromString(Player p, String string, ItemStack base) {
        FoodParser.Result parsed = FoodParser.parse(string);
        if (parsed == null || parsed.template == null) {
            p.sendMessage("§cInvalid item string!");
            return;
        }

        FoodItem template = parsed.template;
        boolean unique = parsed.unique;

        List<ItemStack> stacks = ItemBuilder.build(template, unique, base);
        boolean sound = true;

        for (ItemStack is : stacks) {
            ItemStack leftover = InventoryAdder.addItem(p, is);

            if (leftover == null) {
                if (sound) {
                    p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
                    sound = false;
                }
            } else {
                p.getWorld().dropItemNaturally(p.getLocation(), leftover);
            }
        }
    }


    public static ItemStack buildSingleString(String string, ItemStack base) {

        // Parse the input
        FoodParser.Result parsed = FoodParser.parse(string);

        FoodItem template = parsed.template;

        // Build item(s)
        ItemStack stack = ItemBuilder.buildSingle(template, base);
        return stack;
        
    }
}
