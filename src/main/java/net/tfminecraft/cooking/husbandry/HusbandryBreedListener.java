package net.tfminecraft.cooking.husbandry;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityEnterLoveModeEvent;

public final class HusbandryBreedListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onEnterLove(EntityEnterLoveModeEvent event) {
        Entity entity = event.getEntity();
        BlockReason reason = cannotBreed(entity);
        if (reason == BlockReason.NONE) {
            return;
        }
        event.setCancelled(true);
        clearLove(entity);
        Player feeder = event.getHumanEntity() instanceof Player player ? player : null;
        if (feeder != null) {
            feeder.sendMessage(reason.message);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreed(EntityBreedEvent event) {
        LivingEntity mother = event.getMother();
        LivingEntity father = event.getFather();
        BlockReason motherReason = cannotBreed(mother);
        BlockReason fatherReason = cannotBreed(father);
        if (motherReason != BlockReason.NONE || fatherReason != BlockReason.NONE) {
            event.setCancelled(true);
            clearLove(mother);
            clearLove(father);
            messageBreeder(event, firstBlocked(motherReason, fatherReason));
            return;
        }

        LivingEntity child = event.getEntity();
        if (!HusbandryConfig.isHusbandryType(child.getType())
                && !HusbandryConfig.isHusbandryType(mother.getType())
                && !HusbandryConfig.isHusbandryType(father.getType())) {
            return;
        }

        if (!ownershipAllows(event.getBreeder(), mother, father)) {
            event.setCancelled(true);
            clearLove(mother);
            clearLove(father);
            messageBreeder(event, BlockReason.NOT_OWNER);
            return;
        }

        persistBaby(child, mother, father);
        scheduleMountInherit(child, mother, father);
    }

    private static void scheduleMountInherit(LivingEntity child, LivingEntity mother, LivingEntity father) {
        if (!HusbandryMounts.isMount(child) || !HusbandryMounts.isMount(mother) || !HusbandryMounts.isMount(father)) {
            return;
        }
        org.bukkit.Bukkit.getScheduler().runTaskLater(net.tfminecraft.cooking.Cooking.plugin, () -> {
            if (child == null || !child.isValid()) {
                return;
            }
            HusbandryMounts.applyInheritedStats(child, mother, father);
            HusbandryRepository repository = HusbandryEntities.repository();
            if (repository == null) {
                return;
            }
            repository.getAnimal(child.getUniqueId()).ifPresent(baby -> {
                HusbandryMounts.applySpeed(child, baby);
                repository.upsertAnimal(baby);
            });
        }, 1L);
    }

    private static BlockReason firstBlocked(BlockReason mother, BlockReason father) {
        return mother != BlockReason.NONE ? mother : father;
    }

    private static void messageBreeder(EntityBreedEvent event, BlockReason reason) {
        if (event.getBreeder() instanceof Player player) {
            player.sendMessage(reason.message);
        }
    }

    private static boolean ownershipAllows(LivingEntity breeder, LivingEntity mother, LivingEntity father) {
        boolean owned = HusbandryOwnershipService.hasAnyOwner(mother.getUniqueId())
                || HusbandryOwnershipService.hasAnyOwner(father.getUniqueId());
        if (!owned) {
            return true;
        }
        if (!(breeder instanceof Player player)) {
            return false;
        }
        if (HusbandryOwnershipService.isStaff(player)) {
            return true;
        }
        return HusbandryOwnershipService.isOwner(player, mother.getUniqueId())
                && HusbandryOwnershipService.isOwner(player, father.getUniqueId());
    }

    private static BlockReason cannotBreed(Entity entity) {
        if (entity == null || !HusbandryConfig.isHusbandryType(entity.getType())) {
            return BlockReason.NONE;
        }
        HusbandryRepository repository = HusbandryEntities.repository();
        if (repository == null) {
            return BlockReason.UNAVAILABLE;
        }
        Optional<HusbandryAnimal> stored = repository.getAnimal(entity.getUniqueId());
        if (stored.isEmpty()) {
            return BlockReason.NO_RECORD;
        }
        HusbandryAnimal animal = stored.get();
        if (animal.neutered()) {
            return BlockReason.NEUTERED;
        }
        if (animal.hungrySince() != null || animal.dirtySince() != null) {
            return BlockReason.UNWELL;
        }
        if (!HusbandryGrowth.isMature(animal, System.currentTimeMillis())) {
            return BlockReason.IMMATURE;
        }
        return BlockReason.NONE;
    }

    private static void persistBaby(LivingEntity child, LivingEntity mother, LivingEntity father) {
        if (child == null) {
            return;
        }
        HusbandryRepository repository = HusbandryEntities.repository();
        if (repository == null) {
            return;
        }
        UUID uuid = child.getUniqueId();
        if (repository.exists(uuid)) {
            return;
        }
        int motherGenetics = repository.getAnimal(mother.getUniqueId()).map(HusbandryAnimal::genetics).orElse(0);
        int fatherGenetics = repository.getAnimal(father.getUniqueId()).map(HusbandryAnimal::genetics).orElse(0);
        int genetics = HusbandryGenetics.roll(motherGenetics, fatherGenetics, ThreadLocalRandom.current());
        String name = HusbandryEntities.displayName(child.getType());
        child.setCustomName(name);
        child.setCustomNameVisible(false);
        HusbandryEntities.applyPersistFlags(child);
        HusbandryEntities.stampManaged(child);
        if (HusbandryMounts.isMount(child)) {
            HusbandryMounts.setNerfed(child, true);
        }

        long now = System.currentTimeMillis();
        HusbandryAnimal baby = new HusbandryAnimal(uuid, child.getType().name(), name);
        baby.setState(HusbandryAnimalState.UNTAMED);
        baby.setGenetics(genetics);
        baby.setCare(0);
        baby.setLastProcessedAt(now);
        baby.setLoadedVisitStart(now);
        baby.setUnloadedAt(null);
        baby.setMatureAt(HusbandryGrowth.computeMatureAt(child.getType(), now));
        HusbandryHarvest.prepareNewAnimal(baby, child.getType());
        repository.upsertAnimal(baby);
        HusbandryEntities.trackLoaded(uuid);
    }

    private static void clearLove(Entity entity) {
        if (entity instanceof Animals animals) {
            animals.setLoveModeTicks(0);
        }
    }

    private enum BlockReason {
        NONE(""),
        UNAVAILABLE("§cCould not breed this animal."),
        NO_RECORD("§cThis animal cannot breed."),
        UNWELL("§cHungry or dirty animals cannot breed."),
        NEUTERED("§cNeutered animals cannot breed."),
        IMMATURE("§cThis animal is still growing up."),
        NOT_OWNER("§cYou must own both parents.");

        final String message;

        BlockReason(String message) {
            this.message = message;
        }
    }
}
