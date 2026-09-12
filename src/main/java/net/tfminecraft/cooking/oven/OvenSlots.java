package net.tfminecraft.cooking.oven;

public final class OvenSlots {
    public static final String WOOD_1 = "wood_1";
    public static final String WOOD_2 = "wood_2";
    public static final String WOOD_3 = "wood_3";
    public static final String WOOD_4 = "wood_4";
    public static final String WOOD_5 = "wood_5";
    public static final String WOOD_6 = "wood_6";
    public static final String FIRE = "fire";

    public static final String[] FILL_ORDER = {
            WOOD_1, WOOD_2, WOOD_3, WOOD_4, WOOD_5, WOOD_6
    };

    /** Top log, middle row, bottom row — burn proceeds layer by layer downward. */
    public static final String[][] BURN_LAYERS = {
            {WOOD_6},
            {WOOD_4, WOOD_5},
            {WOOD_1, WOOD_2, WOOD_3}
    };

    private OvenSlots() {}

    public enum WoodStage {
        EMPTY('E'),
        FRESH('F'),
        BURNT('B');

        private final char code;

        WoodStage(char code) {
            this.code = code;
        }

        public char code() {
            return code;
        }

        public static WoodStage fromCode(char code) {
            for (WoodStage stage : values()) {
                if (stage.code == code) {
                    return stage;
                }
            }
            return EMPTY;
        }
    }
}
