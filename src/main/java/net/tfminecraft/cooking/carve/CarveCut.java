package net.tfminecraft.cooking.carve;

public class CarveCut {
    private final String output;
    private final String itemRef;
    private final int amount;
    private final double food;
    private final double nutrition;

    public static CarveCut food(String output, double food, double nutrition) {
        return new CarveCut(output, null, 1, food, nutrition);
    }

    public static CarveCut item(String itemRef, int amount, double food, double nutrition) {
        return new CarveCut(null, itemRef, amount, food, nutrition);
    }

    private CarveCut(String output, String itemRef, int amount, double food, double nutrition) {
        this.output = output;
        this.itemRef = itemRef;
        this.amount = amount;
        this.food = food;
        this.nutrition = nutrition;
    }

    public String getOutput() {
        return output;
    }

    public String getItemRef() {
        return itemRef;
    }

    public int getAmount() {
        return amount;
    }

    public double getFood() {
        return food;
    }

    public double getNutrition() {
        return nutrition;
    }

    public boolean isFoodCut() {
        return output != null && !output.isBlank();
    }

    public boolean isItemCut() {
        return itemRef != null && !itemRef.isBlank();
    }
}
