package net.tfminecraft.cooking.sausagemaker;

import net.tfminecraft.cooking.item.FoodItem;

public final class SausageMeatRules {

    private SausageMeatRules() {}

    public static boolean isMeat(FoodItem item) {
        if (item == null) {
            return false;
        }
        String type = item.getId();
        if (type == null) {
            return false;
        }
        return "roast".equalsIgnoreCase(type) || type.toLowerCase().startsWith("meat_");
    }
}
