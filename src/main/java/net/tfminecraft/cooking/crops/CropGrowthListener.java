package net.tfminecraft.cooking.crops;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;

public final class CropGrowthListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockGrow(BlockGrowEvent event) {
        if (CropGrowthGate.shouldCancelVanilla(event.getBlock().getType(), event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }
}
