package net.tfminecraft.cooking.cup;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.tfminecraft.cooking.cache.ItemCache;
import net.tfminecraft.cooking.item.FoodItem;
import net.tfminecraft.cooking.item.tag.TagTrack;
import net.tfminecraft.cooking.utils.QualityUtils;

public final class MilkBucketSnapshot {
    private MilkBucketSnapshot() {}

    public static int readQuality(ItemStack stack) {
        return readQuality(null, stack);
    }

    public static int readQuality(Player player, ItemStack stack) {
        FoodItem food = foodFromStack(player, stack);
        if (food == null) {
            return QualityUtils.clamp(1);
        }
        return QualityUtils.clamp(food.getQualityMin());
    }

    public static int readDairyFreshness(ItemStack stack) {
        return readDairyFreshness(null, stack);
    }

    public static int readDairyFreshness(Player player, ItemStack stack) {
        FoodItem food = foodFromStack(player, stack);
        if (food == null) {
            return 0;
        }
        TagTrack track = food.getTagTrack("dairy_freshness");
        return track == null ? 0 : Math.max(0, track.getValue());
    }

    private static FoodItem foodFromStack(Player player, ItemStack stack) {
        if (stack == null || !ItemCache.isMilkBucket(stack)) {
            return null;
        }
        ItemStack converted = player == null
                ? stack
                : MilkBucketConverter.convertIfNeeded(player, stack);
        return FoodItem.fromItem(converted);
    }
}
