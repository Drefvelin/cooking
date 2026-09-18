package net.tfminecraft.cooking.husbandry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class HusbandryWoolTimerTest {

    @Test
    void speciesOverrideBeatsDefault() {
        assertEquals(2400, HusbandryConfig.resolveWoolTimerSeconds(2400, 1200));
        assertEquals(1200, HusbandryConfig.resolveWoolTimerSeconds(0, 1200));
        assertEquals(1200, HusbandryConfig.resolveWoolTimerSeconds(-1, 1200));
    }

    @Test
    void goatWoolCooldownBlocksSlaughterWool() {
        HusbandryAnimal animal = new HusbandryAnimal(UUID.randomUUID(), "GOAT", "Test");
        animal.setWoolReadyAt(5_000L);
        assertTrue(HusbandryDropRoller.woolBlocked(animal.type(), animal, 1_000L));
        assertEquals(0, HusbandryDropRoller.hideCount(animal, 1_000L));
    }
}
