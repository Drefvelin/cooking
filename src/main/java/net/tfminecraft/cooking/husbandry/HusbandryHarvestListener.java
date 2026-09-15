package net.tfminecraft.cooking.husbandry;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import net.tfminecraft.cooking.Cooking;
import net.tfminecraft.cooking.cup.MilkBucketConverter;
import net.tfminecraft.cooking.item.FoodItem;
import net.tfminecraft.cooking.utils.InventoryAdder;
import net.tfminecraft.cooking.utils.QualityUtils;

public final class HusbandryHarvestListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMilk(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() != Material.BUCKET) {
            return;
        }
        Entity clicked = event.getRightClicked();
        if (!(clicked instanceof LivingEntity living)) {
            return;
        }
        HusbandrySpecies species = HusbandryConfig.species(living.getType());
        if (species == null || !species.hasHarvest("milk")) {
            return;
        }
        HusbandryRepository repository = HusbandryEntities.repository();
        if (repository == null) {
            return;
        }
        Optional<HusbandryAnimal> stored = repository.getAnimal(living.getUniqueId());
        if (stored.isEmpty()) {
            return;
        }
        HusbandryAnimal animal = stored.get();
        long now = System.currentTimeMillis();
        if (!HusbandryGrowth.isMature(animal, now)) {
            event.setCancelled(true);
            player.sendMessage("§cThis animal is still growing up.");
            return;
        }
        if (onMilkCooldown(animal, now)) {
            event.setCancelled(true);
            player.sendMessage("§cThis animal is not ready to be milked.");
            return;
        }
        Bukkit.getScheduler().runTask(Cooking.plugin, () -> finishMilk(player, animal, species, repository));
    }

    @EventHandler(ignoreCancelled = true)
    public void onShear(PlayerShearEntityEvent event) {
        Entity sheared = event.getEntity();
        HusbandrySpecies species = HusbandryConfig.species(sheared.getType());
        if (species == null || !species.hasHarvest("shear") || species.shear().isBlank()) {
            return;
        }
        HusbandryRepository repository = HusbandryEntities.repository();
        if (repository == null) {
            return;
        }
        Optional<HusbandryAnimal> stored = repository.getAnimal(sheared.getUniqueId());
        if (stored.isEmpty()) {
            return;
        }
        HusbandryAnimal animal = stored.get();
        long now = System.currentTimeMillis();
        if (!HusbandryGrowth.isMature(animal, now)) {
            event.getPlayer().sendMessage("§cThis animal is still growing up.");
            event.setCancelled(true);
            return;
        }
        if (animal.woolReadyAt() != null && animal.woolReadyAt() > now) {
            return;
        }
        int amount = HusbandryConfig.woolFor(HusbandryConfig.effectiveGenetics(animal));
        ItemStack extra = HusbandryHarvest.buildTlibs(species.shear(), amount);
        if (extra != null) {
            InventoryAdder.addItem(event.getPlayer(), extra);
        }
        long wait = HusbandryConfig.woolTimerSeconds() * 1000L;
        animal.setWoolReadyAt(now + wait);
        repository.upsertAnimal(animal);
    }

    private static boolean onMilkCooldown(HusbandryAnimal animal, long now) {
        Long last = animal.lastMilkAt();
        if (last == null) {
            return false;
        }
        long wait = HusbandryConfig.milkCooldownSeconds() * 1000L;
        return now - last < wait;
    }

    private static void finishMilk(
            Player player,
            HusbandryAnimal animal,
            HusbandrySpecies species,
            HusbandryRepository repository) {
        ItemStack main = player.getInventory().getItemInMainHand();
        if (main == null || main.getType() != Material.MILK_BUCKET) {
            return;
        }
        ItemStack converted;
        if (!species.milk().isBlank()) {
            converted = HusbandryHarvest.buildFood(animal, species.milk());
        } else {
            converted = MilkBucketConverter.convert(
                    player, main, QualityUtils.clamp(HusbandryHarvest.stars(animal)));
        }
        if (converted == null) {
            converted = MilkBucketConverter.convert(
                    player, main, QualityUtils.clamp(HusbandryHarvest.stars(animal)));
        }
        if (converted != null && (FoodItem.fromItem(main) == null || converted != main)) {
            converted.setAmount(main.getAmount());
            player.getInventory().setItemInMainHand(converted);
            player.updateInventory();
        }
        animal.setLastMilkAt(System.currentTimeMillis());
        repository.upsertAnimal(animal);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDropEgg(EntityDropItemEvent event) {
        if (!(event.getEntity() instanceof Chicken chicken)) {
            return;
        }
        ItemStack stack = event.getItemDrop().getItemStack();
        if (stack.getType() != Material.EGG) {
            return;
        }
        HusbandryRepository repository = HusbandryEntities.repository();
        if (repository == null || !repository.exists(chicken.getUniqueId())) {
            return;
        }
        HusbandrySpecies species = HusbandryConfig.species(chicken.getType());
        if (species != null && species.hasHarvest("egg")) {
            event.setCancelled(true);
        }
    }
}
