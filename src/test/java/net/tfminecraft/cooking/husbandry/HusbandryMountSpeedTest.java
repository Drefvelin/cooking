package net.tfminecraft.cooking.husbandry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HusbandryMountSpeedTest {

    @Test
    void zeroGeneticsZeroCareIsFortyPercent() {
        double speed = HusbandryMounts.speedFor(0.3375, 0, 0, 1000, 200, 0.40, 0.30, 0.20);
        assertEquals(0.3375 * 0.40, speed, 0.0000001);
    }

    @Test
    void maxGeneticsZeroCareIsSeventyPercent() {
        double speed = HusbandryMounts.speedFor(0.3375, 1000, 0, 1000, 200, 0.40, 0.30, 0.20);
        assertEquals(0.3375 * 0.70, speed, 0.0000001);
    }

    @Test
    void maxGeneticsMaxCareIsMinPlusGeneticsPlusCare() {
        double speed = HusbandryMounts.speedFor(0.3375, 1000, 200, 1000, 200, 0.40, 0.30, 0.20);
        assertEquals(0.3375 * 0.90, speed, 0.0000001);
    }

    @Test
    void muleUsesOwnMaxNotHorseMax() {
        double horse = HusbandryMounts.speedFor(0.3375, 1000, 0, 1000, 200, 0.40, 0.30, 0.20);
        double mule = HusbandryMounts.speedFor(0.270, 1000, 0, 1000, 200, 0.40, 0.30, 0.20);
        assertEquals(0.3375 * 0.70, horse, 0.0000001);
        assertEquals(0.270 * 0.70, mule, 0.0000001);
    }
}
