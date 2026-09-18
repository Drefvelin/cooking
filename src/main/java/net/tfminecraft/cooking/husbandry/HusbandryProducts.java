package net.tfminecraft.cooking.husbandry;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class HusbandryProducts {

    private HusbandryProducts() {}

    public static boolean hasProducts(Set<String> harvestModes) {
        if (harvestModes == null || harvestModes.isEmpty()) {
            return false;
        }
        return harvestModes.contains("slaughter")
                || harvestModes.contains("shear")
                || harvestModes.contains("shed")
                || harvestModes.contains("milk")
                || harvestModes.contains("egg");
    }

    public static List<String> modeLines(Set<String> harvestModes) {
        List<String> lines = new ArrayList<>();
        if (harvestModes == null) {
            return lines;
        }
        if (harvestModes.contains("slaughter")) {
            lines.add("On slaughter");
        }
        if (harvestModes.contains("shear")) {
            lines.add("Shear");
        }
        if (harvestModes.contains("shed")) {
            lines.add("Shed");
        }
        if (harvestModes.contains("milk")) {
            lines.add("Milk");
        }
        if (harvestModes.contains("egg")) {
            lines.add("Eggs");
        }
        return lines;
    }
}
