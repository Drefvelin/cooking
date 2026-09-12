package net.tfminecraft.cooking.farming;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;

public final class FarmingConfig {

    private static boolean enabled = true;
    private static double harvestParticleMultiplier = 1.0;
    private static double replantParticleMultiplier = 1.0;
    private static boolean toolSwingParticle = true;
    private static int replantDelayMin = 10;
    private static int replantDelayMax = 20;
    private static boolean onlyHarvestMature = true;
    private static int toolDamagePerHarvest = 1;
    private static boolean applyUnbreaking = true;
    private static List<FarmingToolDefinition> tools = List.of();
    private static Map<Material, FarmingCropDefinition> cropsByType = Map.of();
    private static boolean antiTrampleEnabled = true;
    private static boolean trampleByWalking = false;
    private static boolean dryEmptyFarmland = true;
    private static double trampleParticleMultiplier = 1.0;
    private static Set<Material> trampleableCrops = Set.of();

    private FarmingConfig() {}

    public static void apply(
            boolean enabledValue,
            double harvestParticleMultiplierValue,
            double replantParticleMultiplierValue,
            boolean toolSwingParticleValue,
            int replantDelayMinValue,
            int replantDelayMaxValue,
            boolean onlyHarvestMatureValue,
            int toolDamagePerHarvestValue,
            boolean applyUnbreakingValue,
            List<FarmingToolDefinition> toolDefinitions,
            Map<Material, FarmingCropDefinition> cropDefinitions,
            boolean antiTrampleEnabledValue,
            boolean trampleByWalkingValue,
            boolean dryEmptyFarmlandValue,
            double trampleParticleMultiplierValue,
            Set<Material> trampleableCropMaterials) {
        enabled = enabledValue;
        harvestParticleMultiplier = harvestParticleMultiplierValue;
        replantParticleMultiplier = replantParticleMultiplierValue;
        toolSwingParticle = toolSwingParticleValue;
        replantDelayMin = replantDelayMinValue;
        replantDelayMax = Math.max(replantDelayMinValue, replantDelayMaxValue);
        onlyHarvestMature = onlyHarvestMatureValue;
        toolDamagePerHarvest = toolDamagePerHarvestValue;
        applyUnbreaking = applyUnbreakingValue;
        tools = List.copyOf(toolDefinitions);
        cropsByType = Map.copyOf(cropDefinitions);
        antiTrampleEnabled = antiTrampleEnabledValue;
        trampleByWalking = trampleByWalkingValue;
        dryEmptyFarmland = dryEmptyFarmlandValue;
        trampleParticleMultiplier = trampleParticleMultiplierValue;
        trampleableCrops = Set.copyOf(trampleableCropMaterials);
    }

    public static boolean enabled() {
        return enabled;
    }

    public static double harvestParticleMultiplier() {
        return harvestParticleMultiplier;
    }

    public static double replantParticleMultiplier() {
        return replantParticleMultiplier;
    }

    public static boolean toolSwingParticle() {
        return toolSwingParticle;
    }

    public static int replantDelayMin() {
        return replantDelayMin;
    }

    public static int replantDelayMax() {
        return replantDelayMax;
    }

    public static boolean onlyHarvestMature() {
        return onlyHarvestMature;
    }

    public static int toolDamagePerHarvest() {
        return toolDamagePerHarvest;
    }

    public static boolean applyUnbreaking() {
        return applyUnbreaking;
    }

    public static List<FarmingToolDefinition> tools() {
        return tools;
    }

    public static Map<Material, FarmingCropDefinition> cropsByType() {
        return cropsByType;
    }

    public static FarmingCropDefinition cropFor(Material material) {
        return cropsByType.get(material);
    }

    public static Set<Material> cropMaterials() {
        return cropsByType.keySet();
    }

    public static boolean antiTrampleEnabled() {
        return enabled && antiTrampleEnabled;
    }

    public static boolean trampleByWalking() {
        return trampleByWalking;
    }

    public static boolean dryEmptyFarmland() {
        return dryEmptyFarmland;
    }

    public static double trampleParticleMultiplier() {
        return trampleParticleMultiplier;
    }

    public static boolean isTrampleableCrop(Material material) {
        return material != null && trampleableCrops.contains(material);
    }
}
