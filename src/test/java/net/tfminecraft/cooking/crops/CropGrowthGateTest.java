package net.tfminecraft.cooking.crops;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.OptionalDouble;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CropGrowthGateTest {

    @BeforeEach
    void wheatAndTomato() {
        CropsConfig.apply(
                Map.of(),
                Map.of(),
                Map.of(
                        "wheat", new CropDefinition(
                                "wheat",
                                CropsConfig.SOURCE_VANILLA,
                                "v.wheat_seeds",
                                0.90,
                                null),
                        "tomato", new CropDefinition(
                                "tomato",
                                CropsConfig.SOURCE_CUSTOMCROPS,
                                "ia.playbox_custom_crops:tomato_seeds",
                                0.75,
                                null)),
                true);
    }

    @Test
    void featureDisabled_allowsGrowth() {
        assertTrue(CropGrowthGate.allowsGrowth(
                0, OptionalDouble.of(0.90), false, true, randomReturning(1.0)));
    }

    @Test
    void mapInactive_allowsGrowth() {
        assertTrue(CropGrowthGate.allowsGrowth(
                0, OptionalDouble.of(0.90), true, false, randomReturning(1.0)));
    }

    @Test
    void missingAffection_allowsGrowth() {
        assertTrue(CropGrowthGate.allowsGrowth(
                0, OptionalDouble.empty(), true, true, randomReturning(1.0)));
    }

    @Test
    void fertility100_allowsGrowth() {
        assertTrue(CropGrowthGate.allowsGrowth(
                100, OptionalDouble.of(0.90), true, true, randomReturning(1.0)));
    }

    @Test
    void fertility0_deniesGrowth() {
        assertFalse(CropGrowthGate.allowsGrowth(
                0, OptionalDouble.of(0.90), true, true, randomReturning(0.0)));
    }

    @Test
    void failedRoll_deniesGrowth() {
        assertFalse(CropGrowthGate.allowsGrowth(
                50, OptionalDouble.of(0.90), true, true, randomReturning(1.0)));
    }

    @Test
    void passedRoll_allowsGrowth() {
        assertTrue(CropGrowthGate.allowsGrowth(
                50, OptionalDouble.of(0.90), true, true, randomReturning(0.0)));
    }

    @Test
    void customOverrideBeatsConfiguredAffection() {
        double roll = 0.80;
        assertFalse(CropGrowthGate.allowsGrowth(
                50, CropGrowthGate.resolveCustomAffection("tomato", OptionalDouble.empty()),
                true, true, randomReturning(roll)));
        assertTrue(CropGrowthGate.allowsGrowth(
                50, CropGrowthGate.resolveCustomAffection("tomato", OptionalDouble.of(0.10)),
                true, true, randomReturning(roll)));
    }

    @Test
    void unknownCustomCropAllowsGrowth() {
        assertTrue(CropGrowthGate.allowsGrowth(
                0,
                CropGrowthGate.resolveCustomAffection("rice", OptionalDouble.empty()),
                true,
                true,
                randomReturning(1.0)));
    }

    private static Random randomReturning(double value) {
        return new Random() {
            @Override
            public double nextDouble() {
                return value;
            }
        };
    }
}
