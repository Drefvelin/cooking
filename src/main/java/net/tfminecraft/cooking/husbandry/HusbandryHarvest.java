package net.tfminecraft.cooking.husbandry;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import me.Plugins.TLibs.TLibs;
import net.tfminecraft.cooking.carve.CarvableRoastUtils;
import net.tfminecraft.cooking.carve.CarveSequence;
import net.tfminecraft.cooking.item.FoodItem;
import net.tfminecraft.cooking.item.model.ModelData;
import net.tfminecraft.cooking.loader.CarveSequenceLoader;
import net.tfminecraft.cooking.utils.FoodParser;
import net.tfminecraft.cooking.utils.ItemBuilder;
import net.tfminecraft.cooking.utils.ItemUpdater;
import net.tfminecraft.cooking.utils.QualityUtils;

public final class HusbandryHarvest {

    private HusbandryHarvest() {}

    public static void prepareNewAnimal(HusbandryAnimal animal, EntityType type) {
        if (animal == null) {
            return;
        }
        HusbandrySpecies species = HusbandryConfig.species(type);
        if (species != null && species.hasHarvest("shear")) {
            animal.setWoolReadyAt(System.currentTimeMillis());
        }
        if (species != null && species.hasHarvest("shed")) {
            animal.setShedReadyAt(System.currentTimeMillis());
        }
        if (species != null && species.hasHarvest("egg")) {
            animal.setEggReadyAt(System.currentTimeMillis());
        }
    }

    public static int stars(HusbandryAnimal animal) {
        if (animal == null) {
            return 1;
        }
        return HusbandryConfig.starsForGenetics(animal.genetics());
    }

    public static ItemStack buildFood(HusbandryAnimal animal, String foodString) {
        if (foodString == null || foodString.isBlank() || animal == null) {
            return null;
        }
        FoodParser.Result parsed = FoodParser.parse(foodString);
        if (parsed == null || parsed.template == null) {
            return null;
        }
        int quality = QualityUtils.clamp(stars(animal));
        ItemStack stack = ItemBuilder.buildSingleWithQuality(parsed.template, null, quality);
        FoodItem item = FoodItem.fromItem(stack);
        if (item == null) {
            return stack;
        }
        CarvableRoastUtils.readCarveState(item, stack);
        String seqId = item.getCarveSequencePdc() != null
                ? item.getCarveSequencePdc()
                : item.getCarveSequenceId();
        if (seqId == null) {
            return stack;
        }
        CarveSequence seq = CarveSequenceLoader.get(seqId);
        if (seq == null) {
            return stack;
        }
        int maxCuts = Math.max(1, seq.getStartRemaining());
        int cuts = Math.min(maxCuts, HusbandryConfig.roastCutsFor(HusbandryConfig.effectiveGenetics(animal)));
        int nextIndex = Math.max(0, maxCuts - cuts);
        item.setCarveState(seqId, nextIndex, cuts);
        CarvableRoastUtils.writeCarveState(stack, item);
        ItemStack updated = ItemUpdater.applyItemUpdate(stack, item, null);
        if (updated != null) {
            stack = updated;
        }
        ModelData staged = item.getModel() == null
                ? null
                : item.getModel().getModelByStageAndTag(cuts, CarvableRoastUtils.resolveCookTag(item));
        if (staged != null) {
            stack = staged.apply(null, stack);
        }
        return stack;
    }

    public static ItemStack buildTlibs(String path, int amount) {
        if (path == null || path.isBlank() || "vanilla".equalsIgnoreCase(path)) {
            return null;
        }
        try {
            ItemStack stack = TLibs.getItemAPI().getCreator().getItemFromPath(path);
            if (stack == null) {
                return null;
            }
            stack.setAmount(Math.max(1, Math.min(64, amount)));
            return stack;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    public static boolean isWoolDrop(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        String name = stack.getType().name();
        return name.endsWith("_WOOL") || name.equals("WOOL") || stack.getType() == Material.WHITE_WOOL;
    }
}
