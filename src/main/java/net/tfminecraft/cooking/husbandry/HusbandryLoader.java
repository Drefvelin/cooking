package net.tfminecraft.cooking.husbandry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import me.Plugins.TLibs.Utils.TimeFormatter;

public final class HusbandryLoader {

    public void load(File file) {
        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException ex) {
            ex.printStackTrace();
            applyDefaults();
            return;
        }

        HusbandryConfig.apply(
                config.getInt("max-animals", 15),
                config.getInt("care-max", 200),
                HusbandryDuration.parseCareIntervalSeconds(config, "care.up", "care-up-per-hour", 3600),
                HusbandryDuration.parseCareAmount(config, "care.up", "care-up-per-hour", 1),
                HusbandryDuration.parseCareIntervalSeconds(config, "care.down", "care-down-per-hour", 3600),
                HusbandryDuration.parseCareAmount(config, "care.down", "care-down-per-hour", 1),
                HusbandryDuration.parseSeconds(config, "decay-grace", "decay-grace-hours", 3600, 86400),
                HusbandryDuration.parseSeconds(config, "offline-care", "offline-care-hours", 3600, 28800),
                HusbandryDuration.parseSeconds(config, "long-unload-force", "long-unload-force-hours", 3600, 28800),
                HusbandryDuration.parseSeconds(config, "min-loaded", "min-loaded-seconds", 1, 60),
                HusbandryDuration.parseAfflictionHours(config, "mean", "affliction-mean-hours", 6),
                HusbandryDuration.parseAfflictionHours(config, "min", "affliction-min-hours", 4),
                HusbandryDuration.parseAfflictionHours(config, "max", "affliction-max-hours", 8),
                HusbandryDuration.parseSeconds(config, "milk-cooldown", "milk-cooldown-minutes", 60, 1200),
                config.getInt("initial-genetic-max", 20),
                config.getInt("max-genetics", 1000),
                config.getInt("min-roast-cuts", 1),
                HusbandryDuration.parseSeconds(config, "wool-timer", "wool-timer-hours", 3600, 28800),
                HusbandryDuration.parseSeconds(config, "grow-up", null, 1, 3600),
                HusbandryDuration.parseSeconds(config, "shed-timer", null, 1, 28800),
                config.getDouble("shed-chance", 0.15),
                HusbandryDuration.parseSeconds(config, "egg-timer", null, 1, 600),
                config.getString("items.tame", ""),
                config.getString("items.co-own", ""),
                config.getString("items.feed", ""),
                config.getString("items.glove", ""),
                config.getString("items.neuter", ""),
                config.getString("items.inspect", ""),
                config.getString("items.mount-stats", ""),
                parseEntityTypes(config.getStringList("remove-unowned")),
                parseSpecies(config.getConfigurationSection("species")),
                config.getBoolean("damage.other-players", true),
                config.getBoolean("damage.owner", true),
                config.getBoolean("damage.mobs", true),
                config.getBoolean("damage.environment", true),
                parseQualityBands(config.getMapList("quality-from-genetics")),
                parseMounts(config.getConfigurationSection("mounts")),
                config.getDouble("breeding.genetic-variance-multiplier", 1),
                config.getDouble("breeding.genetic-slowdown-divisor", 1),
                parseAmountBands(config.getMapList("amount-from-genetics")),
                config.getBoolean("mounts.nerf", true),
                config.getDouble("mounts.nerf-divisor", 2));
    }

    private static Set<EntityType> parseEntityTypes(List<String> raw) {
        Set<EntityType> types = EnumSet.noneOf(EntityType.class);
        if (raw == null) {
            return types;
        }
        for (String entry : raw) {
            EntityType type = parseEntityType(entry);
            if (type != null) {
                types.add(type);
            }
        }
        return types;
    }

    private static Map<EntityType, HusbandrySpecies> parseSpecies(ConfigurationSection section) {
        Map<EntityType, HusbandrySpecies> species = new EnumMap<>(EntityType.class);
        if (section == null) {
            return species;
        }
        for (String key : section.getKeys(false)) {
            EntityType type = parseEntityType(key);
            if (type == null) {
                continue;
            }
            Set<String> harvest = new HashSet<>();
            List<String> modes = section.getStringList(key + ".harvest");
            for (String mode : modes) {
                if (mode != null && !mode.isBlank()) {
                    harvest.add(mode.trim().toLowerCase(Locale.ROOT));
                }
            }
            species.put(type, new HusbandrySpecies(
                    type,
                    harvest,
                    section.getString(key + ".slaughter", ""),
                    section.getString(key + ".milk", ""),
                    section.getString(key + ".shear", ""),
                    section.getString(key + ".egg", ""),
                    section.getString(key + ".shed", ""),
                    parseSpeciesGrowUp(section, key),
                    parseDropTable(section.getConfigurationSection(key + ".drops"))));
        }
        return species;
    }

    private static HusbandryDropTable parseDropTable(ConfigurationSection section) {
        if (section == null) {
            return HusbandryDropTable.empty();
        }
        return new HusbandryDropTable(
                parseDropEntries(section.getMapList("common")),
                parseDropEntries(section.getMapList("rare")),
                parseDropEntries(section.getMapList("epic")),
                parseDropEntries(section.getMapList("legendary")));
    }

    private static List<HusbandryDropEntry> parseDropEntries(List<Map<?, ?>> raw) {
        List<HusbandryDropEntry> entries = new ArrayList<>();
        if (raw == null) {
            return entries;
        }
        for (Map<?, ?> entry : raw) {
            Object pathRaw = entry.get("path");
            if (pathRaw == null || String.valueOf(pathRaw).isBlank()) {
                continue;
            }
            entries.add(new HusbandryDropEntry(
                    String.valueOf(pathRaw),
                    intValue(entry.get("amount"), 1),
                    intValue(entry.get("weight"), 1)));
        }
        return entries;
    }

    private static int parseSpeciesGrowUp(ConfigurationSection section, String key) {
        if (section == null || !section.contains(key + ".grow-up")) {
            return 0;
        }
        return Math.max(1, TimeFormatter.parseSeconds(section.getString(key + ".grow-up")));
    }

    private static List<HusbandryQualityBand> parseQualityBands(List<Map<?, ?>> raw) {
        List<HusbandryQualityBand> bands = new ArrayList<>();
        if (raw == null) {
            return bands;
        }
        for (Map<?, ?> entry : raw) {
            int min = intValue(entry.get("min"), 0);
            int stars = intValue(entry.get("stars"), 1);
            bands.add(new HusbandryQualityBand(min, Math.max(1, Math.min(5, stars))));
        }
        bands.sort(Comparator.comparingInt(HusbandryQualityBand::minGenetics));
        return bands;
    }

    private static List<HusbandryAmountBand> parseAmountBands(List<Map<?, ?>> raw) {
        List<HusbandryAmountBand> bands = new ArrayList<>();
        if (raw == null) {
            return bands;
        }
        for (Map<?, ?> entry : raw) {
            bands.add(new HusbandryAmountBand(
                    intValue(entry.get("min"), 0),
                    Math.max(1, intValue(entry.get("roast-cuts"), 1)),
                    Math.max(1, intValue(entry.get("wool"), 1))));
        }
        bands.sort(Comparator.comparingInt(HusbandryAmountBand::minGenetics));
        return bands;
    }

    private static Map<EntityType, HusbandryMountStats> parseMounts(ConfigurationSection section) {
        Map<EntityType, HusbandryMountStats> mounts = new EnumMap<>(EntityType.class);
        if (section == null) {
            return mounts;
        }
        for (String key : section.getKeys(false)) {
            if ("nerf".equalsIgnoreCase(key) || "nerf-divisor".equalsIgnoreCase(key)) {
                continue;
            }
            EntityType type = parseEntityType(key);
            ConfigurationSection stats = section.getConfigurationSection(key);
            if (type == null || stats == null) {
                continue;
            }
            mounts.put(type, new HusbandryMountStats(
                    type,
                    stats.getDouble("min-health", 15),
                    stats.getDouble("max-health", 30),
                    stats.getDouble("min-speed", 0.1125),
                    stats.getDouble("max-speed", 0.3375),
                    stats.getDouble("min-jump", 0.4),
                    stats.getDouble("max-jump", 1.0)));
        }
        return mounts;
    }

    private static EntityType parseEntityType(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return EntityType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            Bukkit.getLogger().warning("[Cooking] Invalid husbandry entity type: " + raw);
            return null;
        }
    }

    private static int intValue(Object raw, int fallback) {
        if (raw instanceof Number number) {
            return number.intValue();
        }
        if (raw != null) {
            try {
                return Integer.parseInt(String.valueOf(raw));
            } catch (NumberFormatException ignored) {
                Bukkit.getLogger().warning("[Cooking] Invalid husbandry number: " + raw);
            }
        }
        return fallback;
    }

    private static void applyDefaults() {
        HusbandryConfig.apply(
                15,
                200,
                3600,
                1,
                3600,
                1,
                86400,
                28800,
                28800,
                60,
                6,
                4,
                8,
                1200,
                20,
                1000,
                1,
                28800,
                3600,
                28800,
                0.15,
                600,
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                Set.of(),
                Map.of(),
                true,
                true,
                true,
                true,
                List.of(new HusbandryQualityBand(0, 1)),
                Map.of(),
                1,
                1,
                List.of(new HusbandryAmountBand(0, 1, 1)),
                true,
                2);
    }
}
