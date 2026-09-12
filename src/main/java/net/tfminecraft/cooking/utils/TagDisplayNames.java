package net.tfminecraft.cooking.utils;

import net.tfminecraft.cooking.cache.NamingConfig;
import net.tfminecraft.cooking.item.FoodItem;
import net.tfminecraft.cooking.item.tag.TagStep;
import net.tfminecraft.cooking.item.tag.TagTrack;

public final class TagDisplayNames {
    private TagDisplayNames() {}

    public static String resolve(FoodItem item, TagTrack track, TagStep step) {
        if (step == null) {
            return "Unknown";
        }
        if (item != null && track != null) {
            String override = item.getTagLabel(track.getId(), step.getId());
            if (override != null) {
                return override;
            }
            String category = item.getCategory();
            if (category != null && !category.isBlank()) {
                String categoryDefault = NamingConfig.getTagLabelDefault(category, track.getId(), step.getId());
                if (categoryDefault != null) {
                    return categoryDefault;
                }
            }
        }
        return step.getName();
    }
}
