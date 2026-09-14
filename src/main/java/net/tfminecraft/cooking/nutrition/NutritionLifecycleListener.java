package net.tfminecraft.cooking.nutrition;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import net.tfminecraft.RPCharacters.Objects.RPCharacter;
import net.tfminecraft.RPCharacters.lifecycle.CharacterActivatedEvent;
import net.tfminecraft.cooking.Cooking;

public final class NutritionLifecycleListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        NutritionLog.append("JOIN", player, null,
                "phase=schedule hud=" + player.getFoodLevel() + " saturation=" + player.getSaturation());
        Bukkit.getScheduler().runTaskLater(Cooking.plugin, () -> {
            NutritionLog.append("JOIN", player, null,
                    "phase=execute hud=" + player.getFoodLevel() + " saturation=" + player.getSaturation());
            NutritionDisplayService.syncFromPlayer(player, "join");
        }, 2L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCharacterActivated(CharacterActivatedEvent event) {
        Player owner = event.getOwner();
        if (owner == null) {
            return;
        }
        RPCharacter character = event.getCharacter();
        if (character == null) {
            NutritionLog.append("ACTIVATE_SKIP", owner, null, "cause=null-character");
            return;
        }
        NutritionLog.append("ACTIVATE", owner, character,
                "hud=" + owner.getFoodLevel() + " saturation=" + owner.getSaturation());
        DietTierService.seedIfAbsent(owner, character);
        NutritionDisplayService.sync(owner, character, "activate");
        NutritionAttributeBridge.apply(owner, character);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        NutritionLog.append("RESPAWN", player, null,
                "hud=" + player.getFoodLevel() + " saturation=" + player.getSaturation());
        NutritionDisplayService.syncFromPlayer(player, "respawn");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        RPCharacter character = net.tfminecraft.RPCharacters.RPCharacters.getActiveCharacter(player);
        NutritionLog.append("DEATH", player, character,
                "hud=" + player.getFoodLevel() + " saturation=" + player.getSaturation()
                + " action=preserve");
    }
}
