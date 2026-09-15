package net.tfminecraft.cooking.husbandry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public final class HusbandryDropRoller {

    private HusbandryDropRoller() {}

    public static Optional<ItemStack> rollExtras(HusbandryAnimal animal, Random random) {
        if (animal == null || random == null) {
            return Optional.empty();
        }
        EntityType type;
        try {
            type = EntityType.valueOf(animal.type());
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
        HusbandrySpecies species = HusbandryConfig.species(type);
        if (species == null) {
            return Optional.empty();
        }
        HusbandryDropTable table = species.drops();
        if (table == null || table.isEmpty()) {
            return Optional.empty();
        }
        int stars = HusbandryConfig.starsForGenetics(animal.genetics());
        List<HusbandryDropEntry> pool = new ArrayList<>();
        pool.addAll(table.common());
        if (stars >= 3) {
            pool.addAll(table.rare());
        }
        if (stars >= 4) {
            pool.addAll(table.epic());
        }
        if (stars >= 5) {
            pool.addAll(table.legendary());
        }
        if (pool.isEmpty()) {
            return Optional.empty();
        }
        int totalWeight = pool.stream().mapToInt(HusbandryDropEntry::weight).sum();
        if (totalWeight <= 0) {
            return Optional.empty();
        }
        int roll = random.nextInt(totalWeight);
        for (HusbandryDropEntry entry : pool) {
            roll -= entry.weight();
            if (roll < 0) {
                ItemStack stack = HusbandryHarvest.buildTlibs(entry.path(), entry.amount());
                return Optional.ofNullable(stack);
            }
        }
        return Optional.empty();
    }
}
