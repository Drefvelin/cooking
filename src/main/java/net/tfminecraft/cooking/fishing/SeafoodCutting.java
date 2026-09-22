package net.tfminecraft.cooking.fishing;

public final class SeafoodCutting {
    private SeafoodCutting() {}

    public static SeafoodYield plan(String cut, Integer sizeCm) {
        if (sizeCm == null || sizeCm < 1 || cut == null || cut.isBlank()) {
            return null;
        }
        CutRule rule = CustomFishingCatalog.cutRule(cut);
        if (rule == null) {
            return null;
        }
        double total = sizeCm * rule.foodPerCm();
        total = Math.max(rule.minTotal(), Math.min(rule.maxTotal(), total));
        int portions = (int) Math.ceil(sizeCm / rule.cmPerPortion());
        portions = Math.max(1, Math.min(rule.maxPortions(), portions));
        double each = Math.round((total / portions) * 10.0) / 10.0;
        return new SeafoodYield(rule.outputType(), portions, each);
    }
}
