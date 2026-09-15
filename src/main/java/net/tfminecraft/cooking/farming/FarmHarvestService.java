package net.tfminecraft.cooking.farming;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import net.tfminecraft.cooking.Cooking;
import net.tfminecraft.cooking.crops.CropHarvestItems;
import net.tfminecraft.cooking.crops.CropHarvestQuality;
import net.tfminecraft.cooking.crops.CropsConfig;
import net.tfminecraft.cooking.crops.CropDefinition;
import net.tfminecraft.cooking.utils.IngredientConverter;
import net.tfminecraft.cooking.utils.InventoryAdder;

public final class FarmHarvestService {

    private FarmHarvestService() {}

    public static void harvestArea(
            Player player,
            ItemStack tool,
            FarmingToolDefinition hoe,
            Block centre) {
        if (player == null || tool == null || hoe == null || centre == null) {
            return;
        }

        List<Block> blocks = FarmAreaScanner.findCropBlocks(
                centre,
                hoe.radius(),
                FarmingConfig.cropMaterials());

        for (Block block : blocks) {
            ItemStack currentTool = player.getInventory().getItemInMainHand();
            if (currentTool.getType().isAir() || currentTool.getAmount() <= 0) {
                break;
            }

            FarmingCropDefinition cropDef = FarmingToolMatcher.cropFor(block.getType());
            if (cropDef == null) {
                continue;
            }

            if (harvestBlock(player, currentTool, hoe, block, cropDef)) {
                if (applyToolDamage(player, currentTool)) {
                    break;
                }
            }
        }
    }

    public static boolean harvestBlock(
            Player player,
            ItemStack tool,
            FarmingToolDefinition hoe,
            Block block,
            FarmingCropDefinition cropDef) {
        if (player == null || tool == null || hoe == null || block == null || cropDef == null) {
            return false;
        }
        if (FarmingConfig.onlyHarvestMature() && !isMature(block)) {
            return false;
        }

        CropDefinition cropQuality = CropsConfig.byBlock(block.getType());
        Integer harvestQuality = null;
        if (cropQuality != null && CropsConfig.SOURCE_VANILLA.equals(cropQuality.source())) {
            harvestQuality = rollHarvestQuality(player, block, cropQuality, hoe);
        }

        BlockState state = block.getState();
        List<ItemStack> drops = new ArrayList<>(block.getDrops(tool));
        ItemStack replantSeed = takeOneSeed(drops, cropDef.seed());

        for (ItemStack drop : drops) {
            if (drop == null || drop.getType().isAir() || drop.getAmount() <= 0) {
                continue;
            }
            ItemStack converted = harvestQuality != null
                    ? CropHarvestItems.convertDrop(drop, cropQuality, harvestQuality)
                    : IngredientConverter.convertHarvestDrop(player, drop, hoe.qualityBonusPercent());
            ItemStack leftover = InventoryAdder.addItem(player, converted);
            if (leftover != null) {
                block.getWorld().dropItemNaturally(block.getLocation(), leftover);
            }
        }

        FarmingEffects.breakCrop(block, state, hoe.radius(), FarmingConfig.harvestParticleMultiplier());
        block.setType(Material.AIR);

        if (replantSeed != null) {
            scheduleReplant(player, block, cropDef, replantSeed);
        }

        return true;
    }

    private static int rollHarvestQuality(
            Player player,
            Block block,
            CropDefinition crop,
            FarmingToolDefinition hoe) {
        int harvested = CropHarvestQuality.roll(crop.id(), block.getLocation(), player);
        return HoeQualityBonus.apply(harvested, hoe.qualityBonusPercent());
    }

    private static boolean isMature(Block block) {
        BlockData data = block.getBlockData();
        if (data instanceof Ageable ageable) {
            return ageable.getAge() == ageable.getMaximumAge();
        }
        return true;
    }

    private static ItemStack takeOneSeed(List<ItemStack> drops, Material seedMaterial) {
        for (ItemStack drop : drops) {
            if (drop == null || drop.getType() != seedMaterial || drop.getAmount() < 1) {
                continue;
            }
            ItemStack seed = drop.clone();
            seed.setAmount(1);
            drop.setAmount(drop.getAmount() - 1);
            return seed;
        }
        return null;
    }

    private static void scheduleReplant(
            Player player,
            Block block,
            FarmingCropDefinition cropDef,
            ItemStack seed) {
        int min = FarmingConfig.replantDelayMin();
        int max = FarmingConfig.replantDelayMax();
        int delay = min;
        if (max > min) {
            delay = min + ThreadLocalRandom.current().nextInt(max - min + 1);
        }

        Material cropMaterial = cropDef.crop();
        ItemStack seedCopy = seed.clone();

        Cooking.plugin.getServer().getScheduler().runTaskLater(Cooking.plugin, () -> {
            if (!player.isOnline()) {
                dropSeed(block, seedCopy);
                return;
            }
            if (!block.isEmpty()) {
                dropSeed(block, seedCopy);
                return;
            }

            BlockState replacedState = block.getState();
            block.setType(cropMaterial);
            BlockData data = block.getBlockData();
            if (data instanceof Ageable ageable) {
                ageable.setAge(0);
                block.setBlockData(ageable);
            }

            Block soil = block.getRelative(0, -1, 0);
            BlockPlaceEvent plantEvent = new BlockPlaceEvent(
                    block,
                    replacedState,
                    soil,
                    seedCopy,
                    player,
                    true,
                    EquipmentSlot.HAND);
            Cooking.plugin.getServer().getPluginManager().callEvent(plantEvent);
            if (plantEvent.isCancelled() || !plantEvent.canBuild()) {
                replacedState.update(true);
                dropSeed(block, seedCopy);
                return;
            }

            FarmingEffects.replant(block, FarmingConfig.replantParticleMultiplier());
        }, delay);
    }

    private static void dropSeed(Block block, ItemStack seed) {
        if (block.getWorld() != null) {
            block.getWorld().dropItemNaturally(block.getLocation(), seed);
        }
    }

    private static boolean applyToolDamage(Player player, ItemStack tool) {
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) {
            return false;
        }
        int damage = FarmingConfig.toolDamagePerHarvest();
        if (damage <= 0 || tool.getType().getMaxDurability() == 0) {
            return false;
        }
        if (!(tool.getItemMeta() instanceof Damageable damageable) || tool.getItemMeta().isUnbreakable()) {
            return false;
        }
        if (FarmingConfig.applyUnbreaking()) {
            int unbreaking = tool.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.UNBREAKING);
            double chance = 1.0 / (unbreaking + 1.0);
            if (ThreadLocalRandom.current().nextDouble() >= chance) {
                return false;
            }
        }
        damageable.setDamage(damageable.getDamage() + damage);
        tool.setItemMeta(damageable);
        if (damageable.getDamage() >= tool.getType().getMaxDurability()) {
            tool.setAmount(tool.getAmount() - 1);
            return true;
        }
        return false;
    }
}
