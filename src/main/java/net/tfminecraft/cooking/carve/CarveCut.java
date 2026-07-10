package net.tfminecraft.cooking.carve;

public class CarveCut {
    private final String output;
    private final double food;
    private final double nutrition;

    public CarveCut(String output, double food, double nutrition) {
        this.output = output;
        this.food = food;
        this.nutrition = nutrition;
    }

    public String getOutput() {
        return output;
    }

    public double getFood() {
        return food;
    }

    public double getNutrition() {
        return nutrition;
    }
}
