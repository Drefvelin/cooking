package net.tfminecraft.cooking.milling;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MillingRecipeRegistry {
    private static Map<String, MillingRecipe> byFurnitureId = Map.of();

    private MillingRecipeRegistry() {}

    public static void load(Map<String, MillingRecipe> recipes) {
        Map<String, MillingRecipe> byFurniture = new LinkedHashMap<>();
        for (MillingRecipe recipe : recipes.values()) {
            byFurniture.put(recipe.getFurnitureId().toLowerCase(), recipe);
        }
        byFurnitureId = Collections.unmodifiableMap(byFurniture);
    }

    public static MillingRecipe getByFurnitureId(String furnitureId) {
        if (furnitureId == null) {
            return null;
        }
        return byFurnitureId.get(furnitureId.toLowerCase());
    }

    public static Collection<MillingRecipe> getAll() {
        return byFurnitureId.values();
    }
}
