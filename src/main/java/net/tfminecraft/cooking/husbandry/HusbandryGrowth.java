package net.tfminecraft.cooking.husbandry;

import org.bukkit.entity.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public final class HusbandryGrowth {

    private HusbandryGrowth() {}

    public static boolean isMature(HusbandryAnimal animal, long nowMillis) {
        if (animal == null) {
            return true;
        }
        Long matureAt = animal.matureAt();
        return matureAt == null || nowMillis >= matureAt;
    }

    public static long computeMatureAt(EntityType type, long bornAtMillis) {
        return bornAtMillis + HusbandryConfig.growUpSeconds(type) * 1000L;
    }

    public static void applyMaturity(LivingEntity entity, HusbandryAnimal animal, long nowMillis) {
        if (animal == null || !isMature(animal, nowMillis)) {
            return;
        }
        if (animal.matureAt() != null) {
            animal.setMatureAt(null);
        }
        if (entity instanceof Ageable ageable && !ageable.isAdult()) {
            ageable.setAdult();
        }
    }
}
