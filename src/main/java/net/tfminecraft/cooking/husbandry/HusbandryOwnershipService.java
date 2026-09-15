package net.tfminecraft.cooking.husbandry;

import java.util.List;
import java.util.UUID;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public final class HusbandryOwnershipService {

    public enum Result {
        OK,
        NO_REPOSITORY,
        AT_CAP,
        ALREADY_OWNER,
        NOT_OWNER,
        TARGET_AT_CAP,
        ALREADY_TARGET_OWNER
    }

    private HusbandryOwnershipService() {}

    public static boolean isStaff(Player player) {
        return player != null && player.hasPermission("cooking.admin");
    }

    public static boolean canAccept(Player player) {
        if (player == null) {
            return false;
        }
        if (isStaff(player)) {
            return true;
        }
        HusbandryRepository repository = HusbandryEntities.repository();
        if (repository == null) {
            return false;
        }
        return repository.countForPlayer(player.getUniqueId()) < HusbandryConfig.maxAnimals();
    }

    public static boolean isOwner(Player player, UUID animalUuid) {
        return player != null && isOwner(player.getUniqueId(), animalUuid);
    }

    public static boolean isOwner(UUID playerUuid, UUID animalUuid) {
        HusbandryRepository repository = HusbandryEntities.repository();
        if (repository == null || playerUuid == null || animalUuid == null) {
            return false;
        }
        for (HusbandryOwner owner : repository.listOwners(animalUuid)) {
            if (playerUuid.equals(owner.playerUuid())) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAnyOwner(UUID animalUuid) {
        HusbandryRepository repository = HusbandryEntities.repository();
        if (repository == null || animalUuid == null) {
            return false;
        }
        return !repository.listOwners(animalUuid).isEmpty();
    }

    public static List<HusbandryOwner> listOwners(UUID animalUuid) {
        HusbandryRepository repository = HusbandryEntities.repository();
        if (repository == null) {
            return List.of();
        }
        return repository.listOwners(animalUuid);
    }

    public static Result claimOwner(Player player, HusbandryAnimal animal, LivingEntity entity, String name) {
        HusbandryRepository repository = HusbandryEntities.repository();
        if (repository == null || player == null || animal == null) {
            return Result.NO_REPOSITORY;
        }
        if (isOwner(player, animal.uuid())) {
            applyName(animal, entity, name);
            repository.upsertAnimal(animal);
            return Result.OK;
        }
        if (hasAnyOwner(animal.uuid())) {
            return Result.NOT_OWNER;
        }
        if (!canAccept(player)) {
            return Result.AT_CAP;
        }
        applyName(animal, entity, name);
        animal.setState(HusbandryAnimalState.OWNED);
        repository.upsertAnimal(animal);
        repository.upsertOwner(new HusbandryOwner(animal.uuid(), player.getUniqueId(), "owner"));
        return Result.OK;
    }

    public static Result addCoOwner(Player actor, Player target, HusbandryAnimal animal) {
        HusbandryRepository repository = HusbandryEntities.repository();
        if (repository == null || actor == null || target == null || animal == null) {
            return Result.NO_REPOSITORY;
        }
        if (!hasAnyOwner(animal.uuid())) {
            return Result.NOT_OWNER;
        }
        boolean selfClaim = actor.getUniqueId().equals(target.getUniqueId());
        if (!selfClaim && !isOwner(actor, animal.uuid())) {
            return Result.NOT_OWNER;
        }
        if (isOwner(target, animal.uuid())) {
            return Result.ALREADY_TARGET_OWNER;
        }
        if (!canAccept(target)) {
            return Result.TARGET_AT_CAP;
        }
        animal.setState(HusbandryAnimalState.OWNED);
        repository.upsertAnimal(animal);
        repository.upsertOwner(new HusbandryOwner(animal.uuid(), target.getUniqueId(), "coowner"));
        return Result.OK;
    }

    public static Result removeSelf(Player player, HusbandryAnimal animal) {
        HusbandryRepository repository = HusbandryEntities.repository();
        if (repository == null || player == null || animal == null) {
            return Result.NO_REPOSITORY;
        }
        if (!isOwner(player, animal.uuid())) {
            return Result.NOT_OWNER;
        }
        repository.deleteOwner(animal.uuid(), player.getUniqueId());
        if (!hasAnyOwner(animal.uuid())) {
            animal.setState(HusbandryAnimalState.UNTAMED);
            repository.upsertAnimal(animal);
        }
        return Result.OK;
    }

    public static void applyName(HusbandryAnimal animal, LivingEntity entity, String name) {
        String sanitized = sanitizeName(name, animal, entity);
        animal.setName(sanitized);
        if (entity != null && entity.isValid()) {
            HusbandryStateDisplay.sync(entity, animal);
        }
    }

    private static String sanitizeName(String name, HusbandryAnimal animal, LivingEntity entity) {
        if (name != null && !name.isBlank() && !"???".equals(name.trim())) {
            return name.trim();
        }
        if (animal != null && animal.name() != null && !animal.name().isBlank()
                && !"???".equals(animal.name().trim())) {
            return animal.name();
        }
        if (entity != null) {
            return HusbandryEntities.displayName(entity.getType());
        }
        return "Animal";
    }
}
