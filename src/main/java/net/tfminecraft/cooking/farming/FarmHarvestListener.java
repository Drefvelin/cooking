package net.tfminecraft.cooking.farming;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public final class FarmHarvestListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCropBreak(BlockBreakEvent event) {
        if (!FarmingConfig.enabled()) {
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
        FarmHarvestService.harvestArea(player, tool, hoe, block);
    }
}
