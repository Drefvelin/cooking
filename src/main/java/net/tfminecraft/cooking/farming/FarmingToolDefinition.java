package net.tfminecraft.cooking.farming;

public final class FarmingToolDefinition {

    private final String path;
    private final int radius;
    private final double qualityBonusPercent;

    public FarmingToolDefinition(String path, int radius, double qualityBonusPercent) {
        this.path = path;
        this.radius = radius;
        this.qualityBonusPercent = qualityBonusPercent;
    }

    public String path() {
        return path;
    }

    public int radius() {
        return radius;
    }

    public double qualityBonusPercent() {
        return qualityBonusPercent;
    }
}
