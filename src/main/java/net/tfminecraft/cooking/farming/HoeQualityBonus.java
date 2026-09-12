package net.tfminecraft.cooking.farming;

import java.util.concurrent.ThreadLocalRandom;

import net.tfminecraft.cooking.utils.QualityUtils;

public final class HoeQualityBonus {

    private HoeQualityBonus() {}

    public static int apply(int quality, double bonusPercent) {
        if (bonusPercent <= 0.0) {
            return quality;
        }
        if (ThreadLocalRandom.current().nextDouble() < bonusPercent / 100.0) {
            return QualityUtils.clamp(quality + 1);
        }
        return quality;
    }
}
