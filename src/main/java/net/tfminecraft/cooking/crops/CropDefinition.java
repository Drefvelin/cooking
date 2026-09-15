package net.tfminecraft.cooking.crops;

import org.bukkit.Material;

public record CropDefinition(
        String id,
        String source,
        String seed,
        double affection,
        Material block) {
}
