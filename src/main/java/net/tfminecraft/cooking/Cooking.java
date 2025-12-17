package net.tfminecraft.cooking;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import net.tfminecraft.cooking.loader.FoodLoader;
import net.tfminecraft.cooking.loader.ModelLoader;
import net.tfminecraft.cooking.loader.TrackLoader;
import net.tfminecraft.cooking.loader.ConfigLoader;
import net.tfminecraft.cooking.loader.ConversionLoader;
import net.tfminecraft.cooking.loader.CraftingStationLoader;
import net.tfminecraft.cooking.manager.CommandManager;
import net.tfminecraft.cooking.manager.ConversionManager;
import net.tfminecraft.cooking.manager.CookingManager;
import net.tfminecraft.cooking.manager.CraftingManager;
import net.tfminecraft.cooking.manager.PlateManager;
import net.tfminecraft.cooking.manager.TagManager;

public class Cooking extends JavaPlugin {

    public static Cooking plugin;

    private final CommandManager commands = new CommandManager();

    private final ModelLoader modelLoader = new ModelLoader();
    private final FoodLoader foodLoader = new FoodLoader();
    private final CraftingStationLoader stationLoader = new CraftingStationLoader();
    private final TrackLoader trackLoader = new TrackLoader();
    private final ConfigLoader configLoader = new ConfigLoader();
    private final ConversionLoader conversionLoader = new ConversionLoader();

    private final TagManager tagManager = new TagManager();
    private final CookingManager cookingManager = new CookingManager();
    private final PlateManager plateManager = new PlateManager();

    @Override
    public void onEnable() {
        plugin = this;

        createFolders();
        createConfigs();
        loadConfigs();
        registerListeners();

        cookingManager.start();
        Bukkit.getScheduler().runTask(this, () -> {
            plateManager.start();
        });

        getCommand("cooking").setExecutor(commands);
    }

    @Override
    public void onDisable() {
        // cleanup if needed later
    }

    // ----------------------------------------------------------------------
    //  Config Loading
    // ----------------------------------------------------------------------
    public void loadConfigs() {
        configLoader.loadConfig(new File(getDataFolder(), "config.yml"));
        modelLoader.load(new File(getDataFolder(), "models.yml"));
        foodLoader.load(new File(getDataFolder(), "types.yml"));
        stationLoader.load(new File(getDataFolder(), "crafting-stations.yml"));
        trackLoader.load(new File(getDataFolder(), "tags.yml"));
        conversionLoader.load(new File(getDataFolder(), "conversions.yml"));
    }

    // ----------------------------------------------------------------------
    //  Listeners
    // ----------------------------------------------------------------------
    public void registerListeners() {
        getServer().getPluginManager().registerEvents(plateManager, this);
        getServer().getPluginManager().registerEvents(new CraftingManager(), this);
        getServer().getPluginManager().registerEvents(tagManager, this);
        getServer().getPluginManager().registerEvents(cookingManager, this);
        getServer().getPluginManager().registerEvents(new ConversionManager(), this);
    }

    // ----------------------------------------------------------------------
    //  Folders
    // ----------------------------------------------------------------------
    public void createFolders() {
        if (!getDataFolder().exists())
            getDataFolder().mkdir();

        File subFolder = new File(getDataFolder(), "Data");
        if (!subFolder.exists())
            subFolder.mkdir();
    }

    // ----------------------------------------------------------------------
    //  Config Generation
    // ----------------------------------------------------------------------
    public void createConfigs() {
        String[] files = {
                "models.yml",
                "types.yml",
                "crafting-stations.yml",
                "cookware.yml",
                "tags.yml",
                "config.yml",
                "conversions.yml"
        };

        for (String s : files) {
            File newConfigFile = new File(getDataFolder(), s);
            if (!newConfigFile.exists()) {
                newConfigFile.getParentFile().mkdirs();
                saveResource(s, false);
            }
        }
    }
}
