package net.tfminecraft.cooking.utils;

import net.tfminecraft.cooking.item.FoodItem;
import net.tfminecraft.cooking.item.model.ModelData;
import net.tfminecraft.cooking.item.tag.TagStep;
import net.tfminecraft.cooking.item.tag.TagTrack;
import net.tfminecraft.cooking.utils.Keys;
import net.tfminecraft.furniture.Furniture;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import me.Plugins.TLibs.Objects.API.SubAPI.StringFormatter;

import java.util.*;

public class ItemUpdater {

    /** Ages the FoodItem. Returns true if any TagStep changed (including nested sauce). */
    public static boolean applyAging(FoodItem fi, long deltaSeconds) {
        if (fi == null || deltaSeconds <= 0) return false;

        boolean changed = false;

        // --- AGE MAIN TRACKS ---
        for (TagTrack track : fi.getTagTracks()) {
            if (!track.isAgeable()) continue;

            TagStep before = track.getCurrentStep();
            int newAge = track.getValue() + (int) deltaSeconds;

            track.setValue(newAge);
            TagStep after = track.getCurrentStep();

            if (!before.getTag().equals(after.getTag())) {
                changed = true;
            }
        }

        // --- AGE NESTED SAUCE ---
        if (fi.hasSauce()) {
            FoodItem sauce = fi.getSauce();
            boolean sauceChanged = applyAging(sauce, deltaSeconds);

            if (sauceChanged) changed = true;
        }

        return changed;
    }


    public static ItemStack applyItemUpdate(ItemStack stack, FoodItem fi, String furniture) {
        if (stack == null) return null;

        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return null;

        var pdc = meta.getPersistentDataContainer();

        double food = fi.getFinalFood();
        double nutrition = fi.getFinalNutrition();

        String indexRaw = pdc.get(Keys.LORE_INDEX_MAP, PersistentDataType.STRING);
        Map<String, Integer> indexMap = parseIndexMap(indexRaw);

        List<String> lore = meta.getLore();
        if (lore != null) {

            List<String> newLore = new ArrayList<>(lore);

            // food index
            Integer fiIndex = indexMap.get("food");
            if (fiIndex != null && fiIndex < newLore.size()) {
                newLore.set(fiIndex,
                        StringFormatter.formatHex("#d4ad77Food §f" + food)
                );
            }

            // nutrition index
            Integer nIndex = indexMap.get("nutrition");
            if (nIndex != null && nIndex < newLore.size()) {
                newLore.set(nIndex,
                        StringFormatter.formatHex("#d4ad77Nutrition §f" + nutrition)
                );
            }

            // ---------------------------------------------------------
            // SAUCE BLOCK REWRITE (handles newly added sauces)
            // ---------------------------------------------------------
            int sauceStart = indexMap.getOrDefault("sauce", -1);

            if (fi.hasSauce()) {

                FoodItem sauce = fi.getSauce();
                String sName = fi.hasSauceName() ? fi.getSauceName() : "Sauce";
                String stats = DisplayUtils.getSauceStatString(
                        sauce.getFinalFood(),
                        sauce.getFinalNutrition()
                );

                String sauceLine = "§6" + sName + " §8(" + stats + "§8)";

                if (sauceStart == -1) {
                    // -----------------------------------------------------
                    // Item DID NOT have sauce → INSERT IT
                    // -----------------------------------------------------

                    Integer tagStart = indexMap.get("tags");
                    if (tagStart == null) tagStart = newLore.size(); // fallback end

                    // We insert EXACTLY:
                    //   <blank>
                    //   sauce line
                    //   <blank>
                    newLore.add(tagStart, sauceLine);   // sauce line
                    newLore.add(tagStart + 1, " ");         // blank below sauce

                    // Register new sauce index (the line with text, not the blanks)
                    indexMap.put("sauce", tagStart);

                    // Shift tagStart by 3
                    indexMap.put("tags", tagStart + 2);

                } else {
                    // -----------------------------------------------------
                    // Sauce existed → UPDATE it
                    // -----------------------------------------------------
                    newLore.set(sauceStart, sauceLine);

                    // Ensure exactly ONE blank above
                    if (sauceStart - 1 < 0 || !newLore.get(sauceStart - 1).trim().isEmpty()) {
                        newLore.add(sauceStart, " ");
                        sauceStart++;
                        indexMap.put("tags", indexMap.get("tags") + 1);
                    }

                    // Ensure exactly ONE blank below
                    if (sauceStart + 1 >= newLore.size() || !newLore.get(sauceStart + 1).trim().isEmpty()) {
                        newLore.add(sauceStart + 1, " ");
                        indexMap.put("tags", indexMap.get("tags") + 1);
                    }
                }

            } else {
                // ---------------------------------------------------------
                // No sauce now → remove old sauce block if present
                // ---------------------------------------------------------
                if (sauceStart != -1) {

                    // Remove below blank if present
                    if (sauceStart + 1 < newLore.size() && newLore.get(sauceStart + 1).trim().isEmpty()) {
                        newLore.remove(sauceStart + 1);
                    }

                    // Remove sauce line
                    newLore.remove(sauceStart);

                    // Remove above blank if present
                    if (sauceStart - 1 >= 0 && newLore.get(sauceStart - 1).trim().isEmpty()) {
                        newLore.remove(sauceStart - 1);
                        sauceStart--;
                    }

                    // Shift tag index back
                    Integer tagStart = indexMap.get("tags");
                    if (tagStart != null) {
                        indexMap.put("tags", tagStart - 2); // removed exactly 2 blanks
                    }

                    indexMap.remove("sauce");
                }
            }
            // TAG SECTION REWRITE -------------------------------------------------
            Integer tagStart = indexMap.get("tags");
            if (tagStart != null) {

                // Ensure list is big enough
                while (newLore.size() < tagStart)
                    newLore.add("");

                int pos = tagStart;

                for (TagTrack track : fi.getTagTracks()) {
                    TagStep step = track.getCurrentStep();
                    if (step == null) continue;

                    String line = DisplayUtils.getDisplayString(
                            step.getName(),
                            step.getFoodMultiplier(),
                            step.getNutritionMultiplier()
                    );

                    if (pos < newLore.size())
                        newLore.set(pos, line);
                    else
                        newLore.add(line);

                    pos++;
                }

                // Optionally trim extra old tag lines:
                // Remove leftover lines after newer tags
                while (newLore.size() > pos) newLore.remove(newLore.size() - 1);
            }

            meta.setLore(newLore);
        }

        //pdc update --------------------------------------------------------------
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (TagTrack t : fi.getTagTracks()) {
            if (!first) sb.append(";");
            sb.append(t.getId()).append(".").append(t.getValue());
            first = false;
        }
        // Save sauce (nested FoodItem)
        if (fi.hasSauce()) {
            String sauceString = FoodParser.toString(fi.getSauce(), 1);
            pdc.set(Keys.SAUCE, PersistentDataType.STRING, sauceString);
        } else {
            pdc.remove(Keys.SAUCE);
        }
        // Save sauce name for UI display
        if (fi.hasSauceName()) {
            pdc.set(Keys.SAUCE_NAME, PersistentDataType.STRING, fi.getSauceName());
        } else {
            pdc.remove(Keys.SAUCE_NAME);
        }

        pdc.set(Keys.TAGS, PersistentDataType.STRING, sb.toString());
        pdc.set(Keys.LAST_UPDATE, PersistentDataType.LONG, System.currentTimeMillis());

        stack.setItemMeta(meta);

        // model update -----------------------------------------------------------
        ModelData newModel = fi.getModelData();
        stack = newModel.apply(furniture, stack);

        return stack;
    }



    /** The old method — now split cleanly. */
    public static ItemStack updateItem(ItemStack stack, FoodItem fi, String furniture) {

        long now = System.currentTimeMillis();

        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return null;

        var pdc = meta.getPersistentDataContainer();

        Long lastUpdate = pdc.get(Keys.LAST_UPDATE, PersistentDataType.LONG);
        if (lastUpdate == null) lastUpdate = now;

        long deltaSeconds = (now - lastUpdate) / 1000;
        if (deltaSeconds <= 0) return null;

        boolean changed = applyAging(fi, deltaSeconds);
        if (!changed) return null;

        return applyItemUpdate(stack, fi, furniture);
    }


    private static Map<String, Integer> parseIndexMap(String raw) {
        Map<String, Integer> map = new HashMap<>();
        if (raw == null || raw.isEmpty()) return map;

        String[] parts = raw.split(";");

        for (String p : parts) {
            if (!p.contains(".")) continue;

            String[] kv = p.split("\\.");
            if (kv.length != 2) continue;

            try {
                int idx = Integer.parseInt(kv[1]);
                map.put(kv[0].toLowerCase(), idx);
            } catch (NumberFormatException ignored) {}
        }

        return map;
    }
}
