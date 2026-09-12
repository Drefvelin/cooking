package net.tfminecraft.cooking.nutrition;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.tfminecraft.RPCharacters.Objects.RPCharacter;
import net.tfminecraft.RPCharacters.RPCharacters;
import net.tfminecraft.cooking.Cooking;

public final class NutritionDrainTask {

    private static int taskId = -1;

    private NutritionDrainTask() {}

    public static void start() {
        if (taskId != -1) {
            return;
        }
        long interval = NutritionConfig.drainIntervalTicks();
        taskId = Bukkit.getScheduler().runTaskTimer(Cooking.plugin, NutritionDrainTask::tick, interval, interval).getTaskId();
    }

    public static void stop() {
        if (taskId == -1) {
            return;
        }
        Bukkit.getScheduler().cancelTask(taskId);
        taskId = -1;
    }

    private static void tick() {
        if (!Bukkit.getPluginManager().isPluginEnabled("RPCharacters")) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            RPCharacter character = RPCharacters.getActiveCharacter(player);
            if (character == null) {
                continue;
            }

            int currentFood = character.getFoodValue();
            if (currentFood <= 0) {
                continue;
            }

            int newValue = Math.max(0, currentFood - NutritionConfig.drainAmount());
            if (newValue == currentFood) {
                continue;
            }

            character.setFoodValue(newValue);
            NutritionDisplayService.sync(player, character);
            RPCharacters.getPlayerManager().savePlayer(player);
        }
    }
}
