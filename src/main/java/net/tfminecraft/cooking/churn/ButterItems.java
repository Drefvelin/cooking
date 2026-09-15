package net.tfminecraft.cooking.churn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.tfminecraft.cooking.item.FoodItem;
import net.tfminecraft.cooking.item.tag.TagTrack;
import net.tfminecraft.cooking.loader.FoodLoader;
import net.tfminecraft.cooking.loader.TrackLoader;
import net.tfminecraft.cooking.quality.CompositionContext;
import net.tfminecraft.cooking.quality.CompositionFreshnessApplier;
import net.tfminecraft.cooking.quality.CompositionQualityResolver;
import net.tfminecraft.cooking.quality.CompositionResult;
import net.tfminecraft.cooking.utils.ItemBuilder;
import net.tfminecraft.cooking.utils.QualityUtils;

public final class ButterItems {
    private ButterItems() {}

    public static ItemStack fromMilkSnapshot(Player player, int milkQuality, int dairyFreshness) {
        return fromMilkSnapshot(player, milkQuality, dairyFreshness, false, 1, null, 1, 0);
    }

    public static ItemStack fromMilkSnapshot(
            Player player,
            int milkQuality,
            int dairyFreshness,
            boolean hasSalt,
            int saltQuality,
            String spiceOrigin,
            int spiceQuality,
            int spiceFreshness) {
        FoodItem milkTemplate = FoodLoader.getByString("milk_bucket");
        if (milkTemplate == null) {
            return null;
        }

        List<FoodItem> inputs = new ArrayList<>();
        inputs.add(buildMilkStub(milkQuality, dairyFreshness));

        if (hasSalt) {
            inputs.add(buildSaltStub(saltQuality));
        }

        boolean hasSpice = spiceOrigin != null && !spiceOrigin.isBlank();
        if (hasSpice) {
            inputs.add(buildSpiceStub(spiceOrigin, spiceQuality, spiceFreshness));
        }

        CompositionResult composed = CompositionQualityResolver.compose(
                player,
                inputs,
                CompositionContext.CHURN);

        FoodItem butterTemplate = FoodLoader.getByString("butter");
        if (butterTemplate == null) {
            return null;
        }

        FoodItem butter = new FoodItem(butterTemplate);
        butter.setCategory("dairy");
        butter.setOrigin("Milk");

        CompositionFreshnessApplier.applyTracks(
                butter,
                Map.of("freshness", Math.max(0, dairyFreshness)));

        if (hasSalt) {
            TagTrack salted = new TagTrack(TrackLoader.getByString("butter_salted"));
            salted.setValue(1);
            butter.addOrModifyTrack(salted);
        }
        if (hasSpice) {
            TagTrack spiced = new TagTrack(TrackLoader.getByString("butter_spiced"));
            spiced.setValue(1);
            butter.addOrModifyTrack(spiced);
        }

        return ItemBuilder.buildComposedWithQuality(butter, composed.getFinalQuality());
    }

    private static FoodItem buildMilkStub(int milkQuality, int dairyFreshness) {
        FoodItem milkTemplate = FoodLoader.getByString("milk_bucket");
        FoodItem milkStub = new FoodItem(milkTemplate);
        milkStub.setCategory("dairy");
        milkStub.setQualityRange(QualityUtils.clamp(milkQuality), QualityUtils.clamp(milkQuality));

        TagTrack freshness = new TagTrack(TrackLoader.getByString("freshness"));
        freshness.setValue(Math.max(0, dairyFreshness));
        milkStub.addOrModifyTrack(freshness);
        return milkStub;
    }

    private static FoodItem buildSaltStub(int saltQuality) {
        FoodItem template = FoodLoader.getByString("seasoning_1");
        FoodItem stub = new FoodItem(template);
        stub.setCategory("salt");
        stub.setOrigin("Salt");
        stub.setQualityRange(QualityUtils.clamp(saltQuality), QualityUtils.clamp(saltQuality));
        return stub;
    }

    private static FoodItem buildSpiceStub(String origin, int spiceQuality, int spiceFreshness) {
        FoodItem template = FoodLoader.getByString("spice_1");
        FoodItem stub = new FoodItem(template);
        stub.setCategory("spice");
        stub.setOrigin(origin);
        stub.setQualityRange(QualityUtils.clamp(spiceQuality), QualityUtils.clamp(spiceQuality));

        TagTrack freshness = new TagTrack(TrackLoader.getByString("freshness"));
        freshness.setValue(Math.max(0, spiceFreshness));
        stub.addOrModifyTrack(freshness);
        return stub;
    }
}
