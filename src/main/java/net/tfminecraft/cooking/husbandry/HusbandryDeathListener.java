package net.tfminecraft.cooking.husbandry;

import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public final class HusbandryDeathListener implements Listener {

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        HusbandryRepository repository = HusbandryEntities.repository();
        if (repository == null) {
            return;
        }

        if (HusbandryConfig.isRemoveUnowned(entity.getType())
                && !HusbandryOwnershipService.hasAnyOwner(entity.getUniqueId())) {
            event.getDrops().clear();
            event.setDroppedExp(0);
            repository.deleteAnimal(entity.getUniqueId());
            HusbandryEntities.untrack(entity.getUniqueId());
            HusbandryStateDisplay.removeAll(entity);
            return;
        }

        Optional<HusbandryAnimal> stored = repository.getAnimal(entity.getUniqueId());
        if (stored.isEmpty()) {
            return;
        }
        HusbandryAnimal animal = stored.get();
        HusbandrySpecies species = HusbandryConfig.species(entity.getType());
        long now = System.currentTimeMillis();
        if (species != null && species.hasHarvest("slaughter") && !species.slaughter().isBlank()) {
            Iterator<ItemStack> drops = event.getDrops().iterator();
            while (drops.hasNext()) {
                ItemStack drop = drops.next();
                if (HusbandryHarvest.isWoolDrop(drop)) {
                    continue;
                }
                drops.remove();
            }
            if (HusbandryGrowth.isMature(animal, now)) {
                ItemStack roast = HusbandryHarvest.buildFood(animal, species.slaughter());
                if (roast != null) {
                    event.getDrops().add(roast);
                }
                HusbandryDropRoller.rollExtras(animal, ThreadLocalRandom.current())
                        .ifPresent(extra -> event.getDrops().add(extra));
            }
        }
        repository.deleteAnimal(entity.getUniqueId());
        HusbandryEntities.untrack(entity.getUniqueId());
        HusbandryStateDisplay.removeAll(entity);
    }
}
