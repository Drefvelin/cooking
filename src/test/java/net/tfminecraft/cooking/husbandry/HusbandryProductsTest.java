package net.tfminecraft.cooking.husbandry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class HusbandryProductsTest {

    @Test
    void emptyHarvestHasNoProducts() {
        assertFalse(HusbandryProducts.hasProducts(Set.of()));
        assertFalse(HusbandryProducts.hasProducts(null));
    }

    @Test
    void slaughterShowsProducts() {
        assertTrue(HusbandryProducts.hasProducts(Set.of("slaughter")));
        assertEquals(List.of("On slaughter"), HusbandryProducts.modeLines(Set.of("slaughter")));
    }

    @Test
    void chickenListsSlaughterShedAndEggs() {
        Set<String> harvest = Set.of("slaughter", "egg", "shed");
        assertTrue(HusbandryProducts.hasProducts(harvest));
        assertEquals(List.of("On slaughter", "Shed", "Eggs"), HusbandryProducts.modeLines(harvest));
    }

    @Test
    void cowListsSlaughterAndMilk() {
        assertEquals(List.of("On slaughter", "Milk"), HusbandryProducts.modeLines(Set.of("slaughter", "milk")));
    }
}
