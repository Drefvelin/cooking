package net.tfminecraft.cooking.baking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BakingTrayRecipe {
    private final String id;
    private final String furnitureId;
    private final Map<String, List<String>> molds;
    private final BakingTrayFill fill;
    private final BakingTrayBake bake;

    public BakingTrayRecipe(String id, String furnitureId, Map<String, List<String>> molds,
            BakingTrayFill fill, BakingTrayBake bake) {
        this.id = id;
        this.furnitureId = furnitureId;
        this.molds = molds;
        this.fill = fill;
        this.bake = bake;
    }

    public String getId() {
        return id;
    }

    public String getFurnitureId() {
        return furnitureId;
    }

    public Map<String, List<String>> getMolds() {
        return molds;
    }

    public BakingTrayFill getFill() {
        return fill;
    }

    public BakingTrayBake getBake() {
        return bake;
    }

    public List<String> getAllSlotIds() {
        List<String> slots = new ArrayList<>();
        for (List<String> moldSlots : molds.values()) {
            slots.addAll(moldSlots);
        }
        return slots;
    }

    public String getMoldForSlot(String slotId) {
        for (Map.Entry<String, List<String>> entry : molds.entrySet()) {
            for (String slot : entry.getValue()) {
                if (slot.equalsIgnoreCase(slotId)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    public List<String> getMoldSlotIds(String moldId) {
        for (Map.Entry<String, List<String>> entry : molds.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(moldId)) {
                return entry.getValue();
            }
        }
        return Collections.emptyList();
    }

    public List<String> getMoldIds() {
        return new ArrayList<>(molds.keySet());
    }
}
