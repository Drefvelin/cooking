package net.tfminecraft.cooking.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import net.tfminecraft.cooking.item.tag.TagStep;
import net.tfminecraft.cooking.item.tag.TagTrack;

class ItemLoreRebuildTest {

    @Test
    void emptyLoreNeedsRebuild() {
        assertTrue(ItemUpdater.needsLoreRebuild(null, true));
        assertTrue(ItemUpdater.needsLoreRebuild(List.of(), true));
    }

    @Test
    void missingIndexNeedsRebuild() {
        assertTrue(ItemUpdater.needsLoreRebuild(List.of("Dairy", "Nutrition 6", "Food 10"), false));
    }

    @Test
    void nameOnlyLoreNeedsRebuild() {
        assertTrue(ItemUpdater.needsLoreRebuild(List.of("Cup of Milk"), true));
    }

    @Test
    void completeLoreDoesNotNeedRebuild() {
        assertFalse(ItemUpdater.needsLoreRebuild(
                List.of("Dairy", "Origin: Milk", "stars", "Nutrition 6.0", "Food 10.0", " ", "Fresh"),
                true));
    }

    @Test
    void raisingFreshnessThresholdsKeepsFullLore() {
        TagTrack freshness = new TagTrack("freshness", true, List.of(
                new TagStep("fresh", "Fresh", 0, 1.0, 1.5),
                new TagStep("stale", "Stale", 86400, 1.0, 0.8),
                new TagStep("rotten", "Rotten", 259200, 0.6, 0.05)));
        freshness.forceSetValue(3200);
        assertEquals("fresh", freshness.getCurrentStep().getId());

        Map<String, Integer> indexMap = new HashMap<>();
        List<String> lore = ItemBuilder.assembleLore(
                "Dairy",
                "Origin: Milk",
                3,
                "Nutrition 6.0",
                "Food 10.0",
                List.of(),
                null,
                null,
                List.of(freshness),
                indexMap);

        assertTrue(lore.size() > 3);
        assertTrue(lore.stream().anyMatch(line -> line.contains("Nutrition")));
        assertTrue(lore.stream().anyMatch(line -> line.contains("Food") && !line.contains("% Food")));
        assertTrue(lore.stream().anyMatch(line -> line.contains("Fresh")));
        assertFalse(lore.stream().anyMatch(line -> line.contains("Rotten")));
        assertEquals(Integer.valueOf(3), indexMap.get("nutrition"));
        assertEquals(Integer.valueOf(4), indexMap.get("food"));
        assertFalse(ItemUpdater.needsLoreRebuild(lore, true));
    }

    @Test
    void heldSilentDoesNotWrite() {
        assertFalse(ItemUpdater.shouldWriteToSlot(true, false, true));
    }

    @Test
    void heldVisualWrites() {
        assertTrue(ItemUpdater.shouldWriteToSlot(true, true, false));
        assertTrue(ItemUpdater.shouldWriteToSlot(true, true, true));
    }

    @Test
    void notHeldSilentWrites() {
        assertTrue(ItemUpdater.shouldWriteToSlot(false, false, true));
    }

    @Test
    void nothingToWriteSkips() {
        assertFalse(ItemUpdater.shouldWriteToSlot(false, false, false));
        assertFalse(ItemUpdater.shouldWriteToSlot(true, false, false));
    }
}
