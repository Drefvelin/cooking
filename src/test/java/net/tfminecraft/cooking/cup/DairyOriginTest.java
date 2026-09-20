package net.tfminecraft.cooking.cup;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DairyOriginTest {

    @Test
    void blankBecomesCow() {
        assertEquals(DairyOrigin.COW, DairyOrigin.orCow(null));
        assertEquals(DairyOrigin.COW, DairyOrigin.orCow(""));
        assertEquals(DairyOrigin.COW, DairyOrigin.orCow("   "));
    }

    @Test
    void goatAndCowSurvive() {
        assertEquals(DairyOrigin.GOAT, DairyOrigin.orCow("Goat"));
        assertEquals(DairyOrigin.COW, DairyOrigin.orCow("Cow"));
    }
}
