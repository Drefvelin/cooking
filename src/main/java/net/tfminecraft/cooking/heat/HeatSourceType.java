package net.tfminecraft.cooking.heat;

public enum HeatSourceType {
    OVEN;

    public static HeatSourceType fromConfig(String value) {
        if (value == null) {
            return null;
        }
        return switch (value.toLowerCase().replace('-', '_')) {
            case "oven" -> OVEN;
            default -> null;
        };
    }
}
