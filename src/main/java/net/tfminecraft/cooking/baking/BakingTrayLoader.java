package net.tfminecraft.cooking.baking;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public final class BakingTrayLoader {
    public void load(File file) {
        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            BakingTrayRegistry.load(Map.of());
            return;
        }

        Map<String, BakingTrayRecipe> recipes = new LinkedHashMap<>();
        for (String recipeId : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(recipeId);
            if (section == null) {
                continue;
            }

            String furnitureId = section.getString("furniture");
            if (furnitureId == null || furnitureId.isBlank()) {
                Bukkit.getLogger().warning("[Cooking] baking-trays entry '" + recipeId + "' missing furniture id");
                continue;
            }

            String category = section.getString("category", "grain");
            Map<String, List<String>> molds = parseMolds(section.getConfigurationSection("molds"));
            if (molds.isEmpty()) {
                Bukkit.getLogger().warning("[Cooking] baking-trays entry '" + recipeId + "' has no molds");
                continue;
            }

            ConfigurationSection fillSection = section.getConfigurationSection("fill");
            if (fillSection == null) {
                Bukkit.getLogger().warning("[Cooking] baking-trays entry '" + recipeId + "' missing fill section");
                continue;
            }

            String input = fillSection.getString("input");
            String inputFood = fillSection.getString("input-food");
            String food = fillSection.getString("food");
            String tags = fillSection.getString("tags", "cooked.0:freshness.0");
            int outputsPerMold = fillSection.getInt("outputs-per-mold", 2);
            if (input == null || food == null) {
                Bukkit.getLogger().warning("[Cooking] baking-trays entry '" + recipeId + "' has invalid fill config");
                continue;
            }

            BakingTrayFill fill = new BakingTrayFill(input, inputFood, outputsPerMold, food, tags, category);

            ConfigurationSection bakeSection = section.getConfigurationSection("bake");
            BakingTrayBake bake = bakeSection == null
                    ? new BakingTrayBake(45, 90, true)
                    : new BakingTrayBake(
                            bakeSection.getInt("cook-seconds", 45),
                            bakeSection.getInt("burn-seconds", 90),
                            bakeSection.getBoolean("sync", true));

            recipes.put(recipeId, new BakingTrayRecipe(recipeId, furnitureId, molds, fill, bake));
        }

        BakingTrayRegistry.load(recipes);
    }

    private static Map<String, List<String>> parseMolds(ConfigurationSection moldsSection) {
        Map<String, List<String>> molds = new LinkedHashMap<>();
        if (moldsSection == null) {
            return molds;
        }
        for (String moldId : moldsSection.getKeys(false)) {
            List<String> slots = moldsSection.getStringList(moldId);
            if (!slots.isEmpty()) {
                molds.put(moldId, new ArrayList<>(slots));
            }
        }
        return molds;
    }
}
