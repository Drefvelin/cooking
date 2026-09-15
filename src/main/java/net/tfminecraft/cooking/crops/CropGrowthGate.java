package net.tfminecraft.cooking.crops;

import java.util.OptionalDouble;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.Material;

public final class CropGrowthGate {

    private CropGrowthGate() {}

    public static boolean shouldCancelVanilla(Material material, Location location) {
        CropDefinition crop = CropsConfig.byBlock(material);
        OptionalDouble affection = crop == null ? OptionalDouble.empty() : OptionalDouble.of(crop.affection());
        return !allowsGrowth(
                CropFertility.at(location),
                affection,
                CropsConfig.growthGateEnabled(),
                CropFertility.mapActive(),
                ThreadLocalRandom.current());
    }

    public static boolean allowsCustom(String cropId, Location location, OptionalDouble affectionOverride) {
        return allowsGrowth(
                CropFertility.at(location),
                resolveCustomAffection(cropId, affectionOverride),
                CropsConfig.growthGateEnabled(),
                CropFertility.mapActive(),
                ThreadLocalRandom.current());
    }

    static boolean allowsGrowth(
            int fertility,
            OptionalDouble affection,
            boolean featureEnabled,
            boolean mapActive,
            Random random) {
        if (!featureEnabled || !mapActive) {
            return true;
        }
        if (affection == null || affection.isEmpty() || random == null) {
            return true;
        }
        if (fertility >= 100) {
            return true;
        }
        if (fertility <= 0) {
            return false;
        }
        return CropGrowthChance.rollGrows(fertility, affection.getAsDouble(), random);
    }

    static OptionalDouble resolveCustomAffection(String cropId, OptionalDouble affectionOverride) {
        if (affectionOverride != null && affectionOverride.isPresent()) {
            return affectionOverride;
        }
        CropDefinition crop = CropsConfig.crop(cropId);
        if (crop == null) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(crop.affection());
    }
}
