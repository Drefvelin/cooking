package net.tfminecraft.cooking.husbandry;

import java.util.Locale;
import java.util.UUID;

public final class HusbandryOwner {

    private final UUID animalUuid;
    private final UUID playerUuid;
    private final String role;

    public HusbandryOwner(UUID animalUuid, UUID playerUuid, String role) {
        this.animalUuid = animalUuid;
        this.playerUuid = playerUuid;
        this.role = role == null || role.isBlank() ? "owner" : role.toLowerCase(Locale.ROOT);
    }

    public UUID animalUuid() {
        return animalUuid;
    }

    public UUID playerUuid() {
        return playerUuid;
    }

    public String role() {
        return role;
    }
}
