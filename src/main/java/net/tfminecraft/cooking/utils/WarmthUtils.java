package net.tfminecraft.cooking.utils;

import net.tfminecraft.cooking.item.FoodItem;
import net.tfminecraft.cooking.item.tag.TagTrack;
import net.tfminecraft.cooking.loader.TrackLoader;

public final class WarmthUtils {
    private WarmthUtils() {}

    public static boolean applyHot(FoodItem fi) {
        TagTrack warmth = TrackLoader.getByString("warmth");
        if (warmth == null || fi == null) {
            return false;
        }
        fi.addOrModifyTrack(warmth);
        return true;
    }

    public static boolean isHeated(FoodItem fi, int minCookedValue) {
        TagTrack cooked = fi.getTagTrack("cooked");
        return cooked != null && cooked.getValue() >= minCookedValue;
    }
}
