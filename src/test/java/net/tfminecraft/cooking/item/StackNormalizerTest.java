package net.tfminecraft.cooking.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import net.tfminecraft.cooking.item.tag.TagStep;
import net.tfminecraft.cooking.item.tag.TagTrack;
import net.tfminecraft.cooking.utils.StackNormalizer;

class StackNormalizerTest {

    @Test
    void freshSnapsToStepFloor() {
        FoodItem item = potatoWithFreshness(50000);
        assertTrue(StackNormalizer.needsNormalize(item));
        StackNormalizer.normalize(item);
        assertEquals(0, item.getTagTrack("freshness").getValue());
        assertFalse(StackNormalizer.needsNormalize(item));
    }

    @Test
    void staleSnapsToStepFloor() {
        FoodItem item = potatoWithFreshness(90000);
        assertTrue(StackNormalizer.needsNormalize(item));
        StackNormalizer.normalize(item);
        assertEquals(86400, item.getTagTrack("freshness").getValue());
        assertFalse(StackNormalizer.needsNormalize(item));
    }

    @Test
    void nonAgeableTracksStayPut() {
        FoodItem item = potatoWithFreshness(50000);
        TagTrack cooked = new TagTrack("cooked", false, List.of(
                new TagStep("raw", "Raw", 0, 0.4, 0.5),
                new TagStep("cooked", "Cooked", 1, 1.55, 1.25)));
        cooked.forceSetValue(1);
        item.addOrModifyTrack(cooked);
        TagTrack salted = new TagTrack("butter_salted", false, List.of(
                new TagStep("plain", "", 0, 1.0, 1.0),
                new TagStep("butter_salted", "Salted", 1, 1.0, 1.0)));
        salted.forceSetValue(1);
        item.addOrModifyTrack(salted);

        StackNormalizer.normalize(item);
        assertEquals(1, item.getTagTrack("cooked").getValue());
        assertEquals(1, item.getTagTrack("butter_salted").getValue());
        assertEquals(0, item.getTagTrack("freshness").getValue());
    }

    @Test
    void remainderCleared() {
        FoodItem item = potatoWithFreshness(0);
        item.setAgeRemainder("freshness", 0.4);
        assertTrue(item.hasAgeRemainder());
        assertTrue(StackNormalizer.needsNormalize(item));
        StackNormalizer.normalize(item);
        assertFalse(item.hasAgeRemainder());
        assertFalse(StackNormalizer.needsNormalize(item));
    }

    @Test
    void quantizedNowIsWholeSeconds() {
        assertEquals(0L, StackNormalizer.quantizedNow() % 1000L);
    }

    private static FoodItem potatoWithFreshness(int value) {
        FoodItem item = new FoodItem("vegetable_1", "Potato", true);
        TagTrack freshness = new TagTrack("freshness", true, List.of(
                new TagStep("fresh", "Fresh", 0, 1.0, 1.5),
                new TagStep("stale", "Stale", 86400, 1.0, 0.8),
                new TagStep("rotten", "Rotten", 259200, 0.6, 0.05)));
        freshness.forceSetValue(value);
        item.addOrModifyTrack(freshness);
        return item;
    }
}
