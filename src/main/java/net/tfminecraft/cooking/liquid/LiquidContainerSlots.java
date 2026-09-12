package net.tfminecraft.cooking.liquid;

public final class LiquidContainerSlots {
    private LiquidContainerSlots() {}

    public static final String LIQUID_1 = "liquid_1";
    public static final String LIQUID_2 = "liquid_2";
    public static final String LIQUID_3 = "liquid_3";
    public static final String LIQUID_4 = "liquid_4";
    public static final String LIQUID_5 = "liquid_5";
    public static final String LIQUID_6 = "liquid_6";

    public static String slotForLevel(int level) {
        return switch (level) {
            case 1 -> LIQUID_1;
            case 2 -> LIQUID_2;
            case 3 -> LIQUID_3;
            case 4 -> LIQUID_4;
            case 5 -> LIQUID_5;
            case 6 -> LIQUID_6;
            default -> null;
        };
    }

    public static String[] all() {
        return new String[] { LIQUID_1, LIQUID_2, LIQUID_3, LIQUID_4, LIQUID_5, LIQUID_6 };
    }
}
