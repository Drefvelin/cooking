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
        Bukkit.getScheduler().runTaskLater(Cooking.plugin, () -> NutritionDisplayService.syncFromPlayer(player), 2L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCharacterActivated(CharacterActivatedEvent event) {
        Player owner = event.getOwner();
        if (owner == null) {
            return;
        }
        RPCharacter character = event.getCharacter();
        if (character == null) {
            return;
        }
        DietTierService.seedIfAbsent(owner, character);
        NutritionDisplayService.sync(owner, character);
        NutritionAttributeBridge.apply(owner, character);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        NutritionDisplayService.syncFromPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Diet and food pool persist on death; no nutrition reset here.
    }
}
