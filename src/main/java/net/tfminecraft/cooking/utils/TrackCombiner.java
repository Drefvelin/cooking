package net.tfminecraft.cooking.utils;

import java.util.Map;

import net.tfminecraft.cooking.item.tag.TagTrack;
import net.tfminecraft.cooking.loader.TrackLoader;
import net.tfminecraft.cooking.item.FoodItem;

public class TrackCombiner {
    public static TagTrack getCombinedTracks(Map<FoodItem, Integer> items, String id) {
        TagTrack track = TrackLoader.getByString(id);
        if(track == null) return null;
        if(!track.isAgeable()) return track;
        int count = 0;
        int total = 0;
        for(Map.Entry<FoodItem, Integer> entry : items.entrySet()) {
            FoodItem item = entry.getKey();
            int amount = entry.getValue();
            if(item.hasTagTrack(id)) {
                count += amount;
                total += item.getTagTrack(id).getValue()*amount;
            }
        }
        if(total > 0 && count > 0) {
            track.setValue(total/count);
        }
        return track;
    }
}
