package net.tfminecraft.cooking.utils;

import net.tfminecraft.cooking.item.FoodItem;
import net.tfminecraft.cooking.item.tag.TagStep;
import net.tfminecraft.cooking.item.tag.TagTrack;
import net.tfminecraft.cooking.loader.TrackLoader;

public final class WarmthUtils {
    public static final int ROOM_TEMP_AGE = 20;

    private WarmthUtils() {}

    public static boolean applyHot(FoodItem fi) {
        TagTrack warmth = TrackLoader.getByString("warmth");
        if (warmth == null || fi == null) {
            return false;
        }
        fi.addOrModifyTrack(new TagTrack(warmth));
        return true;
    }

    public static boolean isHeated(FoodItem fi, int minCookedValue) {
        TagTrack cooked = fi.getTagTrack("cooked");
        return cooked != null && cooked.getValue() >= minCookedValue;
    }

    public static boolean shouldExpire(int value, String currentStepId) {
        return value >= ROOM_TEMP_AGE || "cold".equalsIgnoreCase(currentStepId);
    }

    public static boolean expireIfRoomTemp(FoodItem fi) {
        if (fi == null) {
            return false;
        }
        TagTrack warmth = fi.getTagTrack("warmth");
        if (warmth == null) {
            return false;
        }
        TagStep step = warmth.getCurrentStep();
        String stepId = step == null ? null : step.getId();
        if (!shouldExpire(warmth.getValue(), stepId)) {
            return false;
        }
        fi.removeTrack("warmth");
        return true;
    }
}
