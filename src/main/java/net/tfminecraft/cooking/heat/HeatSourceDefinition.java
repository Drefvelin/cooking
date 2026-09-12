package net.tfminecraft.cooking.heat;

public final class HeatSourceDefinition {
    private final String furnitureId;
    private final HeatSourceType type;

    public HeatSourceDefinition(String furnitureId, HeatSourceType type) {
        this.furnitureId = furnitureId;
        this.type = type;
    }

    public String getFurnitureId() {
        return furnitureId;
    }

    public HeatSourceType getType() {
        return type;
    }
}
