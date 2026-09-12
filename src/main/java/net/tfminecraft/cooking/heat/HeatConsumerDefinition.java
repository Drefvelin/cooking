package net.tfminecraft.cooking.heat;

public final class HeatConsumerDefinition {
    private final String furnitureId;
    private final String sourceFurnitureId;
    private final HeatLookup lookup;

    public HeatConsumerDefinition(String furnitureId, String sourceFurnitureId, HeatLookup lookup) {
        this.furnitureId = furnitureId;
        this.sourceFurnitureId = sourceFurnitureId;
        this.lookup = lookup;
    }

    public String getFurnitureId() {
        return furnitureId;
    }

    public String getSourceFurnitureId() {
        return sourceFurnitureId;
    }

    public HeatLookup getLookup() {
        return lookup;
    }
}
