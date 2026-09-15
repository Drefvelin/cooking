package net.tfminecraft.cooking.item.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AgeScaleTest {

    @Test
    void defaultMultiplierAddsOnePerSecond() {
        AgeScale.Scaled scaled = AgeScale.apply(0, 10, 1.0, 0);
        assertEquals(10, scaled.value());
        assertEquals(0.0, scaled.leftover(), 0.0001);
    }

    @Test
    void halfDurationDoublesAgeRate() {
        AgeScale.Scaled scaled = AgeScale.apply(0, 10, 0.5, 0);
        assertEquals(20, scaled.value());
        assertEquals(0.0, scaled.leftover(), 0.0001);
    }

    @Test
    void longerDurationAddsTenOverSeventeenSeconds() {
        AgeScale.Scaled scaled = AgeScale.apply(0, 17, 1.7, 0);
        assertEquals(10, scaled.value());
        assertEquals(17 / 1.7 - 10, scaled.leftover(), 0.0001);
    }

    @Test
    void dairyTrackMigratesToFreshnessTimesFour() {
        assertEquals("freshness", AgeScale.migrateTrackId("dairy_freshness"));
        assertEquals(1600, AgeScale.migrateTrackValue("dairy_freshness", 400));
        assertEquals("freshness", AgeScale.migrateTrackId("butter_freshness"));
        assertEquals(400, AgeScale.migrateTrackValue("butter_freshness", 400));
    }
}
