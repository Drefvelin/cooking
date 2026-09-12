package net.tfminecraft.cooking.farming;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class FarmingEffects {

    private FarmingEffects() {}

    public static void breakCrop(Block block, BlockState state, int areaRadius, double multiplier) {
        if (block == null || state == null) {
            return;
        }
        int baseCount;
        if (areaRadius > 0) {
            baseCount = (10 * (2 * areaRadius + 1)) / (4 * areaRadius * areaRadius + 4 * areaRadius + 1);
        } else {
            baseCount = 10;
        }
        int count = Math.max(1, (int) (baseCount * multiplier));
        Location location = block.getLocation().add(0.5, 0.5, 0.5);
        if (location.getWorld() == null) {
            return;
        }
        location.getWorld().spawnParticle(Particle.BLOCK, location, count, 0.5, 0.5, 0.5, state.getBlockData());
        location.getWorld().playSound(location, Sound.BLOCK_CROP_BREAK, 1f, 1f);
    }

    public static void trampleCrop(Block block, BlockState state, double multiplier) {
        breakCrop(block, state, 0, multiplier);
    }

    public static void replant(Block block, double multiplier) {
        if (block == null) {
            return;
        }
        int count = Math.max(1, (int) (15 * multiplier));
        Location location = block.getLocation().add(0.5, 0.5, 0.375);
        if (location.getWorld() == null) {
            return;
        }
        location.getWorld().spawnParticle(Particle.COMPOSTER, location, count, 0.4, 0.4, 0.35);
    }

    public static void toolSwing(Player player) {
        if (!FarmingConfig.toolSwingParticle() || player == null) {
            return;
        }
        Vector direction = player.getLocation().getDirection();
        Location location = player.getEyeLocation().clone().add(direction.clone().multiply(1.5));
        if (location.getWorld() == null) {
            return;
        }
        location.getWorld().spawnParticle(Particle.SWEEP_ATTACK, location, 1);
    }
}
