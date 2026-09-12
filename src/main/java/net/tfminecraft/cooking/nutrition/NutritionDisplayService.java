package net.tfminecraft.cooking.nutrition;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.tfminecraft.RPCharacters.Objects.RPCharacter;
import net.tfminecraft.RPCharacters.RPCharacters;

public final class NutritionDisplayService {

    private static final Set<UUID> syncingPlayers = new HashSet<>();

    private NutritionDisplayService() {}

    public static int toFoodLevel(int foodValue) {
        int max = NutritionConfig.maxFood();
        int clamped = Math.max(0, Math.min(foodValue, max));
        double scale = max / 20.0;
        return Math.min(20, Math.max(0, (int) Math.round(clamped / scale)));
    }

    public static boolean isSyncing(Player player) {
        return player != null && syncingPlayers.contains(player.getUniqueId());
    }

    public static void sync(Player player, RPCharacter character) {
        if (player == null || character == null) {
            return;
        }
        UUID id = player.getUniqueId();
        syncingPlayers.add(id);
        try {
            player.setFoodLevel(toFoodLevel(character.getFoodValue()));
            player.setSaturation(0f);
        } finally {
            syncingPlayers.remove(id);
        }
    }

    public static void syncFromPlayer(Player player) {
        if (player == null || !Bukkit.getPluginManager().isPluginEnabled("RPCharacters")) {
            return;
        }
        RPCharacter character = RPCharacters.getActiveCharacter(player);
        if (character == null) {
            return;
        }
        sync(player, character);
    }
}
