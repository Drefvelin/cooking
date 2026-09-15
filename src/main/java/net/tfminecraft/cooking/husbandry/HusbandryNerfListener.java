package net.tfminecraft.cooking.husbandry;

import org.bukkit.entity.AbstractHorse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public final class HusbandryNerfListener implements Listener {

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {
        if (!HusbandryConfig.mountNerf()) {
            return;
        }
        if (!(event.getEntity() instanceof AbstractHorse horse)) {
            return;
        }
        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();
        if (reason == CreatureSpawnEvent.SpawnReason.NATURAL
                || reason == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) {
            HusbandryMounts.nerf(horse);
        }
    }
}
