package net.tfminecraft.cooking.husbandry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.Plugins.TLibs.Utils.TimeFormatter;

public final class HusbandryInspectGui {

    private static final long MILLIS_PER_HOUR = 3_600_000L;
    private static final int INVENTORY_SIZE = 54;
    private static final int REMOVE_SLOT = 8;
    private static final int CARE_BAR_START = 20;
    private static final int GENETICS_BAR_START = 38;
    private static final int MOUNT_HEALTH_SLOT = 46;
    private static final int MOUNT_SPEED_SLOT = 49;
    private static final int MOUNT_JUMP_SLOT = 52;

    private HusbandryInspectGui() {}

    public static void open(Player player, LivingEntity entity, HusbandryAnimal animal) {
        if (player == null || entity == null || animal == null) {
            return;
        }
        boolean mount = HusbandryMounts.isMount(entity);
        HusbandryInspectHolder holder = new HusbandryInspectHolder(animal.uuid());
        String title = "§6" + (animal.name() == null ? "Animal" : animal.name());
        Inventory inv = Bukkit.createInventory(holder, INVENTORY_SIZE, title);
        holder.setInventory(inv);

        int careMax = HusbandryConfig.careMax();
        int yieldPct = HusbandryGuiBars.yieldPercent(animal);

        inv.setItem(0, statusItem(animal));
        inv.setItem(1, named(Material.SHEARS, animal.neutered() ? "§cNeutered" : "§aNot neutered", List.of()));
        inv.setItem(2, ownersItem(animal.uuid()));
        inv.setItem(4, named(Material.NAME_TAG, "§6" + animal.name(),
                List.of("§7" + entity.getType().name(), "§7State: " + animal.state().storage(),
                        growingLine(animal))));

        HusbandryGuiBars.fillBar(inv, CARE_BAR_START, "Care", animal.care(), careMax, yieldPct);
        HusbandryGuiBars.fillBar(inv, GENETICS_BAR_START, "Genetics", animal.genetics(),
                HusbandryConfig.maxGenetics(), yieldPct);

        if (mount) {
            inv.setItem(MOUNT_HEALTH_SLOT, named(Material.GOLDEN_APPLE,
                    "§dHealth: " + String.format("%.1f", HusbandryMounts.healthHearts(entity)) + " hearts",
                    List.of()));
            inv.setItem(MOUNT_SPEED_SLOT, named(Material.FEATHER,
                    "§bSpeed: " + String.format("%.2f", HusbandryMounts.speedBlocksPerSecond(entity)) + " b/s",
                    List.of()));
            inv.setItem(MOUNT_JUMP_SLOT, named(Material.LEATHER_BOOTS,
                    "§aJump: " + String.format("%.2f", HusbandryMounts.jumpBlockHeight(entity)) + " blocks",
                    List.of()));
        }

        if (HusbandryOwnershipService.isOwner(player, animal.uuid())) {
            inv.setItem(REMOVE_SLOT, named(Material.WRITABLE_BOOK, "§4Remove ownership",
                    List.of("§7Click to stop owning this animal.")));
        }
        player.openInventory(inv);
    }

    private static String growingLine(HusbandryAnimal animal) {
        if (HusbandryGrowth.isMature(animal, System.currentTimeMillis())) {
            return "§7Mature";
        }
        Long matureAt = animal.matureAt();
        if (matureAt == null) {
            return "§7Growing up";
        }
        long remaining = Math.max(0L, matureAt - System.currentTimeMillis());
        return "§7Grows up in " + TimeFormatter.formatTime((int) (remaining / 1000L));
    }

    private static ItemStack statusItem(HusbandryAnimal animal) {
        boolean hungry = animal.hungrySince() != null;
        boolean dirty = animal.dirtySince() != null;
        if (!hungry && !dirty) {
            return named(Material.LIME_DYE, "§aHappy", List.of("§7No Hungry or Dirty."));
        }
        List<String> lore = new ArrayList<>();
        if (hungry) {
            lore.add("§6Hungry");
        }
        if (dirty) {
            lore.add("§eDirty");
        }
        lore.add(decayLine(animal));
        return named(Material.ROTTEN_FLESH, "§cNeeds care", lore);
    }

    private static String decayLine(HusbandryAnimal animal) {
        Long hungry = animal.hungrySince();
        Long dirty = animal.dirtySince();
        if (hungry == null && dirty == null) {
            return "§7No decay";
        }
        long earliest = hungry == null ? dirty : (dirty == null ? hungry : Math.min(hungry, dirty));
        long decayStart = earliest + HusbandryConfig.decayGraceSeconds() * 1000L;
        long now = System.currentTimeMillis();
        if (now < decayStart) {
            double hours = (decayStart - now) / (double) MILLIS_PER_HOUR;
            return "§eDecay in " + formatHours(hours) + "h";
        }
        return "§cDecaying (−" + trim(HusbandryConfig.careDownAmount()) + " / "
                + TimeFormatter.formatTime(HusbandryConfig.careDownIntervalSeconds()) + ")";
    }

    private static ItemStack ownersItem(UUID animalUuid) {
        List<String> lore = new ArrayList<>();
        for (HusbandryOwner owner : HusbandryOwnershipService.listOwners(animalUuid)) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(owner.playerUuid());
            String name = offline.getName() == null ? owner.playerUuid().toString() : offline.getName();
            lore.add("§7" + owner.role() + ": " + name);
        }
        if (lore.isEmpty()) {
            lore.add("§7None (untamed)");
        }
        return named(Material.PLAYER_HEAD, "§eOwners", lore);
    }

    private static String formatHours(double hours) {
        return String.format("%.1f", Math.max(0, hours));
    }

    private static String trim(double value) {
        if (value == (long) value) {
            return String.valueOf((long) value);
        }
        return String.format("%.1f", value);
    }

    private static ItemStack named(Material material, String name, List<String> lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore);
            }
            stack.setItemMeta(meta);
        }
        return stack;
    }
}
