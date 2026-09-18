package net.tfminecraft.cooking.crops;

import java.lang.reflect.Field;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import net.momirealms.customcrops.api.core.Registries;
import net.momirealms.customcrops.api.core.mechanic.crop.CropConfig;
import net.momirealms.customcrops.api.core.mechanic.crop.GrowCondition;
import net.momirealms.customcrops.api.event.CustomCropsReloadEvent;
import net.momirealms.customcrops.api.requirement.Requirement;
import net.tfminecraft.cooking.Cooking;

public final class CropCustomCropsBridge implements Listener {

    private static boolean harvestListenerRegistered;

    public CropCustomCropsBridge() {}

    public static void tryRegister(JavaPlugin plugin) {
        Plugin customCrops = Bukkit.getPluginManager().getPlugin("CustomCrops");
        if (customCrops == null || !customCrops.isEnabled()) {
            return;
        }
        registerHarvestListener(plugin);
        scheduleInjectFertility();
    }

    private static void registerHarvestListener(JavaPlugin plugin) {
        if (harvestListenerRegistered || plugin == null) {
            return;
        }
        plugin.getServer().getPluginManager().registerEvents(new CropCustomCropsListener(), plugin);
        harvestListenerRegistered = true;
    }

    private static void scheduleInjectFertility() {
        if (Cooking.plugin == null) {
            injectFertility();
            return;
        }
        Cooking.plugin.getServer().getScheduler().runTask(Cooking.plugin, CropCustomCropsBridge::injectFertility);
    }

    static void injectFertility() {
        try {
            for (CropConfig config : Registries.CROP) {
                wrapGrowConditions(config);
            }
        } catch (NoClassDefFoundError | IllegalStateException exception) {
            Logger logger = Bukkit.getLogger();
            logger.warning("[Cooking] Failed to inject CustomCrops fertility gate: " + exception.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static void wrapGrowConditions(CropConfig config) {
        if (config == null) {
            return;
        }
        GrowCondition[] conditions = config.growConditions();
        if (conditions == null || conditions.length == 0) {
            GrowCondition always = new GrowCondition(new Requirement[0], 1);
            replaceGrowConditions(config, new GrowCondition[] { new CookingFertilityGrowCondition(always) });
            return;
        }
        for (int i = 0; i < conditions.length; i++) {
            GrowCondition current = conditions[i];
            if (current == null || current instanceof CookingFertilityGrowCondition) {
                continue;
            }
            conditions[i] = new CookingFertilityGrowCondition(current);
        }
    }

    private static void replaceGrowConditions(CropConfig config, GrowCondition[] next) {
        try {
            Field field = config.getClass().getDeclaredField("growConditions");
            field.setAccessible(true);
            field.set(config, next);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        if ("CustomCrops".equalsIgnoreCase(event.getPlugin().getName())) {
            tryRegister(Cooking.plugin);
        }
    }

    @EventHandler
    public void onCustomCropsReload(CustomCropsReloadEvent event) {
        scheduleInjectFertility();
    }
}
