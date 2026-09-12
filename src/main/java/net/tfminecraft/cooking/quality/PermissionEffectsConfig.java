package net.tfminecraft.cooking.quality;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PermissionEffectsConfig {
    private static List<PickupEffect> pickupEffects = List.of();
    private static List<CompositionEffect> compositionEffects = List.of();

    private PermissionEffectsConfig() {}

    public static void apply(List<PickupEffect> pickup, List<CompositionEffect> composition) {
        pickupEffects = pickup == null ? List.of() : List.copyOf(pickup);
        compositionEffects = composition == null ? List.of() : List.copyOf(composition);
    }

    public static List<PickupEffect> getPickupEffects() {
        return pickupEffects;
    }

    public static List<CompositionEffect> getCompositionEffects() {
        return compositionEffects;
    }

    public record PickupEffect(String id, String permission, int minQuality, int rollBias) {}

    public record CompositionEffect(String id, String permission, double chance, int boost) {}
}
