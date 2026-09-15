package net.tfminecraft.cooking.husbandry;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;

import net.tfminecraft.cooking.Cooking;

public final class HusbandryEntities {

    private static final Set<UUID> LOADED = ConcurrentHashMap.newKeySet();

    private HusbandryEntities() {}

    public static Set<UUID> loadedIds() {
        return Collections.unmodifiableSet(LOADED);
    }

    public static void trackLoaded(UUID uuid) {
        if (uuid != null) {
            LOADED.add(uuid);
        }
    }

    public static void untrack(UUID uuid) {
        if (uuid != null) {
            LOADED.remove(uuid);
        }
    }

    public static void clearLoaded() {
        LOADED.clear();
    }

    public static boolean isManaged(Entity entity) {
        if (entity == null) {
            return false;
        }
        Byte value = entity.getPersistentDataContainer().get(HusbandryKeys.MANAGED, PersistentDataType.BYTE);
        return value != null && value == 1;
    }

    public static void stampManaged(Entity entity) {
        if (entity == null) {
            return;
        }
        entity.getPersistentDataContainer().set(HusbandryKeys.MANAGED, PersistentDataType.BYTE, (byte) 1);
    }

    public static void applyPersistFlags(LivingEntity entity) {
        if (entity == null) {
            return;
        }
        entity.setPersistent(true);
        entity.setRemoveWhenFarAway(false);
    }

    public static String displayName(org.bukkit.entity.EntityType type) {
        if (type == null) {
            return "Animal";
        }
        String raw = type.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        StringBuilder out = new StringBuilder();
        for (String word : raw.split(" ")) {
            if (word.isEmpty()) {
                continue;
            }
            if (!out.isEmpty()) {
                out.append(' ');
            }
            out.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                out.append(word.substring(1));
            }
        }
        return out.toString();
    }

    public static HusbandryRepository repository() {
        Cooking plugin = Cooking.plugin;
        if (plugin == null) {
            return null;
        }
        return plugin.getHusbandryRepository();
    }
}
