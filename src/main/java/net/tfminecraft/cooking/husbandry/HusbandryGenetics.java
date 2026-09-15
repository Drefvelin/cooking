package net.tfminecraft.cooking.husbandry;

import java.util.Random;

public final class HusbandryGenetics {

    private HusbandryGenetics() {}

    public static int roll(int motherGenetics, int fatherGenetics, Random random) {
        int avg = (motherGenetics + fatherGenetics) / 2;
        double divider = 10.0 * Math.max(0.0001, HusbandryConfig.geneticSlowdownDivisor());
        int variance = Math.max(
                100,
                (int) (HusbandryConfig.geneticVarianceMultiplier() * (1000 - avg / divider)));
        int rolled = avg + random.nextInt(variance);
        return Math.max(0, Math.min(HusbandryConfig.maxGenetics(), rolled));
    }
}
