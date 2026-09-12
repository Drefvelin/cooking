package net.tfminecraft.cooking.nutrition;

public final class DietTierDefinition {

    private final String id;
    private final int minPercent;
    private final String label;

    public DietTierDefinition(String id, int minPercent, String label) {
        this.id = id;
        this.minPercent = minPercent;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public int getMinPercent() {
        return minPercent;
    }

    public String getLabel() {
        return label;
    }
}
