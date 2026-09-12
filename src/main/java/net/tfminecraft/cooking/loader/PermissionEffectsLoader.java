package net.tfminecraft.cooking.loader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.tfminecraft.cooking.quality.PermissionEffectsConfig;
import net.tfminecraft.cooking.quality.PermissionEffectsConfig.CompositionEffect;
import net.tfminecraft.cooking.quality.PermissionEffectsConfig.PickupEffect;

public class PermissionEffectsLoader {
    public void load(File configFile) {
        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        List<PickupEffect> pickup = new ArrayList<>();
        ConfigurationSection pickupSection = config.getConfigurationSection("pickup");
        if (pickupSection != null) {
            for (String key : pickupSection.getKeys(false)) {
                ConfigurationSection section = pickupSection.getConfigurationSection(key);
                if (section == null) {
                    continue;
                }
                String permission = section.getString("permission");
                if (permission == null || permission.isBlank()) {
                    continue;
                }
                pickup.add(new PickupEffect(
                        key,
                        permission,
                        section.getInt("min-quality", 1),
                        section.getInt("roll-bias", 0)
                ));
            }
        }

        List<CompositionEffect> composition = new ArrayList<>();
        ConfigurationSection compositionSection = config.getConfigurationSection("composition");
        if (compositionSection != null) {
            for (String key : compositionSection.getKeys(false)) {
                ConfigurationSection section = compositionSection.getConfigurationSection(key);
                if (section == null) {
                    continue;
                }
                String permission = section.getString("permission");
                if (permission == null || permission.isBlank()) {
                    continue;
                }
                composition.add(new CompositionEffect(
                        key,
                        permission,
                        section.getDouble("chance", 0),
                        section.getInt("boost", 1)
                ));
            }
        }

        PermissionEffectsConfig.apply(pickup, composition);
    }
}
