package net.tfminecraft.cooking.nutrition;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.tfminecraft.RPCharacters.Objects.RPCharacter;
import net.tfminecraft.RPCharacters.RPCharacters;
import net.tfminecraft.cooking.item.FoodItem;

public final class NutritionService {

    private NutritionService() {}

    public static void tryApplyEat(Player player, FoodItem food) {
        if (player == null || food == null) {
            return;
        }
        if (!Bukkit.getPluginManager().isPluginEnabled("RPCharacters")) {
            return;
        }

        RPCharacter character = RPCharacters.getActiveCharacter(player);
        if (character == null) {
            return;
        }

        int gained = (int) Math.ceil(food.getFinalFood());
        int actualGain = applyFoodGain(character, gained);
        if (actualGain <= 0) {
            return;
        }

        if (applyDietLerp(character, food, actualGain)) {
            DietTierService.checkAndNotify(player, character);
        }

        RPCharacters.getPlayerManager().savePlayer(player);
        NutritionDisplayService.sync(player, character);
        NutritionAttributeBridge.apply(player, character);
    }

    private static int applyFoodGain(RPCharacter character, int gained) {
        if (gained <= 0) {
            return 0;
        }
        if (character.getFoodValue() >= NutritionConfig.maxFood()) {
            return 0;
        }

        int priorFood = character.getFoodValue();
        int newValue = Math.min(priorFood + gained, NutritionConfig.maxFood());
        if (newValue == priorFood) {
            return 0;
        }

        character.setFoodValue(newValue);
        return newValue - priorFood;
    }

    private static boolean applyDietLerp(RPCharacter character, FoodItem food, int actualFoodGain) {
        double foodNutrition = food.getFinalNutrition();
        double currentDiet = character.getDietScore();
        double weight = actualFoodGain / (double) NutritionConfig.maxFood();
        double delta = (foodNutrition - currentDiet) * weight * NutritionConfig.lerpStepRate();
        if (Math.abs(delta) < 1e-9) {
            return false;
        }

        int newDiet = Math.round((float) (currentDiet + delta));
        if (newDiet == character.getDietScore()) {
            return false;
        }

        character.setDietScore(newDiet);
        return true;
    }
}
