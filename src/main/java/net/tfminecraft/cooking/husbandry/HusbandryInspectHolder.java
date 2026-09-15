package net.tfminecraft.cooking.husbandry;

import java.util.UUID;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class HusbandryInspectHolder implements InventoryHolder {

    private final UUID animalUuid;
    private Inventory inventory;

    public HusbandryInspectHolder(UUID animalUuid) {
        this.animalUuid = animalUuid;
    }

    public UUID animalUuid() {
        return animalUuid;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
