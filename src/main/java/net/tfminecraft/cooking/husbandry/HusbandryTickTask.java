package net.tfminecraft.cooking.husbandry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import net.tfminecraft.cooking.Cooking;

public final class HusbandryTickTask {

    private static final long PERIOD_TICKS = 20L * 60L;

    private static int taskId = -1;

    private HusbandryTickTask() {}

    public static void start() {
        if (taskId != -1 || Cooking.plugin == null) {
            return;
        }
        taskId = Bukkit.getScheduler().runTaskTimer(
                Cooking.plugin, HusbandryTickTask::tick, PERIOD_TICKS, PERIOD_TICKS).getTaskId();
    }

    public static void stop() {
        if (taskId == -1) {
            return;
        }
        Bukkit.getScheduler().cancelTask(taskId);
        taskId = -1;
    }

    private static void tick() {
        HusbandryRepository repository = HusbandryEntities.repository();
        if (repository == null) {
            return;
        }
        long now = System.currentTimeMillis();
        List<UUID> snapshot = new ArrayList<>(HusbandryEntities.loadedIds());
        for (UUID uuid : snapshot) {
            Entity entity = Bukkit.getEntity(uuid);
            if (!(entity instanceof LivingEntity) || entity.isDead()) {
                HusbandryEntities.untrack(uuid);
                continue;
            }
            Optional<HusbandryAnimal> stored = repository.getAnimal(uuid);
            if (stored.isEmpty()) {
                HusbandryEntities.untrack(uuid);
                continue;
            }
            HusbandryAnimal animal = stored.get();
            LivingEntity living = (LivingEntity) entity;
            if (HusbandrySimulator.visitLongEnough(animal, now)) {
                HusbandrySimulator.tickLoaded(animal, now);
            }
            HusbandryGrowth.applyMaturity(living, animal, now);
            HusbandryMounts.applyStats(living, animal);
            HusbandryShed.tryShed(living, animal, now);
            HusbandryEggs.tryLay(living, animal, now);
            repository.upsertAnimal(animal);
            HusbandryStateDisplay.sync(living, animal);
        }
    }
}
