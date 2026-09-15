package net.tfminecraft.cooking.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WarmthUtilsTest {

    @Test
    void warmBelowRoomTempDoesNotExpire() {
        assertFalse(WarmthUtils.shouldExpire(19, "warm"));
        assertFalse(WarmthUtils.shouldExpire(0, "hot"));
        assertFalse(WarmthUtils.shouldExpire(10, "warm"));
    }

    @Test
    void roomTempAgeExpires() {
        assertTrue(WarmthUtils.shouldExpire(20, "warm"));
        assertTrue(WarmthUtils.shouldExpire(25, "warm"));
    }

    @Test
    void legacyColdStepExpires() {
        assertTrue(WarmthUtils.shouldExpire(10, "cold"));
        assertTrue(WarmthUtils.shouldExpire(20, "cold"));
    }
}
