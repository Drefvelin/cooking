package net.tfminecraft.cooking.crops;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class CropHarvestItemsTest {

    @Test
    void convertDropLeavesNullAlone() {
        assertNull(CropHarvestItems.convertDrop(null, null, 3));
        CropDefinition crop = new CropDefinition(
                "tomato",
                CropsConfig.SOURCE_CUSTOMCROPS,
                "ia.playbox_custom_crops:tomato_seeds",
                0.75,
                null);
        assertNull(CropHarvestItems.convertDrop(null, crop, 3));
        assertNull(CropHarvestItems.rewriteCustomDrop(null, crop, 4));
    }
}
