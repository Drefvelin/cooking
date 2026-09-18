package net.tfminecraft.cooking.crops;

import java.util.OptionalDouble;

import org.bukkit.Location;

import net.momirealms.customcrops.api.context.Context;
import net.momirealms.customcrops.api.context.ContextKeys;
import net.momirealms.customcrops.api.core.block.CropBlock;
import net.momirealms.customcrops.api.core.mechanic.crop.CropConfig;
import net.momirealms.customcrops.api.core.mechanic.crop.GrowCondition;
import net.momirealms.customcrops.api.core.world.CustomCropsBlockState;
import net.momirealms.customcrops.api.requirement.Requirement;

final class CookingFertilityGrowCondition extends GrowCondition {

    private final GrowCondition delegate;

    @SuppressWarnings("unchecked")
    CookingFertilityGrowCondition(GrowCondition delegate) {
        super(new Requirement[0], unwrap(delegate).pointToAdd());
        this.delegate = unwrap(delegate);
    }

    @Override
    public boolean isMet(Context<CustomCropsBlockState> context) {
        if (!delegate.isMet(context)) {
            return false;
        }
        return fertilityAllows(context);
    }

    static boolean fertilityAllows(Context<CustomCropsBlockState> context) {
        if (context == null) {
            return true;
        }
        CustomCropsBlockState state = context.holder();
        if (state == null || !(state.type() instanceof CropBlock cropBlock)) {
            return true;
        }
        CropConfig config = cropBlock.config(state);
        if (config == null || config.id() == null) {
            return true;
        }
        Location location = context.arg(ContextKeys.LOCATION);
        if (location == null) {
            return false;
        }
        return CropGrowthGate.allowsCustom(config.id(), location, OptionalDouble.empty());
    }

    private static GrowCondition unwrap(GrowCondition condition) {
        GrowCondition current = condition;
        while (current instanceof CookingFertilityGrowCondition wrapped) {
            current = wrapped.delegate;
        }
        return current;
    }
}
