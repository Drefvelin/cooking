package net.tfminecraft.cooking.cache;

import net.tfminecraft.cooking.enums.Method;
import net.tfminecraft.furniture.Furniture;

public class FurnitureCache {
    public static String fryingPan;
    public static String saucePan;
    public static String pot;

    public static String butterChurn;
    public static String butterPlate;

    public static String plate;
    public static String bowl;

    public static Method getByFurniture(Furniture f) {
        if(f.getId().equalsIgnoreCase(fryingPan)) return Method.FRYING_PAN;
        if(f.getId().equalsIgnoreCase(saucePan)) return Method.SAUCEPAN;
        if(f.getId().equalsIgnoreCase(pot)) return Method.POT;
        return Method.NONE;
    }

    public static boolean isButterChurn(Furniture f) {
        return f.getId().equalsIgnoreCase(butterChurn);
    }

    public static boolean isButterPlate(Furniture f) {
        return f.getId().equalsIgnoreCase(butterPlate);
    }

    public static boolean isPlate(Furniture f) {
        return f.getId().equalsIgnoreCase(plate);
    }
    
    public static boolean isBowl(Furniture f) {
        return f.getId().equalsIgnoreCase(bowl);
    }

    public static boolean isMealHolder(Furniture f) {
        return isBowl(f) || isPlate(f);
    }

    public static boolean canCarry(Furniture f) {
        if(f.getId().equalsIgnoreCase(plate)) return true;
        if(f.getId().equalsIgnoreCase(bowl)) return true;
        if(f.getId().equalsIgnoreCase(butterPlate)) return true;
        if(getByFurniture(f) != Method.NONE) return true;
        return false;
    }
}
