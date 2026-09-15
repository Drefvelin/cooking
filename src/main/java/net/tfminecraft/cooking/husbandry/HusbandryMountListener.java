package net.tfminecraft.cooking.husbandry;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityMountEvent;

public final class HusbandryMountListener implements Listener {

    @EventHandler
    public void onMount(EntityMountEvent event) {
        Entity mount = event.getMount();
        if (!HusbandryMounts.isMount(mount)) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!HusbandryOwnershipService.hasAnyOwner(mount.getUniqueId())) {
            return;
        }
        if (HusbandryOwnershipService.isOwner(player, mount.getUniqueId())
                || HusbandryOwnershipService.isStaff(player)) {
            return;
        }
        event.setCancelled(true);
        player.sendMessage("§cThis is not your animal.");
    }
}
