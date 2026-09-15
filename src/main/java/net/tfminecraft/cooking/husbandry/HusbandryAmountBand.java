package net.tfminecraft.cooking.husbandry;

public final class HusbandryAmountBand {

    private final int minGenetics;
    private final int roastCuts;
    private final int wool;

    public HusbandryAmountBand(int minGenetics, int roastCuts, int wool) {
        this.minGenetics = minGenetics;
        this.roastCuts = roastCuts;
        this.wool = wool;
    }

    public int minGenetics() {
        return minGenetics;
    }

    public int roastCuts() {
        return roastCuts;
    }

    public int wool() {
        return wool;
    }
}
