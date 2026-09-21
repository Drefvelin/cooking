package net.tfminecraft.cooking.nutrition;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NutritionDeathFoodTest {

    @Test
    void battleKeepsCurrentFood() {
        assertEquals(20, NutritionService.foodAfterDeath(20, 80, true));
        assertEquals(150, NutritionService.foodAfterDeath(150, 80, true));
    }

    @Test
    void outsideBattleSetsRespawnFood() {
        assertEquals(80, NutritionService.foodAfterDeath(20, 80, false));
        assertEquals(80, NutritionService.foodAfterDeath(150, 80, false));
    }

    @Test
    void respawnFoodClampsToMax() {
        assertEquals(NutritionConfig.maxFood(), NutritionService.foodAfterDeath(10, 9999, false));
        assertEquals(0, NutritionService.foodAfterDeath(50, -5, false));
    }
}
