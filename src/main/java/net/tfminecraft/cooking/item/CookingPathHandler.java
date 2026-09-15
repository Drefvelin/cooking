package net.tfminecraft.cooking.item;

import java.util.Locale;

import org.bukkit.inventory.ItemStack;

import me.Plugins.TLibs.Objects.API.SubAPI.ItemPathHandler;
import net.tfminecraft.cooking.utils.FoodParser;
import net.tfminecraft.cooking.utils.ItemBuilder;

public final class CookingPathHandler implements ItemPathHandler {

    public static final CookingPathHandler INSTANCE = new CookingPathHandler();

    private CookingPathHandler() {}

    public static String stripPrefix(String path) {
        if (path == null) {
            return null;
        }
        String trimmed = path.trim();
        if (trimmed.length() >= 2 && trimmed.regionMatches(true, 0, "c.", 0, 2)) {
            return trimmed.substring(2);
        }
        return trimmed;
    }

    @Override
    public ItemStack create(String fullPath) {
        String rest = stripPrefix(fullPath);
        if (rest == null || rest.isBlank() || !rest.contains("(")) {
            return null;
        }
        FoodParser.Result parsed = FoodParser.parse(rest);
        if (parsed == null || parsed.template == null) {
            return null;
        }
        ItemStack stack;
        if (parsed.explicitQuality) {
            stack = ItemBuilder.buildSingle(parsed.template, null);
        } else {
            stack = ItemBuilder.buildSingleString(rest, null);
        }
        if (stack == null) {
            return null;
        }
        stack.setAmount(1);
        return stack;
    }

    @Override
    public boolean matches(ItemStack item, String fullPath) {
        FoodItem food = FoodItem.fromItem(item);
        if (food == null || fullPath == null) {
            return false;
        }
        String raw = stripPrefix(fullPath);
        if (raw == null || raw.isBlank()) {
            return false;
        }
        String category;
        String typeFilter = null;
        int paren = raw.indexOf('(');
        if (paren >= 0 && raw.endsWith(")")) {
            category = raw.substring(0, paren).trim();
            String inside = raw.substring(paren + 1, raw.length() - 1);
            typeFilter = firstTypeField(inside);
        } else {
            category = raw.trim();
        }
        if (!food.getCategory().equalsIgnoreCase(category)) {
            return false;
        }
        if (typeFilter == null || typeFilter.isBlank()) {
            return true;
        }
        return food.getId().equalsIgnoreCase(typeFilter);
    }

    static String firstTypeField(String inside) {
        if (inside == null || inside.isBlank()) {
            return null;
        }
        for (String part : inside.split(";")) {
            String piece = part.trim();
            int eq = piece.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            if (!"type".equals(piece.substring(0, eq).trim().toLowerCase(Locale.ROOT))) {
                continue;
            }
            String value = piece.substring(eq + 1).trim();
            return value.isEmpty() ? null : value;
        }
        return null;
    }
}
