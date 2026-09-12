package net.tfminecraft.cooking.churn;

import org.bukkit.inventory.ItemStack;

import net.tfminecraft.cooking.item.FoodItem;
import net.tfminecraft.cooking.item.tag.TagTrack;

public final class ChurnExtras {
    private ChurnExtras() {}

    public static boolean isChurnSalt(ItemStack stack) {
        FoodItem food = FoodItem.fromItem(stack);
        return food != null && "salt".equalsIgnoreCase(food.getCategory());
    }

    public static boolean isChurnSpice(ItemStack stack) {
        FoodItem food = FoodItem.fromItem(stack);
        return food != null && "spice".equalsIgnoreCase(food.getCategory());
    }

    public static int readFreshness(FoodItem food) {
        if (food == null) {
            return 0;
        }
        TagTrack track = food.getTagTrack("freshness");
        return track == null ? 0 : Math.max(0, track.getValue());
    }
}
