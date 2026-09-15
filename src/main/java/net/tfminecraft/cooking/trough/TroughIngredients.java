package net.tfminecraft.cooking.trough;

import java.util.Locale;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.tfminecraft.cooking.item.FoodItem;
import net.tfminecraft.cooking.item.tag.TagStep;
import net.tfminecraft.cooking.item.tag.TagTrack;
import net.tfminecraft.cooking.utils.IngredientConverter;

public final class TroughIngredients {
    private TroughIngredients() {}

    public static boolean isAllowedCategory(String category) {
        if (category == null || category.isBlank()) {
            return false;
        }
        String normalized = category.toLowerCase(Locale.ROOT);
        return normalized.startsWith("vegetable");
    }

    public static boolean isWheatFood(String foodId) {
        return foodId != null && "wheat".equalsIgnoreCase(foodId);
    }

    public static boolean isRotten(FoodItem food) {
        if (food == null) {
            return true;
        }
        TagTrack freshness = food.getTagTrack("freshness");
        if (freshness == null) {
            return false;
        }
        TagStep step = freshness.getCurrentStep();
        return step != null && "rotten".equals(step.getId());
    }

    public static boolean isAllowed(FoodItem food) {
        if (food == null || isRotten(food)) {
            return false;
        }
        return isAllowedCategory(food.getCategory()) || isWheatFood(food.getId());
    }

    public static FoodItem resolveHand(Player player, ItemStack hand) {
        if (hand == null) {
            return null;
        }
        ItemStack converted = IngredientConverter.convertIfNeeded(player, hand);
        if (converted != hand) {
            player.getInventory().setItemInMainHand(converted);
            hand = converted;
        }
        return FoodItem.fromItem(hand);
    }
}
