package net.tfminecraft.cooking.trough;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TroughIngredientsTest {

    @Test
    void vegetableFormsAreAllowed() {
        assertTrue(TroughIngredients.isAllowedCategory("vegetable_1"));
        assertTrue(TroughIngredients.isAllowedCategory("vegetable_cut"));
        assertTrue(TroughIngredients.isAllowedCategory("vegetable_chop_only"));
        assertTrue(TroughIngredients.isAllowedCategory("vegetable_chopped"));
    }

    @Test
    void wheatFoodIsAllowedNotOtherGrain() {
        assertTrue(TroughIngredients.isWheatFood("wheat"));
        assertTrue(TroughIngredients.isWheatFood("Wheat"));
        assertFalse(TroughIngredients.isWheatFood("flour"));
        assertFalse(TroughIngredients.isWheatFood("dough"));
        assertFalse(TroughIngredients.isWheatFood(null));
    }

    @Test
    void otherCategoriesAreRejected() {
        assertFalse(TroughIngredients.isAllowedCategory("flour"));
        assertFalse(TroughIngredients.isAllowedCategory("grain"));
        assertFalse(TroughIngredients.isAllowedCategory("crop"));
        assertFalse(TroughIngredients.isAllowedCategory("meat"));
        assertFalse(TroughIngredients.isAllowedCategory("dough"));
        assertFalse(TroughIngredients.isAllowedCategory(null));
        assertFalse(TroughIngredients.isAllowedCategory(""));
    }

    @Test
    void nullFoodIsRottenForSafety() {
        assertTrue(TroughIngredients.isRotten(null));
        assertFalse(TroughIngredients.isAllowed(null));
    }
}
