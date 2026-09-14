package net.tfminecraft.cooking.nutrition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public final class NutritionConfig {

    private static final int DEFAULT_MAX_FOOD = 200;
    private static final int DEFAULT_MAX_DIET = 40;
    private static final String DEFAULT_ATTRIBUTE_NAME = "nutrition";
    private static final int DEFAULT_DRAIN_AMOUNT = 1;
    private static final int DEFAULT_DRAIN_INTERVAL_SECONDS = 120;
    private static final double DEFAULT_LERP_STEP_RATE = 1.0;

    private static int maxFood = DEFAULT_MAX_FOOD;
    private static int maxDiet = DEFAULT_MAX_DIET;
    private static String attributeName = DEFAULT_ATTRIBUTE_NAME;
    private static int drainAmount = DEFAULT_DRAIN_AMOUNT;
    private static int drainIntervalSeconds = DEFAULT_DRAIN_INTERVAL_SECONDS;
    private static double lerpStepRate = DEFAULT_LERP_STEP_RATE;
    private static List<DietTierDefinition> dietTiers = defaultTiers();

    private NutritionConfig() {}

    public static void load(FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("nutrition");
        if (section == null) {
            applyDefaults();
            return;
        }

        maxFood = section.getInt("max-food", DEFAULT_MAX_FOOD);
        maxDiet = section.getInt("max-diet", DEFAULT_MAX_DIET);
        attributeName = section.getString("attribute-name", DEFAULT_ATTRIBUTE_NAME);
        drainAmount = section.getInt("drain-amount", DEFAULT_DRAIN_AMOUNT);
        drainIntervalSeconds = readDrainIntervalSeconds(section);
        lerpStepRate = section.getDouble("lerp-step-rate", DEFAULT_LERP_STEP_RATE);
        dietTiers = parseTiers(section);
    }

    public static int maxFood() {
        return maxFood;
    }

    public static int maxDiet() {
        return maxDiet;
    }

    public static String attributeName() {
        return attributeName;
    }

    public static int drainAmount() {
        return drainAmount;
    }

    public static int drainIntervalSeconds() {
        return drainIntervalSeconds;
    }

    private static int readDrainIntervalSeconds(ConfigurationSection section) {
        if (section.contains("drain-interval")) {
            return Math.max(1, section.getInt("drain-interval"));
        }
        if (section.contains("drain-interval-ticks")) {
            return Math.max(1, section.getInt("drain-interval-ticks") / 20);
        }
        return DEFAULT_DRAIN_INTERVAL_SECONDS;
    }

    public static double lerpStepRate() {
        return lerpStepRate;
    }

    public static List<DietTierDefinition> dietTiers() {
        return dietTiers;
    }

    public static DietTierDefinition resolveTier(int dietScore) {
        int clamped = Math.max(0, Math.min(dietScore, maxDiet));
        DietTierDefinition best = null;
        for (DietTierDefinition tier : dietTiers) {
            int threshold = (int) Math.floor(maxDiet * tier.getMinPercent() / 100.0);
            if (clamped >= threshold) {
                best = tier;
            }
        }
        return best != null ? best : dietTiers.get(0);
    }

    private static void applyDefaults() {
        maxFood = DEFAULT_MAX_FOOD;
        maxDiet = DEFAULT_MAX_DIET;
        attributeName = DEFAULT_ATTRIBUTE_NAME;
        drainAmount = DEFAULT_DRAIN_AMOUNT;
        drainIntervalSeconds = DEFAULT_DRAIN_INTERVAL_SECONDS;
        lerpStepRate = DEFAULT_LERP_STEP_RATE;
        dietTiers = defaultTiers();
    }

    private static List<DietTierDefinition> parseTiers(ConfigurationSection section) {
        List<Map<?, ?>> raw = section.getMapList("diet-tiers");
        if (raw.isEmpty()) {
            return defaultTiers();
        }

        List<DietTierDefinition> parsed = new ArrayList<>();
        for (Map<?, ?> entry : raw) {
            Object idRaw = entry.get("id");
            Object percentRaw = entry.get("min-percent");
            Object labelRaw = entry.get("label");
            if (idRaw == null || String.valueOf(idRaw).isBlank()) {
                continue;
            }
            int minPercent;
            try {
                minPercent = percentRaw instanceof Number
                        ? ((Number) percentRaw).intValue()
                        : Integer.parseInt(String.valueOf(percentRaw));
            } catch (NumberFormatException ex) {
                Bukkit.getLogger().warning("[Cooking] Invalid diet tier min-percent for id '" + idRaw + "'");
                continue;
            }
            String label = labelRaw != null ? String.valueOf(labelRaw) : idRaw.toString();
            parsed.add(new DietTierDefinition(String.valueOf(idRaw), minPercent, label));
        }

        if (parsed.isEmpty()) {
            return defaultTiers();
        }

        parsed.sort(Comparator.comparingInt(DietTierDefinition::getMinPercent));
        return Collections.unmodifiableList(parsed);
    }

    private static List<DietTierDefinition> defaultTiers() {
        List<DietTierDefinition> tiers = new ArrayList<>();
        tiers.add(new DietTierDefinition("terrible", 0, "<red>Terrible"));
        tiers.add(new DietTierDefinition("bad", 10, "<gold>Bad"));
        tiers.add(new DietTierDefinition("decent", 25, "<yellow>Decent"));
        tiers.add(new DietTierDefinition("good", 45, "<green>Good"));
        tiers.add(new DietTierDefinition("very_good", 65, "<aqua>Very Good"));
        tiers.add(new DietTierDefinition("fantastic", 85, "<light_purple>Fantastic"));
        return Collections.unmodifiableList(tiers);
    }
}
