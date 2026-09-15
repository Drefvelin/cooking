package net.tfminecraft.cooking.husbandry;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;
import org.bukkit.persistence.PersistentDataType;

public final class HusbandryMounts {

    private HusbandryMounts() {}

    public static boolean isMount(Entity entity) {
        return entity instanceof AbstractHorse;
    }

    public static void setNerfed(Entity entity, boolean nerfed) {
        if (entity == null) {
            return;
        }
        entity.getPersistentDataContainer().set(
                HusbandryKeys.NERFED, PersistentDataType.BYTE, (byte) (nerfed ? 1 : 0));
    }

    public static boolean isNerfed(Entity entity) {
        if (entity == null) {
            return false;
        }
        Byte value = entity.getPersistentDataContainer().get(HusbandryKeys.NERFED, PersistentDataType.BYTE);
        return value != null && value == 1;
    }

    public static void setMaxHealth(LivingEntity entity, double health) {
        AttributeInstance attr = entity.getAttribute(Attribute.MAX_HEALTH);
        if (attr == null) {
            return;
        }
        attr.setBaseValue(health);
        entity.setHealth(Math.min(health, entity.getHealth()));
    }

    public static void setSpeed(LivingEntity entity, double speed) {
        AttributeInstance attr = entity.getAttribute(Attribute.MOVEMENT_SPEED);
        if (attr != null) {
            attr.setBaseValue(speed);
        }
    }

    public static void setJump(Entity entity, double jump) {
        if (entity instanceof AbstractHorse horse) {
            horse.setJumpStrength(jump);
        }
    }

    public static double maxHealth(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.MAX_HEALTH);
        return attr != null ? attr.getBaseValue() : -1;
    }

    public static double speed(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.MOVEMENT_SPEED);
        return attr != null ? attr.getBaseValue() : -1;
    }

    public static double jump(Entity entity) {
        if (entity instanceof AbstractHorse horse) {
            return horse.getJumpStrength();
        }
        return -1;
    }

    public static double breedAttribute(double parent1, double parent2, double min, double max) {
        if (max <= min) {
            return min;
        }
        double normP1 = (parent1 - min) / (max - min);
        double normP2 = (parent2 - min) / (max - min);
        double minVal = Math.min(normP1, normP2);
        double maxVal = Math.max(normP1, normP2);
        double offset = Math.pow(maxVal - minVal, 3) * 3.5;
        double adjMax = maxVal + ((1 - maxVal - offset) / 4.0);
        double adjMin = minVal - ((1 - minVal) / 5.5);
        double result = Math.random() * (adjMax - adjMin) + adjMin;
        result = Math.min(Math.max(result, 0), 1);
        return result * (max - min) + min;
    }

    public static void applyInheritedStats(LivingEntity child, LivingEntity parent1, LivingEntity parent2) {
        if (!isMount(child) || !isMount(parent1) || !isMount(parent2)) {
            return;
        }
        HusbandryMountStats stats = HusbandryConfig.mountStats(child.getType());
        if (stats == null) {
            return;
        }
        double speed = breedAttribute(
                Math.min(speed(parent1), stats.maxSpeed()),
                Math.min(speed(parent2), stats.maxSpeed()),
                stats.minSpeed(),
                stats.maxSpeed());
        setSpeed(child, speed);

        double health = breedAttribute(
                Math.min(maxHealth(parent1), stats.maxHealth()),
                Math.min(maxHealth(parent2), stats.maxHealth()),
                stats.minHealth(),
                stats.maxHealth());
        setMaxHealth(child, health);

        double jump1 = jump(parent1);
        double jump2 = jump(parent2);
        if (jump1 >= 0 && jump2 >= 0) {
            double jump = breedAttribute(
                    Math.min(jump1, stats.maxJump()),
                    Math.min(jump2, stats.maxJump()),
                    stats.minJump(),
                    stats.maxJump());
            setJump(child, jump);
        }
        if (child instanceof Tameable tameable) {
            tameable.setTamed(true);
        }
        setNerfed(child, true);
    }

    public static void nerf(AbstractHorse horse) {
        if (horse == null || isNerfed(horse) || !HusbandryConfig.mountNerf()) {
            return;
        }
        HusbandryMountStats stats = HusbandryConfig.mountStats(horse.getType());
        double divisor = Math.max(1.0, HusbandryConfig.mountNerfDivisor());
        double minHealth = stats == null ? 1 : stats.minHealth();
        double minSpeed = stats == null ? 0.1 : stats.minSpeed();
        double minJump = stats == null ? 0.4 : stats.minJump();
        double health = maxHealth(horse);
        if (health > 0) {
            setMaxHealth(horse, Math.max(minHealth, health / divisor));
        }
        double spd = speed(horse);
        if (spd > 0) {
            setSpeed(horse, Math.max(minSpeed, spd / divisor));
        }
        double jmp = jump(horse);
        if (jmp >= 0) {
            setJump(horse, Math.max(minJump, jmp / divisor));
        }
        setNerfed(horse, true);
    }

    public static int geneticsFromStats(Entity entity) {
        if (!(entity instanceof LivingEntity living) || !isMount(entity)) {
            return 0;
        }
        HusbandryMountStats stats = HusbandryConfig.mountStats(entity.getType());
        if (stats == null) {
            return 0;
        }
        double healthNorm = norm(maxHealth(living), stats.minHealth(), stats.maxHealth());
        double speedNorm = norm(speed(living), stats.minSpeed(), stats.maxSpeed());
        double jumpNorm = norm(jump(entity), stats.minJump(), stats.maxJump());
        double average = (healthNorm + speedNorm + jumpNorm) / 3.0;
        return (int) Math.round(average * HusbandryConfig.maxGenetics());
    }

    private static double norm(double value, double min, double max) {
        if (max <= min) {
            return 0;
        }
        return Math.min(1, Math.max(0, (value - min) / (max - min)));
    }

    public static double healthHearts(LivingEntity entity) {
        return maxHealth(entity) / 2.0;
    }

    public static double speedBlocksPerSecond(LivingEntity entity) {
        return speed(entity) * 42.16;
    }

    public static double jumpBlockHeight(Entity entity) {
        double strength = jump(entity);
        if (strength < 0) {
            return -1;
        }
        return -0.1817584952 * Math.pow(strength, 3)
                + 3.689713992 * Math.pow(strength, 2)
                + 2.128599134 * strength
                - 0.343930367;
    }
}
