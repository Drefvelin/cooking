package net.tfminecraft.cooking.baking;

public final class BakingTrayFill {
    private final String input;
    private final String inputFood;
    private final int outputsPerMold;
    private final String food;
    private final String tags;
    private final String category;

    public BakingTrayFill(String input, String inputFood, int outputsPerMold, String food, String tags,
            String category) {
        this.input = input;
        this.inputFood = inputFood;
        this.outputsPerMold = outputsPerMold;
        this.food = food;
        this.tags = tags;
        this.category = category;
    }

    public String getInput() {
        return input;
    }

    public String getInputFood() {
        return inputFood;
    }

    public int getOutputsPerMold() {
        return outputsPerMold;
    }

    public String getFood() {
        return food;
    }

    public String getTags() {
        return tags;
    }

    public String getCategory() {
        return category;
    }

    public String buildSpawnString() {
        return category + "(type=" + food + ";tags=" + tags + ")";
    }
}
