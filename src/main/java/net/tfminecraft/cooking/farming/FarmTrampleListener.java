package net.tfminecraft.cooking.farming;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import net.tfminecraft.cooking.Cooking;

public final class FarmTrampleListener implements Listener {

    private boolean callingPlayerInteractEvent;
    private boolean callingBlockFadeEvent;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFarmlandTrample(PlayerInteractEvent event) {
        if (callingPlayerInteractEvent) {
            return;
        }
        if (!FarmingConfig.antiTrampleEnabled()) {
            return;
        }

        Block farmland = event.getClickedBlock();
        boolean trample = farmland != null
                && farmland.getType() == Material.FARMLAND
                && event.getAction() == Action.PHYSICAL;
        if (!trample) {
            return;
        }

        Block crop = farmland.getRelative(0, 1, 0);
        if (crop.isEmpty() && FarmingConfig.dryEmptyFarmland()) {
            return;
        }
        if (!FarmingConfig.isTrampleableCrop(crop.getType())) {
            return;
        }

        event.setUseInteractedBlock(Event.Result.DENY);
        attemptCropTrample(farmland, event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWalk(PlayerMoveEvent event) {
        if (event.isCancelled() || !FarmingConfig.antiTrampleEnabled() || !FarmingConfig.trampleByWalking()) {
            return;
        }

        Player player = event.getPlayer();
        if (player.isSneaking() || event.getTo() == null) {
            return;
        }

        Block targetBlock = event.getTo().getBlock();
        Block sourceBlock = event.getFrom().getBlock();
        if (targetBlock.equals(sourceBlock) || targetBlock.getType() != Material.FARMLAND) {
            return;
        }

        attemptCropTrample(targetBlock, player);
    }

    private void attemptCropTrample(Block farmlandBlock, Player player) {
        Block crop = farmlandBlock.getRelative(0, 1, 0);
        if (!FarmingConfig.isTrampleableCrop(crop.getType())) {
            return;
        }

        PlayerInteractEvent trampleEvent = new PlayerInteractEvent(
                player,
                Action.PHYSICAL,
                null,
                crop,
                BlockFace.SELF);
        callingPlayerInteractEvent = true;
        Cooking.plugin.getServer().getPluginManager().callEvent(trampleEvent);
        callingPlayerInteractEvent = false;
        if (trampleEvent.useInteractedBlock() == Event.Result.DENY) {
            return;
        }

        BlockState oldState = crop.getState();
        BlockState state = crop.getState();
        state.setType(Material.AIR);
        state.setType(crop.getType());
        BlockState blockDataState = state;
        if (state.getBlockData() instanceof Ageable ageable) {
            ageable.setAge(0);
            blockDataState.setBlockData(ageable);
        }

        BlockFadeEvent fadeEvent = new BlockFadeEvent(crop, blockDataState);
        callingBlockFadeEvent = true;
        Cooking.plugin.getServer().getPluginManager().callEvent(fadeEvent);
        callingBlockFadeEvent = false;
        if (fadeEvent.isCancelled()) {
            return;
        }

        blockDataState.update(true);
        FarmingEffects.trampleCrop(crop, oldState, FarmingConfig.trampleParticleMultiplier());
    }
}
