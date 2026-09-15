package net.tfminecraft.cooking.crops;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.tfminecraft.cooking.loader.ConversionLoader;

class CropsConfigTest {

    @BeforeEach
    void defaults() {
        ConversionLoader.conversions.clear();
        ConversionLoader.conversions.put(
                "v.wheat",
                "grain(type=wheat;origin=Wheat;tags=freshness.0)");
        ConversionLoader.conversions.put(
                "ia.tfmc_cooking:tomato",
                "vegetable(type=vegetable_1;origin=Tomato;tags=freshness.0)");
        ConversionLoader.conversions.put(
                "v.beef",
                "meat(type=roast;origin=Beef;tags=freshness.0:cooked.0)");
        ConversionLoader.conversions.put(
                "ia.tfmc_cooking:dough",
                "ingredient(type=dough;origin=Wheat;tags=freshness.0)");
        CropsConfig.apply(Map.of(), Map.of(), Map.of("wheat", new CropDefinition(
                "wheat",
                CropsConfig.SOURCE_VANILLA,
                "v.wheat_seeds",
                0.90,
                null)));
    }

    @Test
    void farmFoodMatchesFarmConversionsAndCPrefix() {
        assertTrue(CropsConfig.isFarmFood("grain(type=wheat;origin=Wheat;tags=freshness.0)"));
        assertTrue(CropsConfig.isFarmFood("c.grain(type=wheat;origin=Wheat;tags=freshness.0)"));
        assertTrue(CropsConfig.isFarmFood("vegetable(type=vegetable_1;origin=Tomato;tags=freshness.0)"));
        assertFalse(CropsConfig.isFarmFood("meat(type=roast;origin=Beef;tags=freshness.0:cooked.0)"));
        assertFalse(CropsConfig.isFarmFood("ingredient(type=dough;origin=Wheat;tags=freshness.0)"));
    }
}
