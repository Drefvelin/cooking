package net.tfminecraft.cooking.crops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ProvinceFertilityRequirementTest {

    @Test
    void parseAffectionOverrideFromNumberAndString() {
        assertEquals(0.75, ProvinceFertilityRequirement.parseAffectionOverride(0.75).orElseThrow());
        assertEquals(0.10, ProvinceFertilityRequirement.parseAffectionOverride("0.10").orElseThrow());
        assertTrue(ProvinceFertilityRequirement.parseAffectionOverride(null).isEmpty());
        assertTrue(ProvinceFertilityRequirement.parseAffectionOverride("nope").isEmpty());
    }
}
