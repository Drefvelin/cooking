package net.tfminecraft.cooking.item;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import net.tfminecraft.cooking.cooking.PotReference;
import net.tfminecraft.cooking.item.tag.TagTrack;

class PotMashTest {

    @Test
    void mashAcceptsOnlyMashableBoiledFood() {
        assertTrue(PotReference.canMash(food(true, 3)));
        assertFalse(PotReference.canMash(food(true, 0)));
        assertFalse(PotReference.canMash(food(true, 1)));
        assertFalse(PotReference.canMash(food(true, 2)));
        assertFalse(PotReference.canMash(food(false, 3)));
        assertFalse(PotReference.canMash(null));
    }

    private static FoodItem food(boolean mashable, int cooked) {
        FoodItem item = new FoodItem("meat_red_meat", "Steak", true);
        item.setMashable(mashable);
        TagTrack track = new TagTrack("cooked", false, List.of());
        track.setValue(cooked);
        item.addOrModifyTrack(track);
        return item;
    }
}
