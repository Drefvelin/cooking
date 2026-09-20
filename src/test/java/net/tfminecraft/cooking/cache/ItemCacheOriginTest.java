package net.tfminecraft.cooking.cache;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ItemCacheOriginTest {

    @Test
    void blackPepperMatchesUnderscoreKey() {
        assertTrue(ItemCache.originsMatch("Black Pepper", "black_pepper"));
    }

    @Test
    void basilMatchesSameWord() {
        assertTrue(ItemCache.originsMatch("Basil", "basil"));
    }

    @Test
    void spiceLeafDoesNotMatchBasil() {
        assertFalse(ItemCache.originsMatch("Spice Leaf", "basil"));
    }

    @Test
    void blankOriginDoesNotMatch() {
        assertFalse(ItemCache.originsMatch("", "carrot"));
        assertFalse(ItemCache.originsMatch(null, "carrot"));
    }

    @Test
    void goatMatchesUnderscoreKey() {
        assertTrue(ItemCache.originsMatch("Goat", "goat"));
        assertTrue(ItemCache.originsMatch("Cow", "cow"));
        assertFalse(ItemCache.originsMatch("Goat", "milk"));
    }
}
