package net.tfminecraft.cooking.loader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.tfminecraft.cooking.carve.CarveCut;
import net.tfminecraft.cooking.carve.CarveSequence;

public class CarveSequenceLoader {

    private static final Map<String, CarveSequence> sequences = new HashMap<>();

    public static CarveSequence get(String id) {
        if (id == null) return null;
        return sequences.get(id.toLowerCase());
    }

    public void load(File file) {
        sequences.clear();
        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            return;
        }

        for (String key : config.getKeys(false)) {
            ConfigurationSection sec = config.getConfigurationSection(key);
            if (sec == null) continue;

            int startRemaining = sec.getInt("start-remaining", 0);
            List<CarveCut> cuts = new ArrayList<>();

            for (Map<?, ?> map : sec.getMapList("cuts")) {
                Object outputObj = map.get("output");
                if (outputObj == null) continue;
                double food = map.containsKey("food")
                        ? ((Number) map.get("food")).doubleValue() : 1.0;
                double nutrition = map.containsKey("nutrition")
                        ? ((Number) map.get("nutrition")).doubleValue() : 1.0;
                cuts.add(new CarveCut(String.valueOf(outputObj), food, nutrition));
            }

            sequences.put(key.toLowerCase(), new CarveSequence(key, startRemaining, cuts));
        }
    }
}
