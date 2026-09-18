package net.tfminecraft.cooking.husbandry;

import java.util.Set;

import org.bukkit.entity.EntityType;

public final class HusbandrySpecies {

    private final EntityType type;
    private final Set<String> harvestModes;
    private final String slaughter;
    private final String milk;
    private final String shear;
    private final String egg;
    private final String shed;
    private final int growUpSeconds;
    private final int woolTimerSeconds;
    private final HusbandryDropTable drops;

    public HusbandrySpecies(
            EntityType type,
            Set<String> harvestModes,
            String slaughter,
            String milk,
            String shear,
            String egg,
            String shed,
            int growUpSeconds,
            int woolTimerSeconds,
            HusbandryDropTable drops) {
        this.type = type;
        this.harvestModes = Set.copyOf(harvestModes);
        this.slaughter = slaughter == null ? "" : slaughter;
        this.milk = milk == null ? "" : milk;
        this.shear = shear == null ? "" : shear;
        this.egg = egg == null ? "" : egg;
        this.shed = shed == null ? "" : shed;
        this.growUpSeconds = Math.max(0, growUpSeconds);
        this.woolTimerSeconds = Math.max(0, woolTimerSeconds);
        this.drops = drops == null ? HusbandryDropTable.empty() : drops;
    }

    public EntityType type() {
        return type;
    }

    public Set<String> harvestModes() {
        return harvestModes;
    }

    public boolean hasHarvest(String mode) {
        return mode != null && harvestModes.contains(mode.toLowerCase());
    }

    public String slaughter() {
        return slaughter;
    }

    public String milk() {
        return milk;
    }

    public String shear() {
        return shear;
    }

    public String egg() {
        return egg;
    }

    public String shed() {
        return shed;
    }

    public int growUpSeconds() {
        return growUpSeconds;
    }

    public int woolTimerSeconds() {
        return woolTimerSeconds;
    }

    public HusbandryDropTable drops() {
        return drops;
    }

    public boolean vanillaEggs() {
        return egg.isBlank() || "vanilla".equalsIgnoreCase(egg);
    }
}
