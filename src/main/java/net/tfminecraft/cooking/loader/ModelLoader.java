package net.tfminecraft.cooking.loader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.tfminecraft.cooking.item.model.FoodModel;

public class ModelLoader {

    public static List<FoodModel> models = new ArrayList<>();

    public static List<FoodModel> get() {
        return models;
    }

    public static FoodModel getById(String id) {
        for (FoodModel m : models) {
            // We must assume FoodModel stores an ID; if not we add it
            if (m.getId().equalsIgnoreCase(id)) {
                return m;
            }
        }
        return null;
    }

    public void load(File file) {
        FileConfiguration config = new YamlConfiguration();

        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        Set<String> keys = config.getKeys(false);

        for (String key : keys) {
            FoodModel model = new FoodModel(key, config.getConfigurationSection(key));
            models.add(model);
        }
    }
}
