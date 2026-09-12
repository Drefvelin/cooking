package net.tfminecraft.cooking.cache;

public class MixingIngredient {
    private final String input;
    private final String inputFood;
    private final String model;
    private final String output;

    public MixingIngredient(String input, String inputFood, String model, String output) {
        this.input = input;
        this.inputFood = inputFood;
        this.model = model;
        this.output = output;
    }

    public String getInput() {
        return input;
    }

    public String getInputFood() {
        return inputFood;
    }

    public String getModel() {
        return model;
    }

    public String getOutput() {
        return output;
    }
}
