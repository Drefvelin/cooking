package net.tfminecraft.cooking.loader;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.tfminecraft.cooking.cache.CategoryDictionary;
import net.tfminecraft.cooking.cache.FurnitureCache;
import net.tfminecraft.cooking.cache.ItemCache;

public class ConfigLoader {
    public void loadConfig(File configFile) {
		FileConfiguration config = new YamlConfiguration();
        try {
        	config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        FurnitureCache.fryingPan = config.getString("frying_pan", "none");
        FurnitureCache.saucePan = config.getString("saucepan", "none");
        FurnitureCache.plate = config.getString("plate", "none");
        FurnitureCache.bowl = config.getString("bowl", "none");
        FurnitureCache.pot = config.getString("pot", "none");

        FurnitureCache.butterChurn = config.getString("butter-churn", "none");
        FurnitureCache.butterPlate = config.getString("butter-plate", "none");
        FurnitureCache.firePit = config.getString("fire-pit", "none");

        ItemCache.butter = config.getString("butter", "none");
        ItemCache.butterModel = config.getString("butter-model", "v.magma_cream");
        ItemCache.butterPieceModel = config.getString("butter-piece-model", "v.magma_cream");
        ItemCache.firePitTurner = config.getString("fire-pit-turner", "ia.tfmc_cooking:fire_pit_turner");
        ItemCache.firePitVisualY = (float) config.getDouble("fire-pit-visual-y", 0.5);
        ItemCache.firePitPivotY = (float) config.getDouble("fire-pit-pivot-y", 0.5);
        ItemCache.firePitSpinAxis = config.getString("fire-pit-spin-axis", "x").toLowerCase();
        ItemCache.liquidFallback = config.getString("liquid-fallback", "ia.tfmc_cooking:water");

        ItemCache.water = config.getString("water", "v.water_bucket");

        ItemCache.ladle = config.getString("ladle", "none");
        ItemCache.masher = config.getString("masher", "none");

        ItemCache.grain = config.getString("grain-item", "none");
        ItemCache.flour = config.getString("flour-item", "none");
        ItemCache.bag = config.getString("bag", "none");

        if(config.contains("liquids")) {
            for (String key : config.getStringList("liquids")) {
                String[] parts = key.split("\\s+"); 
                if(parts.length != 2) continue;
                String path = parts[0];
                String model = parts[1];
                ItemCache.liquidModels.put(path, model);
            }
        }
        if(config.contains("colours")) {
            for (String key : config.getStringList("colours")) {
                String[] parts = key.split("\\s+"); 
                if(parts.length != 2) continue;
                String path = parts[0];
                String model = parts[1];
                ItemCache.colourMap.put(path, model);
            }
        }

        if(config.isConfigurationSection("dictionary")) {
            for (String key : config.getConfigurationSection("dictionary").getKeys(false)) {
                String value = config.getString("dictionary." + key, "none");
                if(value.equals("none")) continue;
                CategoryDictionary.dictionary.put(key, value);
            }
        }
        if(config.contains("sauce-dict")) {
            for (String key : config.getStringList("sauce-dict")) {
                String[] parts = key.split("\\s+"); 
                if(parts.length != 2) continue;
                String hex = parts[0];
                String model = parts[1];
                CategoryDictionary.sauceDict.put(hex, model);
            }
        }
	}
}
