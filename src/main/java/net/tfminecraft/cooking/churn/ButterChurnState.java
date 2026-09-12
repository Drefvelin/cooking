package net.tfminecraft.cooking.churn;

import net.tfminecraft.furniture.Furniture;

public final class ButterChurnState {
    public static final String VAR_CHURN_COUNT = "butter.churnCount";
    public static final String VAR_MILK_QUALITY = "butter.milkQuality";
    public static final String VAR_DAIRY_FRESHNESS = "butter.dairyFreshness";
    public static final String VAR_LAST_UPDATE = "butter.lastUpdate";
    public static final String VAR_HAS_SALT = "butter.hasSalt";
    public static final String VAR_SALT_QUALITY = "butter.saltQuality";
    public static final String VAR_SPICE_ORIGIN = "butter.spiceOrigin";
    public static final String VAR_SPICE_QUALITY = "butter.spiceQuality";
    public static final String VAR_SPICE_FRESHNESS = "butter.spiceFreshness";

    private ButterChurnState() {}

    public static int getChurnCount(Furniture furniture) {
        Object value = furniture.getVariables().get(VAR_CHURN_COUNT);
        if (value instanceof Number number) {
            return number.intValue();
        }
        return 0;
    }

    public static int getMilkQuality(Furniture furniture) {
        Object value = furniture.getVariables().get(VAR_MILK_QUALITY);
        if (value instanceof Number number) {
            return number.intValue();
        }
        return 1;
    }

    public static int getDairyFreshness(Furniture furniture) {
        Object value = furniture.getVariables().get(VAR_DAIRY_FRESHNESS);
        if (value instanceof Number number) {
            return Math.max(0, number.intValue());
        }
        return 0;
    }

    public static long getLastUpdate(Furniture furniture) {
        Object value = furniture.getVariables().get(VAR_LAST_UPDATE);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return System.currentTimeMillis();
    }

    public static boolean hasSalt(Furniture furniture) {
        Object value = furniture.getVariables().get(VAR_HAS_SALT);
        return value instanceof Boolean bool && bool;
    }

    public static int getSaltQuality(Furniture furniture) {
        Object value = furniture.getVariables().get(VAR_SALT_QUALITY);
        if (value instanceof Number number) {
            return number.intValue();
        }
        return 1;
    }

    public static boolean hasSpice(Furniture furniture) {
        String origin = getSpiceOrigin(furniture);
        return origin != null && !origin.isBlank();
    }

    public static String getSpiceOrigin(Furniture furniture) {
        Object value = furniture.getVariables().get(VAR_SPICE_ORIGIN);
        return value instanceof String origin ? origin : null;
    }

    public static int getSpiceQuality(Furniture furniture) {
        Object value = furniture.getVariables().get(VAR_SPICE_QUALITY);
        if (value instanceof Number number) {
            return number.intValue();
        }
        return 1;
    }

    public static int getSpiceFreshness(Furniture furniture) {
        Object value = furniture.getVariables().get(VAR_SPICE_FRESHNESS);
        if (value instanceof Number number) {
            return Math.max(0, number.intValue());
        }
        return 0;
    }

    public static boolean hasExtras(Furniture furniture) {
        return hasSalt(furniture) || hasSpice(furniture);
    }

    public static void setChurnCount(Furniture furniture, int count) {
        furniture.getVariables().put(VAR_CHURN_COUNT, count);
    }

    public static void setMilkSnapshot(Furniture furniture, int quality, int dairyFreshness) {
        clearExtras(furniture);
        furniture.getVariables().put(VAR_MILK_QUALITY, quality);
        furniture.getVariables().put(VAR_DAIRY_FRESHNESS, Math.max(0, dairyFreshness));
        touchLastUpdate(furniture);
    }

    public static void setSalt(Furniture furniture, int quality) {
        furniture.getVariables().put(VAR_HAS_SALT, true);
        furniture.getVariables().put(VAR_SALT_QUALITY, quality);
    }

    public static void setSpice(Furniture furniture, String origin, int quality, int freshness) {
        furniture.getVariables().put(VAR_SPICE_ORIGIN, origin);
        furniture.getVariables().put(VAR_SPICE_QUALITY, quality);
        furniture.getVariables().put(VAR_SPICE_FRESHNESS, Math.max(0, freshness));
    }

    public static void touchLastUpdate(Furniture furniture) {
        furniture.getVariables().put(VAR_LAST_UPDATE, System.currentTimeMillis());
    }

    public static int incrementChurnCount(Furniture furniture) {
        int count = getChurnCount(furniture) + 1;
        setChurnCount(furniture, count);
        return count;
    }

    public static boolean isReady(Furniture furniture, int required) {
        return getChurnCount(furniture) >= required;
    }

    public static void tickAge(Furniture furniture) {
        if (!furniture.hasActiveSlot("input_1")) {
            return;
        }
        long now = System.currentTimeMillis();
        long last = getLastUpdate(furniture);
        long elapsedMs = now - last;
        if (elapsedMs <= 0) {
            return;
        }
        int seconds = (int) (elapsedMs / 1000L);
        if (seconds <= 0) {
            return;
        }
        furniture.getVariables().put(VAR_DAIRY_FRESHNESS, getDairyFreshness(furniture) + seconds);
        furniture.getVariables().put(VAR_LAST_UPDATE, now);
    }

    public static void clearExtras(Furniture furniture) {
        furniture.getVariables().remove(VAR_HAS_SALT);
        furniture.getVariables().remove(VAR_SALT_QUALITY);
        furniture.getVariables().remove(VAR_SPICE_ORIGIN);
        furniture.getVariables().remove(VAR_SPICE_QUALITY);
        furniture.getVariables().remove(VAR_SPICE_FRESHNESS);
    }

    public static void clear(Furniture furniture) {
        furniture.getVariables().remove(VAR_CHURN_COUNT);
        furniture.getVariables().remove(VAR_MILK_QUALITY);
        furniture.getVariables().remove(VAR_DAIRY_FRESHNESS);
        furniture.getVariables().remove(VAR_LAST_UPDATE);
        clearExtras(furniture);
    }
}
