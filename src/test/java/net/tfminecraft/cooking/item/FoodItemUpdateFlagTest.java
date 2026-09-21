package net.tfminecraft.cooking.item;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FoodItemUpdateFlagTest {

    @Test
    void updateFalseCopiesAndOmitsAgeClock() {
        FoodItem item = new FoodItem("seasoning_1", "Salt", false);
        assertFalse(item.shouldUpdate());
        assertFalse(new FoodItem(item).shouldUpdate());
    }

    @Test
    void updateTrueByDefaultCopies() {
        FoodItem item = new FoodItem("fruit_1", "Apple", true);
        assertTrue(item.shouldUpdate());
        assertTrue(new FoodItem(item).shouldUpdate());
    }
}
