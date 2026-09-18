package net.tfminecraft.cooking.husbandry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public final class HusbandryDropRoller {

    private HusbandryDropRoller() {}

    public static List<ItemStack> rollSlaughterExtras(HusbandryAnimal animal, Random random, long nowMillis) {
        if (animal == null || random == null) {
            return List.of();
        }
        EntityType type = typeOf(animal);
        if (type == null) {
            return List.of();
        }
        HusbandrySpecies species = HusbandryConfig.species(type);
        if (species == null) {
            return List.of();
        }
        HusbandryDropTable table = species.drops();
        if (table == null || table.isEmpty()) {
            return List.of();
        }
        if (table.counted()) {
            int count = hideCount(animal, nowMillis);
            List<HusbandryDropEntry> pool = unlockedPool(table, HusbandryConfig.starsForGenetics(animal.genetics()));
            List<ItemStack> drops = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                HusbandryDropEntry picked = pickEntry(pool, random);
                if (picked == null || picked.path().isBlank()) {
                    continue;
                }
                ItemStack stack = HusbandryHarvest.buildTlibs(picked.path(), 1);
                if (stack != null) {
                    drops.add(stack);
                }
            }
            return drops;
        }
        return rollExtras(animal, random).map(List::of).orElse(List.of());
    }

    public static Optional<ItemStack> rollExtras(HusbandryAnimal animal, Random random) {
        if (animal == null || random == null) {
            return Optional.empty();
        }
        EntityType type = typeOf(animal);
        if (type == null) {
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
        List<HusbandryDropEntry> pool = unlockedPool(table, HusbandryConfig.starsForGenetics(animal.genetics()));
        HusbandryDropEntry picked = pickEntry(pool, random);
        if (picked == null || picked.path().isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(HusbandryHarvest.buildTlibs(picked.path(), picked.amount()));
    }

    public static int hideCount(HusbandryAnimal animal, long nowMillis) {
        if (animal == null) {
            return 0;
        }
        if (woolBlocked(animal.type(), animal, nowMillis)) {
            return 0;
        }
        return HusbandryConfig.hideCount(HusbandryConfig.effectiveGenetics(animal));
    }

    public static boolean woolBlocked(String typeName, HusbandryAnimal animal, long nowMillis) {
        if (typeName == null) {
            return false;
        }
        if (!typeName.equalsIgnoreCase("SHEEP") && !typeName.equalsIgnoreCase("GOAT")) {
            return false;
        }
        Long readyAt = animal == null ? null : animal.woolReadyAt();
        return readyAt != null && readyAt > nowMillis;
    }

    public static List<HusbandryDropEntry> unlockedPool(HusbandryDropTable table, int stars) {
        List<HusbandryDropEntry> pool = new ArrayList<>();
        if (table == null) {
            return pool;
        }
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
        return pool;
    }

    public static HusbandryDropEntry pickEntry(List<HusbandryDropEntry> pool, Random random) {
        if (pool == null || pool.isEmpty() || random == null) {
            return null;
        }
        int totalWeight = pool.stream().mapToInt(HusbandryDropEntry::weight).sum();
        if (totalWeight <= 0) {
            return null;
        }
        int roll = random.nextInt(totalWeight);
        for (HusbandryDropEntry entry : pool) {
            roll -= entry.weight();
            if (roll < 0) {
                return entry;
            }
        }
        return null;
    }

    private static EntityType typeOf(HusbandryAnimal animal) {
        try {
            return EntityType.valueOf(animal.type());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
