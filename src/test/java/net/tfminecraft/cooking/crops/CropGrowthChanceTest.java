package net.tfminecraft.cooking.crops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

class CropGrowthChanceTest {

    @Test
    void growChance_zeroFertility_returnsZero() {
        assertEquals(0.0, CropGrowthChance.growChance(0, 0.90));
        assertEquals(0.0, CropGrowthChance.growChance(-5, 0.30));
    }

    @Test
    void growChance_fullFertility_returnsOne() {
        assertEquals(1.0, CropGrowthChance.growChance(100, 0.90));
        assertEquals(1.0, CropGrowthChance.growChance(150, 0.30));
    }

    @Test
    void growChance_midFertility_matchesReferenceWeights() {
        assertEquals(Math.pow(0.5, 0.90), CropGrowthChance.growChance(50, 0.90), 1e-9);
        assertEquals(Math.pow(0.5, 0.30), CropGrowthChance.growChance(50, 0.30), 1e-9);
    }

    @Test
    void growChance_invalidAffection_throws() {
        assertThrows(IllegalArgumentException.class, () -> CropGrowthChance.growChance(50, 0.0));
        assertThrows(IllegalArgumentException.class, () -> CropGrowthChance.growChance(50, -0.1));
        assertThrows(IllegalArgumentException.class, () -> CropGrowthChance.growChance(50, 1.1));
    }

    @Test
    void rollGrows_alwaysTrueAtFullFertility() {
        Random random = new Random(0L);
        assertTrue(CropGrowthChance.rollGrows(100, 0.90, random));
    }

    @Test
    void rollGrows_alwaysFalseAtZeroFertility() {
        Random random = new Random(0L);
        assertFalse(CropGrowthChance.rollGrows(0, 0.90, random));
    }

    @Test
    void rollGrows_respectsChance() {
        Random alwaysFail = new Random() {
            @Override
            public double nextDouble() {
                return 1.0;
            }
        };
        assertFalse(CropGrowthChance.rollGrows(50, 0.90, alwaysFail));

        Random alwaysPass = new Random() {
            @Override
            public double nextDouble() {
                return 0.0;
            }
        };
        assertTrue(CropGrowthChance.rollGrows(50, 0.90, alwaysPass));
    }
}
