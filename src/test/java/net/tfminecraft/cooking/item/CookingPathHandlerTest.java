package net.tfminecraft.cooking.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import net.tfminecraft.cooking.item.tag.TagTrack;

class CookingPathHandlerTest {

    @Test
    void categoryOnlyMatchesAnyOriginAndType() {
        FoodItem beet = vegetable("vegetable_1", "Beetroot");
        assertTrue(CookingPathHandler.matches(beet, "c.vegetable"));
        assertTrue(CookingPathHandler.matches(beet, "vegetable"));
        assertFalse(CookingPathHandler.matches(beet, "c.grain"));
    }

    @Test
    void typeOnlyIgnoresOrigin() {
        FoodItem beet = vegetable("vegetable_1", "Beetroot");
        FoodItem cut = vegetable("vegetable_cut", "Beetroot");
        assertTrue(CookingPathHandler.matches(beet, "c.vegetable(type=vegetable_1)"));
        assertFalse(CookingPathHandler.matches(cut, "c.vegetable(type=vegetable_1)"));
    }

    @Test
    void originOnlyMatchesThatOrigin() {
        FoodItem beet = vegetable("vegetable_1", "Beetroot");
        FoodItem carrot = vegetable("vegetable_1", "Carrot");
        assertTrue(CookingPathHandler.matches(beet, "c.vegetable(origin=Beetroot)"));
        assertTrue(CookingPathHandler.matches(beet, "c.vegetable(origin=beetroot)"));
        assertFalse(CookingPathHandler.matches(carrot, "c.vegetable(origin=Beetroot)"));
    }

    @Test
    void omittedTagsDoNotRestrict() {
        FoodItem beet = vegetable("vegetable_1", "Beetroot");
        addTag(beet, "freshness", 2);
        addTag(beet, "cut", 1);
        assertTrue(CookingPathHandler.matches(beet, "c.vegetable(origin=Beetroot)"));
    }

    @Test
    void listedTagsAreASubset() {
        FoodItem beet = vegetable("vegetable_1", "Beetroot");
        addTag(beet, "freshness", 0);
        addTag(beet, "cut", 1);
        assertTrue(CookingPathHandler.matches(beet, "c.vegetable(tags=freshness.0)"));
        assertTrue(CookingPathHandler.matches(beet, "c.vegetable(tags=freshness.0:cut.1)"));
        assertFalse(CookingPathHandler.matches(beet, "c.vegetable(tags=freshness.1)"));
        assertFalse(CookingPathHandler.matches(beet, "c.vegetable(tags=warmth.0)"));
    }

    @Test
    void amountIsIgnored() {
        FoodItem beet = vegetable("vegetable_1", "Beetroot");
        beet.setAmount(8);
        assertTrue(CookingPathHandler.matches(beet, "c.vegetable(origin=Beetroot;amount=64)"));
        assertTrue(CookingPathHandler.matches(beet, "c.vegetable(type=vegetable_1;amount=1)"));
    }

    @Test
    void qualityMatchesWhenListed() {
        FoodItem beet = vegetable("vegetable_1", "Beetroot");
        beet.setQualityRange(3, 3);
        assertTrue(CookingPathHandler.matches(beet, "c.vegetable(quality=3)"));
        assertTrue(CookingPathHandler.matches(beet, "c.vegetable(quality=2-4)"));
        assertFalse(CookingPathHandler.matches(beet, "c.vegetable(quality=1)"));
        assertTrue(CookingPathHandler.matches(beet, "c.vegetable(origin=Beetroot)"));
    }

    @Test
    void shortPathUsesCategoryTypeOrigin() {
        FoodItem beet = vegetable("vegetable_1", "Beetroot");
        assertEquals("c.vegetable(type=vegetable_1;origin=Beetroot)", CookingPathHandler.toShortPath(beet));
    }

    private static FoodItem vegetable(String type, String origin) {
        FoodItem item = new FoodItem(type, origin, false);
        item.setCategory("vegetable");
        item.setOrigin(origin);
        item.setQualityRange(1, 1);
        return item;
    }

    private static void addTag(FoodItem item, String id, int value) {
        TagTrack track = new TagTrack(id, false, List.of());
        track.forceSetValue(value);
        item.addOrModifyTrack(track);
    }
}
