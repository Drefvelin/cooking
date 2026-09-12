package net.tfminecraft.cooking.farming;

import org.bukkit.Material;

public final class FarmingCropDefinition {

    private final Material crop;
    private final Material seed;

    public FarmingCropDefinition(Material crop, Material seed) {
        this.crop = crop;
        this.seed = seed;
    }

    public Material crop() {
        return crop;
    }

    public Material seed() {
        return seed;
    }
}
