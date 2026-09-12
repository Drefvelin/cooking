package net.tfminecraft.cooking.quality;

import java.util.HashSet;
import java.util.Set;

public final class QualityConfig {
    private static int pickupMin = 1;
    private static int pickupMax = 5;
    private static Set<String> excludeCategories = defaultExcludes();

    private QualityConfig() {}

    private static Set<String> defaultExcludes() {
        Set<String> set = new HashSet<>();
        set.add("seasoning");
        set.add("sweetener");
        set.add("flour");
        set.add("salt");
        set.add("pepper");
        return set;
    }

    public static void apply(int min, int max, Set<String> excludes) {
        pickupMin = Math.max(1, min);
        pickupMax = Math.max(pickupMin, max);
        excludeCategories = excludes == null || excludes.isEmpty()
                ? defaultExcludes()
                : Set.copyOf(excludes);
    }

    public static int getPickupMin() {
        return pickupMin;
    }

    public static int getPickupMax() {
        return pickupMax;
    }

    public static Set<String> getExcludeCategories() {
        return excludeCategories;
    }

    public static boolean isExcludedCategory(String category) {
        if (category == null) {
            return false;
        }
        return excludeCategories.contains(category.toLowerCase());
    }
}
