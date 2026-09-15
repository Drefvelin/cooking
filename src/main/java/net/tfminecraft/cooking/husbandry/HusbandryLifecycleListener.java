package net.tfminecraft.cooking.husbandry;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;

public final class HusbandryLifecycleListener implements Listener {

    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        for (Entity entity : event.getEntities()) {
            handleLoad(entity);
        }
    }

    @EventHandler
    public void onEntitiesUnload(EntitiesUnloadEvent event) {
        for (Entity entity : event.getEntities()) {
            handleUnload(entity);
        }
    }

    public static void resumeLoadedWorlds() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                handleLoad(entity);
            }
        }
    }

    public static void flushLoadedForDisable() {
        HusbandryRepository repository = HusbandryEntities.repository();
        if (repository == null) {
            HusbandryEntities.clearLoaded();
            return;
        }
        long now = System.currentTimeMillis();
        for (UUID uuid : HusbandryEntities.loadedIds()) {
            Optional<HusbandryAnimal> stored = repository.getAnimal(uuid);
            if (stored.isEmpty()) {
                continue;
            }
            HusbandryAnimal animal = stored.get();
            animal.setUnloadedAt(now);
            repository.upsertAnimal(animal);
        }
        HusbandryEntities.clearLoaded();
    }

    static void handleLoad(Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        HusbandryRepository repository = HusbandryEntities.repository();
        if (repository == null) {
            return;
        }
        UUID uuid = entity.getUniqueId();
        boolean hasRow = repository.exists(uuid);

        if (HusbandryConfig.isRemoveUnowned(entity.getType())
                && !HusbandryOwnershipService.hasAnyOwner(uuid)) {
            if (hasRow) {
                repository.deleteAnimal(uuid);
            }
            HusbandryEntities.untrack(uuid);
            entity.remove();
            return;
        }
        if (!hasRow) {
            return;
        }

        HusbandryEntities.applyPersistFlags(living);
        HusbandryEntities.stampManaged(living);
        HusbandryEntities.trackLoaded(uuid);

        Optional<HusbandryAnimal> stored = repository.getAnimal(uuid);
        if (stored.isEmpty()) {
            return;
        }
        HusbandryAnimal animal = stored.get();
        long now = System.currentTimeMillis();
        HusbandrySimulator.catchUp(animal, now, java.util.concurrent.ThreadLocalRandom.current());
        HusbandryGrowth.applyMaturity(living, animal, now);
        animal.setUnloadedAt(null);
        animal.setLoadedVisitStart(now);
        repository.upsertAnimal(animal);
        HusbandryStateDisplay.sync(living, animal);
    }

    static void handleUnload(Entity entity) {
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        UUID uuid = entity.getUniqueId();
        HusbandryEntities.untrack(uuid);
        HusbandryRepository repository = HusbandryEntities.repository();
        if (repository == null) {
            return;
        }
        Optional<HusbandryAnimal> stored = repository.getAnimal(uuid);
        if (stored.isEmpty()) {
            return;
        }
        HusbandryAnimal animal = stored.get();
        animal.setUnloadedAt(System.currentTimeMillis());
        repository.upsertAnimal(animal);
    }
}
