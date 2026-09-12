package net.tfminecraft.cooking.loader;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.tfminecraft.cooking.quality.QualityConfig;

public class QualityConfigLoader {
    public void load(File configFile) {
        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        int min = config.getInt("pickup.min", 1);
        int max = config.getInt("pickup.max", 5);

        Set<String> excludes = new HashSet<>();
        for (String entry : config.getStringList("composition.exclude-categories")) {
            if (entry != null && !entry.isBlank()) {
                excludes.add(entry.toLowerCase());
            }
        }

        QualityConfig.apply(min, max, excludes);
    }
}
