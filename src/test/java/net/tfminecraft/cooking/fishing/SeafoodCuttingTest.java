package net.tfminecraft.cooking.fishing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SeafoodCuttingTest {

    @BeforeEach
    void loadRules() {
        CustomFishingCatalog.load(new File("src/main/resources/custom-fishing.yml"));
    }

    @AfterEach
    void restoreRules() {
        CustomFishingCatalog.load(new File("src/main/resources/custom-fishing.yml"));
    }

    @Test
    void cutClassSelectsThePortionType() {
        assertEquals("seafood_fish_filet", SeafoodCutting.plan("fish", 45).outputType());
        assertEquals("seafood_jellyfish", SeafoodCutting.plan("jellyfish", 45).outputType());
        assertEquals("seafood_octopus", SeafoodCutting.plan("octopus", 45).outputType());
    }

    @Test
    void sizeBoundariesSplitFoodAndKeepPortionCountInRange() {
        SeafoodYield cod = SeafoodCutting.plan("fish", 45);
        assertEquals(2, cod.portionCount());
        assertEquals(5.6, cod.foodPerPortion(), 0.001);

        SeafoodYield tropical = SeafoodCutting.plan("fish", 12);
        assertEquals(1, tropical.portionCount());
        assertEquals(4.0, tropical.foodPerPortion(), 0.001);

        SeafoodYield tuna = SeafoodCutting.plan("fish", 200);
        assertEquals(6, tuna.portionCount());
        assertEquals(6.7, tuna.foodPerPortion(), 0.001);
    }

    @Test
    void missingSizeOrUnknownCutIsRefused() {
        assertNull(SeafoodCutting.plan("fish", null));
        assertNull(SeafoodCutting.plan("fish", 0));
        assertNull(SeafoodCutting.plan("soup", 45));
        assertNull(SeafoodCutting.plan(null, 45));
    }
}
