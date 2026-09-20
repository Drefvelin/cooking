package net.tfminecraft.cooking.farming;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public final class FarmHarvestListener implements Listener {

    private static final ThreadLocal<Boolean> HOE_HARVEST = ThreadLocal.withInitial(() -> false);

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCropBreak(BlockBreakEvent event) {
        if (!FarmingConfig.enabled()) {
            return;
        }
        if (Boolean.TRUE.equals(HOE_HARVEST.get())) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        FarmingToolDefinition hoe = FarmingToolMatcher.matchTool(tool);
        if (hoe == null) {
            return;
        }

        Block block = event.getBlock();
        FarmingCropDefinition crop = FarmingToolMatcher.cropFor(block.getType());
        if (crop == null) {
            return;
        }

        event.setCancelled(true);
        FarmingEffects.toolSwing(player);
        HOE_HARVEST.set(true);
        try {
            FarmHarvestService.harvestArea(player, tool, hoe, block);
        } finally {
            HOE_HARVEST.set(false);
        }
    }
}
