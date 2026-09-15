package net.tfminecraft.cooking.crops;

import java.util.List;
import java.util.OptionalDouble;

import org.bukkit.Location;

import net.momirealms.customcrops.api.action.Action;
import net.momirealms.customcrops.api.context.Context;
import net.momirealms.customcrops.api.context.ContextKeys;
import net.momirealms.customcrops.api.core.block.CropBlock;
import net.momirealms.customcrops.api.core.world.CustomCropsBlockState;
import net.momirealms.customcrops.api.requirement.Requirement;
import net.momirealms.customcrops.api.requirement.RequirementFactory;

public final class ProvinceFertilityRequirement {

    public static final String TYPE = "province-fertility";
    public static final String ALIAS = "simplefactions-fertility";

    public static final RequirementFactory FACTORY = ProvinceFertilityRequirement::create;

    private ProvinceFertilityRequirement() {}

    private static Requirement create(Object args, List<Action<?>> notSatisfiedActions, boolean runActions) {
        OptionalDouble affectionOverride = parseAffectionOverride(args);
        return context -> evaluate(castContext(context), affectionOverride, notSatisfiedActions, runActions);
    }

    @SuppressWarnings("unchecked")
    private static Context<CustomCropsBlockState> castContext(Context<?> context) {
        return (Context<CustomCropsBlockState>) context;
    }

    private static boolean evaluate(
            Context<CustomCropsBlockState> context,
            OptionalDouble affectionOverride,
            List<Action<?>> notSatisfiedActions,
            boolean runActions) {
        CustomCropsBlockState state = context.holder();
        if (state == null) {
            return true;
        }
        if (!(state.type() instanceof CropBlock cropBlock)) {
            return true;
        }
        Location location = context.arg(ContextKeys.LOCATION);
        if (location == null) {
            return false;
        }
        String cropId = cropBlock.config(state).id();
        boolean allowed = CropGrowthGate.allowsCustom(cropId, location, affectionOverride);
        if (!allowed && runActions && notSatisfiedActions != null) {
            for (Action<CustomCropsBlockState> action : castActions(notSatisfiedActions)) {
                action.trigger(context);
            }
        }
        return allowed;
    }

    @SuppressWarnings("unchecked")
    private static List<Action<CustomCropsBlockState>> castActions(List<Action<?>> actions) {
        return (List<Action<CustomCropsBlockState>>) (List<?>) actions;
    }

    static OptionalDouble parseAffectionOverride(Object args) {
        if (args == null) {
            return OptionalDouble.empty();
        }
        if (args instanceof Number number) {
            return OptionalDouble.of(number.doubleValue());
        }
        if (args instanceof String text) {
            try {
                return OptionalDouble.of(Double.parseDouble(text.trim()));
            } catch (NumberFormatException ignored) {
                return OptionalDouble.empty();
            }
        }
        return OptionalDouble.empty();
    }
}
