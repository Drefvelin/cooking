package net.tfminecraft.cooking.loader;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.tfminecraft.cooking.crafting.CraftingStation;

public class CraftingStationLoader {

    private static final List<CraftingStation> stations = new ArrayList<>();

    public static List<CraftingStation> get() { return stations; }
    public static CraftingStation getById(String id) {
        for (CraftingStation s : stations)
            if (s.getId().equalsIgnoreCase(id)) return s;
        return null;
    }

    public void load(File file) {

        FileConfiguration config = new YamlConfiguration();
        try { config.load(file); }
        catch (IOException | InvalidConfigurationException e) { e.printStackTrace(); }

        for (String id : config.getKeys(false)) {
            stations.add(new CraftingStation(id, config.getConfigurationSection(id)));
        }
    }
}
