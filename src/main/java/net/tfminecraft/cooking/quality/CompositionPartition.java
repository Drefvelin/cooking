package net.tfminecraft.cooking.quality;

import java.util.ArrayList;
import java.util.List;

import net.tfminecraft.cooking.item.FoodItem;

public final class CompositionPartition {
    private final List<FoodItem> mains = new ArrayList<>();
    private final List<FoodItem> extras = new ArrayList<>();
    private final List<FoodItem> neutral = new ArrayList<>();

    public List<FoodItem> getMains() {
        return mains;
    }

    public List<FoodItem> getExtras() {
        return extras;
    }

    public List<FoodItem> getNeutral() {
        return neutral;
    }
}
