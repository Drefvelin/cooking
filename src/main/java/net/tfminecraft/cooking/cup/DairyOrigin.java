package net.tfminecraft.cooking.cup;

public final class DairyOrigin {
    public static final String COW = "Cow";
    public static final String GOAT = "Goat";

    private DairyOrigin() {}

    public static String orCow(String origin) {
        if (origin == null || origin.isBlank()) {
            return COW;
        }
        return origin;
    }
}
