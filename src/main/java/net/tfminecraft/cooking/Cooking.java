package net.tfminecraft.cooking;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import net.tfminecraft.cooking.loader.FoodLoader;
import net.tfminecraft.cooking.loader.ModelLoader;
import net.tfminecraft.cooking.loader.TrackLoader;
import net.tfminecraft.cooking.loader.ConfigLoader;
import net.tfminecraft.cooking.loader.ConversionLoader;
import net.tfminecraft.cooking.loader.CarveSequenceLoader;
import net.tfminecraft.cooking.loader.CraftingStationLoader;
import net.tfminecraft.cooking.loader.NamingLoader;
import net.tfminecraft.cooking.farming.FarmHarvestListener;
import net.tfminecraft.cooking.farming.FarmTrampleListener;
import net.tfminecraft.cooking.farming.FarmingLoader;
import net.tfminecraft.cooking.loader.PermissionEffectsLoader;
import net.tfminecraft.cooking.loader.CompositionConfigLoader;
import net.tfminecraft.cooking.loader.QualityConfigLoader;
import net.tfminecraft.cooking.manager.CommandManager;
import net.tfminecraft.cooking.manager.ConversionManager;
import net.tfminecraft.cooking.manager.CookingManager;
import net.tfminecraft.cooking.manager.CraftingManager;
import net.tfminecraft.cooking.manager.PlateManager;
import net.tfminecraft.cooking.manager.TagManager;
import net.tfminecraft.cooking.baking.BakingTrayAging;
import net.tfminecraft.cooking.baking.BakingTrayHandler;
import net.tfminecraft.cooking.baking.BakingTrayLoader;
import net.tfminecraft.cooking.milling.MillingRecipeLoader;
import net.tfminecraft.cooking.churn.ButterChurnAging;
import net.tfminecraft.cooking.churn.ButterChurnHandler;
import net.tfminecraft.cooking.cup.DrinkConsumeListener;
import net.tfminecraft.cooking.cup.MilkBucketConverter;
import net.tfminecraft.cooking.liquid.LiquidContainerAging;
import net.tfminecraft.cooking.liquid.LiquidContainerHandler;
import net.tfminecraft.cooking.hook.MeatHookHandler;
import net.tfminecraft.cooking.sausagemaker.SausageMakerHandler;
import net.tfminecraft.cooking.milling.MillingStoneHandler;
import net.tfminecraft.cooking.mixing.MixingBowlHandler;
import net.tfminecraft.cooking.heat.HeatPickupGuard;
import net.tfminecraft.cooking.nutrition.BowlEatHandler;
import net.tfminecraft.cooking.nutrition.FoodConsumeListener;
import net.tfminecraft.cooking.nutrition.FoodLevelChangeGuard;
import net.tfminecraft.cooking.nutrition.NutritionDrainTask;
import net.tfminecraft.cooking.nutrition.NutritionLifecycleListener;
import net.tfminecraft.cooking.nutrition.RegenBlocker;
import net.tfminecraft.cooking.nutrition.SaturationGuard;
import net.tfminecraft.cooking.oven.OvenBurnManager;
import net.tfminecraft.cooking.oven.OvenCavityHandler;
import net.tfminecraft.cooking.oven.OvenCavityManager;
import net.tfminecraft.cooking.oven.OvenHandler;
import net.tfminecraft.cooking.oven.OvenLifecycleHandler;
import net.tfminecraft.tfmccore.itemscan.ItemScanService;

public class Cooking extends JavaPlugin {

    public static Cooking plugin;

    private final CommandManager commands = new CommandManager();

    private final ModelLoader modelLoader = new ModelLoader();
    private final FoodLoader foodLoader = new FoodLoader();
    private final CraftingStationLoader stationLoader = new CraftingStationLoader();
    private final TrackLoader trackLoader = new TrackLoader();
    private final ConfigLoader configLoader = new ConfigLoader();
    private final ConversionLoader conversionLoader = new ConversionLoader();
    private final CarveSequenceLoader carveSequenceLoader = new CarveSequenceLoader();
    private final NamingLoader namingLoader = new NamingLoader();
    private final QualityConfigLoader qualityConfigLoader = new QualityConfigLoader();
    private final CompositionConfigLoader compositionConfigLoader = new CompositionConfigLoader();
    private final PermissionEffectsLoader permissionEffectsLoader = new PermissionEffectsLoader();
    private final FarmingLoader farmingLoader = new FarmingLoader();

    private final TagManager tagManager = new TagManager();
    private final CookingManager cookingManager = new CookingManager();
    private final PlateManager plateManager = new PlateManager();
    private final MixingBowlHandler mixingBowlHandler = new MixingBowlHandler();
    private final ButterChurnHandler butterChurnHandler = new ButterChurnHandler();
    private final LiquidContainerHandler liquidContainerHandler = new LiquidContainerHandler();
    private final MeatHookHandler meatHookHandler = new MeatHookHandler();
    private final SausageMakerHandler sausageMakerHandler = new SausageMakerHandler();
    private final BakingTrayLoader bakingTrayLoader = new BakingTrayLoader();
    private final MillingRecipeLoader millingRecipeLoader = new MillingRecipeLoader();
    private final BakingTrayHandler bakingTrayHandler = new BakingTrayHandler();
    private final MillingStoneHandler millingStoneHandler = new MillingStoneHandler();
    private final BakingTrayAging bakingTrayAging = new BakingTrayAging();
    private final OvenCavityManager ovenCavityManager = new OvenCavityManager();
    private final OvenBurnManager ovenBurnManager = new OvenBurnManager();
    private final OvenLifecycleHandler ovenLifecycleHandler = new OvenLifecycleHandler(ovenBurnManager);
    private final OvenHandler ovenHandler = new OvenHandler(ovenBurnManager);
    private final CraftingManager craftingManager = new CraftingManager();

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
            meatHookHandler.start();
            bakingTrayAging.start();
            ovenCavityManager.start();
            ovenLifecycleHandler.resumeLoadedOvens();
            craftingManager.resumeLoadedStations();
            cookingManager.resumeLoadedStations();
            butterChurnHandler.resumeLoadedChurns();
            liquidContainerHandler.resumeLoadedContainers();
            meatHookHandler.resumeLoadedHooks();
            SaturationGuard.start();
            NutritionDrainTask.start();
        });

        getCommand("cooking").setExecutor(commands);
        if (ItemScanService.get() != null) {
            ItemScanService.get().subscribe(tagManager);
        }
    }

    @Override
    public void onDisable() {
        SaturationGuard.stop();
        NutritionDrainTask.stop();
        ovenBurnManager.stopAll();
        LiquidContainerAging.stopAll();
        ButterChurnAging.stopAll();
        if (ItemScanService.get() != null) {
            ItemScanService.get().unsubscribe(tagManager);
        }
    }

    // ----------------------------------------------------------------------
    //  Config Loading
    // ----------------------------------------------------------------------
    public void loadConfigs() {
        configLoader.loadConfig(new File(getDataFolder(), "config.yml"));
        modelLoader.load(new File(getDataFolder(), "models.yml"));
        foodLoader.load(new File(getDataFolder(), "types.yml"));
        bakingTrayLoader.load(new File(getDataFolder(), "baking-trays.yml"));
        millingRecipeLoader.load(new File(getDataFolder(), "milling-recipes.yml"));
        stationLoader.load(new File(getDataFolder(), "crafting-stations.yml"));
        trackLoader.load(new File(getDataFolder(), "tags.yml"));
        namingLoader.load(new File(getDataFolder(), "naming.yml"));
        qualityConfigLoader.load(new File(getDataFolder(), "quality.yml"));
        compositionConfigLoader.load(new File(getDataFolder(), "composition.yml"));
        permissionEffectsLoader.load(new File(getDataFolder(), "permission_effects.yml"));
        conversionLoader.load(new File(getDataFolder(), "conversions.yml"));
        farmingLoader.load(new File(getDataFolder(), "farming.yml"));
        carveSequenceLoader.load(new File(getDataFolder(), "carve-sequences.yml"));
    }

    // ----------------------------------------------------------------------
    //  Listeners
    // ----------------------------------------------------------------------
    public void registerListeners() {
        getServer().getPluginManager().registerEvents(plateManager, this);
        getServer().getPluginManager().registerEvents(craftingManager, this);
        getServer().getPluginManager().registerEvents(cookingManager, this);
        getServer().getPluginManager().registerEvents(mixingBowlHandler, this);
        getServer().getPluginManager().registerEvents(butterChurnHandler, this);
        getServer().getPluginManager().registerEvents(meatHookHandler, this);
        getServer().getPluginManager().registerEvents(sausageMakerHandler, this);
        getServer().getPluginManager().registerEvents(bakingTrayHandler, this);
        getServer().getPluginManager().registerEvents(millingStoneHandler, this);
        getServer().getPluginManager().registerEvents(ovenLifecycleHandler, this);
        getServer().getPluginManager().registerEvents(ovenHandler, this);
        getServer().getPluginManager().registerEvents(new OvenCavityHandler(), this);
        getServer().getPluginManager().registerEvents(new HeatPickupGuard(), this);
        getServer().getPluginManager().registerEvents(new FarmHarvestListener(), this);
        getServer().getPluginManager().registerEvents(new FarmTrampleListener(), this);
        getServer().getPluginManager().registerEvents(new ConversionManager(), this);
        getServer().getPluginManager().registerEvents(new RegenBlocker(), this);
        getServer().getPluginManager().registerEvents(new SaturationGuard(), this);
        getServer().getPluginManager().registerEvents(new NutritionLifecycleListener(), this);
        getServer().getPluginManager().registerEvents(new FoodLevelChangeGuard(), this);
        getServer().getPluginManager().registerEvents(new FoodConsumeListener(), this);
        getServer().getPluginManager().registerEvents(new DrinkConsumeListener(), this);
        getServer().getPluginManager().registerEvents(new BowlEatHandler(plateManager), this);
        getServer().getPluginManager().registerEvents(new MilkBucketConverter(), this);
        getServer().getPluginManager().registerEvents(liquidContainerHandler, this);
    }

    public OvenBurnManager getOvenBurnManager() {
        return ovenBurnManager;
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
                "baking-trays.yml",
                "milling-recipes.yml",
                "cookware.yml",
                "tags.yml",
                "naming.yml",
                "config.yml",
                "conversions.yml",
                "carve-sequences.yml",
                "quality.yml",
                "composition.yml",
                "permission_effects.yml",
                "farming.yml"
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
