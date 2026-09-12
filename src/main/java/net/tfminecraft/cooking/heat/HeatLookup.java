package net.tfminecraft.cooking.heat;

public enum HeatLookup {
    BLOCK_BELOW;

    public static HeatLookup fromConfig(String value) {
        if (value == null) {
            return null;
        }
        return switch (value.toLowerCase().replace('-', '_')) {
            case "block_below" -> BLOCK_BELOW;
            default -> null;
        };
    }
}
