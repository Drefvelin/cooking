package net.tfminecraft.cooking.cache;

import java.util.HashMap;

import org.bukkit.inventory.ItemStack;
import net.tfminecraft.cooking.item.FoodItem;

import me.Plugins.TLibs.TLibs;

public class ItemCache {
    public static String butter;
    public static String butterModel;
    public static String butterPieceModel;

    public static String water;

    public static HashMap<String, String> liquidModels = new HashMap<>();
    public static HashMap<String, String> colourMap = new HashMap<>();
    public static int liquidFallback;

    //TOOLS
    public static String ladle;
    public static String masher;

    public static boolean isLadle(ItemStack i) {
        return TLibs.getItemAPI().getChecker().checkItemWithPath(i, ladle);
    }
    public static boolean isMasher(ItemStack i) {
        return TLibs.getItemAPI().getChecker().checkItemWithPath(i, masher);
    }   

    public static boolean isButter(ItemStack i) {
        return TLibs.getItemAPI().getChecker().checkItemWithPath(i, butter);
    }

    public static boolean isWater(ItemStack i) {
        return TLibs.getItemAPI().getChecker().checkItemWithPath(i, water);
    }

    public static boolean isLiquid(ItemStack i) {
        for (String path : liquidModels.keySet()) {
            if (TLibs.getItemAPI().getChecker().checkItemWithPath(i, path)) {
                return true;
            }
        }
        return false;
    }

    public static String getColour(ItemStack i) {
        for (String path : colourMap.keySet()) {
            if(path.split("\\.")[0].equalsIgnoreCase("origin")) {
                String origin = path.split("\\.")[1];
                if(origin == null) continue;
                FoodItem fi = FoodItem.fromItem(i);
                if(fi == null) return "000000";
                return colourMap.get(path);
            }
            if (TLibs.getItemAPI().getChecker().checkItemWithPath(i, path)) {
                return colourMap.get(path);
            }
        }
        return "000000";
    }

    public static String getLiquidModel(String key) {
        return liquidModels.get(key);
    }

    public static String getLiquidModel(ItemStack i) {
        for (String path : liquidModels.keySet()) {
            if (TLibs.getItemAPI().getChecker().checkItemWithPath(i, path)) {
                return getLiquidModel(path);
            }
        }
        return null;
    }
}
