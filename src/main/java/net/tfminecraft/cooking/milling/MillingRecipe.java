package net.tfminecraft.cooking.milling;

public final class MillingRecipe {
    private final String id;
    private final String furnitureId;
    private final int inputCount;
    private final String inputFood;
    private final String inputMatcher;
    private final String vanillaFallback;
    private final String outputFood;
    private final int revolutions;
    private final int durationTicks;

    public MillingRecipe(String id, String furnitureId, int inputCount, String inputFood, String inputMatcher,
            String vanillaFallback, String outputFood, int revolutions, int durationTicks) {
        this.id = id;
        this.furnitureId = furnitureId;
        this.inputCount = inputCount;
        this.inputFood = inputFood;
        this.inputMatcher = inputMatcher;
        this.vanillaFallback = vanillaFallback;
        this.outputFood = outputFood;
        this.revolutions = revolutions;
        this.durationTicks = durationTicks;
    }

    public String getId() {
        return id;
    }

    public String getFurnitureId() {
        return furnitureId;
    }

    public int getInputCount() {
        return inputCount;
    }

    public String getInputFood() {
        return inputFood;
    }

    public String getInputMatcher() {
        return inputMatcher;
    }

    public String getVanillaFallback() {
        return vanillaFallback;
    }

    public String getOutputFood() {
        return outputFood;
    }

    public int getRevolutions() {
        return revolutions;
    }

    public int getDurationTicks() {
        return durationTicks;
    }
}
