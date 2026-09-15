package net.tfminecraft.cooking.crops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CropHarvestQualityTest {

    @BeforeEach
    void defaults() {
        CropsConfig.apply(Map.of(), Map.of(), Map.of());
    }

    @Test
    void noStarWeightIsZero() {
        for (int fertility : new int[] {0, 50, 100}) {
            for (double affection : new double[] {0.12, 0.5, 0.9, 1.0}) {
                double[] weights = CropHarvestQuality.weights(fertility, affection);
                for (int star = 1; star <= 5; star++) {
                    assertTrue(weights[star] > 0.0);
                }
            }
        }
    }

    @Test
    void barrenHighAffectionFavorsLowStarsButCanRollFive() {
        Random random = new Random(1L);
        int ones = 0;
        int fives = 0;
        for (int i = 0; i < 10_000; i++) {
            int rolled = CropHarvestQuality.roll(0, 0.9, random);
            if (rolled == 1) {
                ones++;
            } else if (rolled == 5) {
                fives++;
            }
        }
        assertTrue(ones > fives, "ones=" + ones + " fives=" + fives);
        assertTrue(fives > 0);
        double[] weights = CropHarvestQuality.weights(0, 0.9);
        assertTrue(weights[1] > weights[5]);
        assertTrue(weights[5] > 0.0);
    }

    @Test
    void fertilityOneHundredFavorsHighStars() {
        Random random = new Random(2L);
        int ones = 0;
        int fives = 0;
        for (int i = 0; i < 10_000; i++) {
            int rolled = CropHarvestQuality.roll(100, 0.9, random);
            if (rolled == 1) {
                ones++;
            } else if (rolled == 5) {
                fives++;
            }
        }
        assertTrue(fives > ones, "ones=" + ones + " fives=" + fives);
        double[] weights = CropHarvestQuality.weights(100, 0.9);
        assertTrue(weights[5] > weights[1]);
    }

    @Test
    void invalidAffectionFallsBackToHalf() {
        assertEquals(
                CropHarvestQuality.weights(0, 0.5)[1],
                CropHarvestQuality.weights(0, 0.0)[1],
                1e-9);
        assertEquals(
                CropHarvestQuality.weights(0, 0.5)[5],
                CropHarvestQuality.weights(0, 1.5)[5],
                1e-9);
    }
}
