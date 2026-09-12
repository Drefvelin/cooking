package net.tfminecraft.cooking.quality;

import java.util.Map;

import net.tfminecraft.cooking.item.FoodItem;
import net.tfminecraft.cooking.item.tag.TagTrack;
import net.tfminecraft.cooking.loader.TrackLoader;

public final class CompositionFreshnessApplier {
    private CompositionFreshnessApplier() {}

    public static void applyTracks(FoodItem target, Map<String, Integer> freshnessTracks) {
        if (target == null || freshnessTracks == null || freshnessTracks.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Integer> entry : freshnessTracks.entrySet()) {
            TagTrack template = TrackLoader.getByString(entry.getKey());
            if (template == null) {
                continue;
            }
            TagTrack track = new TagTrack(template);
            track.setValue(entry.getValue());
            target.addOrModifyTrack(track);
        }
    }
}
