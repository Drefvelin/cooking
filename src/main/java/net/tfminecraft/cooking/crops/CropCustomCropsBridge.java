package net.tfminecraft.cooking.crops;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import net.momirealms.customcrops.api.BukkitCustomCropsPlugin;
import net.momirealms.customcrops.api.core.world.CustomCropsBlockState;
import net.momirealms.customcrops.api.event.CustomCropsReloadEvent;
import net.momirealms.customcrops.api.requirement.RequirementManager;

public final class CropCustomCropsBridge implements Listener {

    private static boolean harvestListenerRegistered;

    public CropCustomCropsBridge() {}

    public static void tryRegister(JavaPlugin plugin) {
        Plugin customCrops = Bukkit.getPluginManager().getPlugin("CustomCrops");
        if (customCrops == null || !customCrops.isEnabled()) {
            return;
        }
        registerHarvestListener(plugin);
        registerFertilityRequirement();
    }

    private static void registerHarvestListener(JavaPlugin plugin) {
        if (harvestListenerRegistered || plugin == null) {
            return;
        }
        plugin.getServer().getPluginManager().registerEvents(new CropCustomCropsListener(), plugin);
        harvestListenerRegistered = true;
    }

    private static void registerFertilityRequirement() {
        try {
            RequirementManager<CustomCropsBlockState> manager = BukkitCustomCropsPlugin.getInstance()
                    .getRequirementManager(CustomCropsBlockState.class);
            manager.unregisterRequirement(ProvinceFertilityRequirement.TYPE);
            manager.unregisterRequirement(ProvinceFertilityRequirement.ALIAS);
            boolean registered = manager.registerRequirement(
                    ProvinceFertilityRequirement.FACTORY,
                    ProvinceFertilityRequirement.TYPE,
                    ProvinceFertilityRequirement.ALIAS);
            if (registered) {
                Bukkit.getLogger().info(
                        "[Cooking] Registered CustomCrops requirement: " + ProvinceFertilityRequirement.TYPE);
            }
        } catch (IllegalArgumentException | IllegalStateException | NoClassDefFoundError exception) {
            Bukkit.getLogger().warning(
                    "[Cooking] Failed to register CustomCrops fertility requirement: "
                            + exception.getMessage());
        }
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        if ("CustomCrops".equalsIgnoreCase(event.getPlugin().getName())) {
            tryRegister(net.tfminecraft.cooking.Cooking.plugin);
        }
    }

    @EventHandler
    public void onCustomCropsReload(CustomCropsReloadEvent event) {
        registerFertilityRequirement();
    }
}
