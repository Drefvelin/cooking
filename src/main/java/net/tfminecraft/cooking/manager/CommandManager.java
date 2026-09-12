package net.tfminecraft.cooking.manager;

import java.util.Map;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.tfminecraft.InteractibleFurniture;
import net.tfminecraft.cooking.baking.BakingTrayRecipe;
import net.tfminecraft.cooking.baking.BakingTrayRegistry;
import net.tfminecraft.cooking.baking.BakingTrayState;
import net.tfminecraft.cooking.cache.FurnitureCache;
import net.tfminecraft.cooking.item.FoodItem;
import net.tfminecraft.cooking.item.tag.TagTrack;
import net.tfminecraft.cooking.heat.HeatSources;
import net.tfminecraft.cooking.mixing.MixingBowlDisplay;
import net.tfminecraft.cooking.mixing.MixingBowlSlots;
import net.tfminecraft.cooking.quality.CompositionContext;
import net.tfminecraft.cooking.quality.CompositionQualityResolver;
import net.tfminecraft.cooking.quality.CompositionResult;
import net.tfminecraft.cooking.quality.OriginQualityResolver;
import net.tfminecraft.cooking.utils.FoodParser;
import net.tfminecraft.cooking.utils.ItemBuilder;
import net.tfminecraft.cooking.utils.NameComposer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.tfminecraft.furniture.Furniture;
import net.tfminecraft.furniture.PlacedFurnitureSlot;
import net.tfminecraft.furniture.PlacedSlot;

public class CommandManager implements CommandExecutor {

    private static final double MIXING_BOWL_SEARCH_RADIUS = 3.0;
    private static final double HEAT_SEARCH_RADIUS = 4.0;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("builditem")) {
            return handleBuildItem(player, args);
        }

        if (args[0].equalsIgnoreCase("preview")) {
            return handlePreview(player, args);
        }

        if (args[0].equalsIgnoreCase("heat")) {
            return handleHeat(player);
        }

        if (args[0].equalsIgnoreCase("nametest")) {
            return handleNameTest(player, args);
        }

        if (args[0].equalsIgnoreCase("qualitytest")) {
            return handleQualityTest(player, args);
        }

        sendUsage(player);
        return true;
    }

    private void sendUsage(Player player) {
        player.sendMessage("§e/cooking builditem <string>");
        player.sendMessage("§e/cooking preview mixing_bowl <flour|water|yeast|dough|all|clear>");
        player.sendMessage("§e/cooking heat");
        player.sendMessage("§e/cooking nametest <foodString> [#colour]");
        player.sendMessage("§e/cooking qualitytest pickup <foodString>");
        player.sendMessage("§e/cooking qualitytest compose <foodString> [context]");
        player.sendMessage("§e/cooking qualitytest compose2 <food|food|...> [context]");
        player.sendMessage("§7Contexts: " + formatCompositionContexts());
    }

    private static String formatCompositionContexts() {
        return Arrays.stream(CompositionContext.values())
                .map(CompositionContext::name)
                .collect(Collectors.joining(", "));
    }

    private boolean handleQualityTest(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cUsage: /cooking qualitytest <pickup|compose|compose2> ...");
            player.sendMessage("§7Contexts: " + formatCompositionContexts());
            return true;
        }

        if (args[1].equalsIgnoreCase("compose2")) {
            return handleCompose2(player, args);
        }

        StringBuilder builder = new StringBuilder();
        int end = args.length;
        CompositionContext context = CompositionContext.CUTTING_BOARD;

        if (args[1].equalsIgnoreCase("compose") && args.length >= 4) {
            try {
                context = CompositionContext.valueOf(args[end - 1].toUpperCase());
                end--;
            } catch (IllegalArgumentException ignored) {
            }
        }

        for (int i = 2; i < end; i++) {
            if (i > 2) {
                builder.append(' ');
            }
            builder.append(args[i]);
        }

        FoodParser.Result result = FoodParser.parse(builder.toString());
        if (result == null || result.template == null) {
            player.sendMessage("§cFailed to parse food string.");
            return true;
        }

        if (args[1].equalsIgnoreCase("pickup")) {
            int quality = OriginQualityResolver.resolve(player, result.template);
            player.sendMessage("§aPickup quality: §f" + quality);
            return true;
        }

        if (args[1].equalsIgnoreCase("compose")) {
            int quality = CompositionQualityResolver.resolve(player, List.of(result.template), context);
            player.sendMessage("§aComposition quality (" + context.name() + "): §f" + quality);
            return true;
        }

        player.sendMessage("§cUsage: /cooking qualitytest <pickup|compose|compose2> ...");
        return true;
    }

    private boolean handleCompose2(Player player, String[] args) {
        int end = args.length;
        CompositionContext context = CompositionContext.CUTTING_BOARD;

        if (args.length >= 4) {
            try {
                context = CompositionContext.valueOf(args[end - 1].toUpperCase());
                end--;
            } catch (IllegalArgumentException ignored) {
            }
        }

        StringBuilder joined = new StringBuilder();
        for (int i = 2; i < end; i++) {
            if (i > 2) {
                joined.append(' ');
            }
            joined.append(args[i]);
        }

        String[] parts = joined.toString().split("\\|");
        List<FoodItem> inputs = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            FoodParser.Result result = FoodParser.parse(trimmed);
            if (result == null || result.template == null) {
                player.sendMessage("§cFailed to parse food string: " + trimmed);
                return true;
            }
            inputs.add(result.template);
        }

        if (inputs.isEmpty()) {
            player.sendMessage("§cUsage: /cooking qualitytest compose2 <food|food|...> [context]");
            return true;
        }

        CompositionResult composed = CompositionQualityResolver.compose(player, inputs, context);
        player.sendMessage("§aComposition (" + context.name() + ")");
        player.sendMessage("§7Baseline: §f" + composed.getBaselineQuality() + " §7→ Final: §f" + composed.getFinalQuality());
        player.sendMessage("§7Mains (" + composed.getMains().size() + "): §f" + formatFoodList(composed.getMains()));
        player.sendMessage("§7Extras (" + composed.getExtras().size() + "): §f" + formatFoodList(composed.getExtras()));
        player.sendMessage("§7Neutral (" + composed.getNeutral().size() + "): §f" + formatFoodList(composed.getNeutral()));
        if (!composed.getFreshnessTracks().isEmpty()) {
            StringBuilder freshness = new StringBuilder();
            for (Map.Entry<String, Integer> entry : composed.getFreshnessTracks().entrySet()) {
                if (freshness.length() > 0) {
                    freshness.append(", ");
                }
                freshness.append(entry.getKey()).append('=').append(entry.getValue());
            }
            player.sendMessage("§7Freshness tracks: §f" + freshness);
        }
        return true;
    }

    private static String formatFoodList(List<FoodItem> items) {
        if (items.isEmpty()) {
            return "-";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            FoodItem item = items.get(i);
            builder.append(item.getId())
                    .append("(q=")
                    .append(item.getQualityMin())
                    .append(", cat=")
                    .append(item.getCategory())
                    .append(')');
        }
        return builder.toString();
    }

    private boolean handleNameTest(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /cooking nametest <foodString> [#colour]");
            return true;
        }

        String colour = null;
        int end = args.length;
        if (args[end - 1].startsWith("#")) {
            colour = args[end - 1];
            end--;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < end; i++) {
            if (i > 1) {
                builder.append(' ');
            }
            builder.append(args[i]);
        }

        FoodParser.Result result = FoodParser.parse(builder.toString());
        if (result == null || result.template == null) {
            player.sendMessage("§cFailed to parse food string.");
            return true;
        }

        Map<String, String> extras = new HashMap<>();
        if (colour != null) {
            extras.put("colour", colour);
        }

        String composed = NameComposer.compose(result.template, extras);
        player.sendMessage("§aComposed name: §f" + composed);
        return true;
    }

    private boolean handleBuildItem(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /cooking builditem <itemString>");
            return true;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) builder.append(" ");
            builder.append(args[i]);
        }
        String itemString = builder.toString();
        ItemBuilder.buildFromString(player, itemString, null);

        player.sendMessage("§aGenerated item(s) from string!");
        return true;
    }

    private boolean handlePreview(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /cooking preview <target> ...");
            player.sendMessage("§cAvailable targets: mixing_bowl");
            return true;
        }

        if (args[1].equalsIgnoreCase("mixing_bowl")) {
            return handleMixingBowlPreview(player, args);
        }

        player.sendMessage("§cUnknown preview target. Available: mixing_bowl");
        return true;
    }

    private boolean handleMixingBowlPreview(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cUsage: /cooking preview mixing_bowl <flour|water|yeast|dough|all|clear>");
            return true;
        }

        Furniture furniture = findNearestMixingBowl(player);
        if (furniture == null) {
            player.sendMessage("§cNo mixing bowl found within " + MIXING_BOWL_SEARCH_RADIUS + " blocks.");
            return true;
        }

        String action = args[2].toLowerCase();
        if (action.equals("clear")) {
            clearMixingPreview(furniture);
            player.sendMessage("§aCleared mixing bowl preview slots.");
            return true;
        }

        if (action.equals("all")) {
            previewSlot(furniture, MixingBowlSlots.FLOUR);
            previewSlot(furniture, MixingBowlSlots.WATER);
            previewSlot(furniture, MixingBowlSlots.YEAST);
            player.sendMessage("§aPreviewing flour, water, and yeast on the nearest mixing bowl.");
            return true;
        }

        if (!isPreviewSlot(action)) {
            player.sendMessage("§cUnknown slot. Use flour, water, yeast, dough, all, or clear.");
            return true;
        }

        if (!previewSlot(furniture, action)) {
            player.sendMessage("§cNo display model configured for §f" + action + "§c.");
            return true;
        }

        player.sendMessage("§aPreviewing §f" + action + " §aon the nearest mixing bowl.");
        return true;
    }

    private boolean isPreviewSlot(String action) {
        return action.equals(MixingBowlSlots.FLOUR)
                || action.equals(MixingBowlSlots.WATER)
                || action.equals(MixingBowlSlots.YEAST)
                || action.equals(MixingBowlSlots.DOUGH);
    }

    private Furniture findNearestMixingBowl(Player player) {
        Furniture nearest = null;
        double nearestDistance = MIXING_BOWL_SEARCH_RADIUS;

        for (Map.Entry<UUID, Furniture> entry : InteractibleFurniture.getInstance()
                .getFurnitureManager()
                .getPlacedFurniture()
                .entrySet()) {
            Furniture furniture = entry.getValue();
            if (furniture.isCarried() || !FurnitureCache.isMixingBowl(furniture)) {
                continue;
            }

            if (furniture.getType() == null) {
                continue;
            }

            double distance = furniture.getLoc().distance(player.getLocation());
            if (distance <= nearestDistance) {
                nearestDistance = distance;
                nearest = furniture;
            }
        }

        return nearest;
    }

    private void clearMixingPreview(Furniture furniture) {
        MixingBowlDisplay.clearLayer(furniture, MixingBowlSlots.FLOUR);
        MixingBowlDisplay.clearLayer(furniture, MixingBowlSlots.WATER);
        MixingBowlDisplay.clearLayer(furniture, MixingBowlSlots.YEAST);
        MixingBowlDisplay.clearLayer(furniture, MixingBowlSlots.DOUGH);
    }

    private boolean previewSlot(Furniture furniture, String slotKey) {
        return MixingBowlDisplay.showLayer(furniture, slotKey);
    }

    private boolean handleHeat(Player player) {
        Furniture furniture = findNearestFurniture(player, HEAT_SEARCH_RADIUS);
        if (furniture == null) {
            player.sendMessage("§cNo furniture found within " + HEAT_SEARCH_RADIUS + " blocks.");
            return true;
        }

        player.sendMessage("§6--- Heat debug ---");
        player.sendMessage("§7Furniture: §f" + furniture.getId());
        player.sendMessage("§7UUID: §f" + furniture.getEntityId());
        player.sendMessage("§7isSource: §f" + HeatSources.isSource(furniture));
        player.sendMessage("§7isConsumer: §f" + HeatSources.isConsumer(furniture));

        if (HeatSources.isSource(furniture)) {
            player.sendMessage("§7hasHeat: §f" + HeatSources.hasHeat(furniture));
        }

        if (HeatSources.isConsumer(furniture)) {
            var source = HeatSources.findSource(furniture);
            player.sendMessage("§7findSource: §f"
                    + (source.isPresent() ? source.get().getId() + " (" + source.get().getEntityId() + ")" : "none"));
            player.sendMessage("§7consumerHasHeat: §f" + HeatSources.consumerHasHeat(furniture));
            appendBakeDebug(player, furniture);
        }

        return true;
    }

    private void appendBakeDebug(Player player, Furniture consumer) {
        for (PlacedFurnitureSlot slot : consumer.getActiveFurnitureSlots().values()) {
            Furniture tray = slot.getNested();
            if (tray == null || !BakingTrayRegistry.isTray(tray)) {
                continue;
            }

            BakingTrayRecipe recipe = BakingTrayRegistry.getByFurniture(tray);
            player.sendMessage("§6--- Bake debug ---");
            player.sendMessage("§7nestedTray: §f" + tray.getId());
            player.sendMessage("§7recipe: §f" + (recipe != null ? recipe.getId() : "none"));
            if (recipe != null) {
                player.sendMessage("§7cook-seconds: §f" + recipe.getBake().getCookSeconds());
                player.sendMessage("§7burn-seconds: §f" + recipe.getBake().getBurnSeconds());
                for (String slotId : recipe.getAllSlotIds()) {
                    if (!tray.hasActiveSlot(slotId)) {
                        continue;
                    }
                    PlacedSlot placedSlot = tray.getActiveSlot(slotId).orElse(null);
                    if (placedSlot == null) {
                        continue;
                    }
                    var item = placedSlot.getCurrentItem();
                    if (item == null || item.getType().isAir()) {
                        continue;
                    }
                    FoodItem foodItem = FoodItem.fromItem(item);
                    if (foodItem == null) {
                        continue;
                    }
                    TagTrack cooked = foodItem.getTagTrack("cooked");
                    int cookedValue = cooked != null ? cooked.getValue() : -1;
                    player.sendMessage("§7" + slotId + ": §felapsed="
                            + BakingTrayState.getSlotElapsed(tray, slotId)
                            + " cooked=" + cookedValue);
                }
            }
            return;
        }
    }

    private Furniture findNearestFurniture(Player player, double radius) {
        Furniture nearest = null;
        double nearestDistance = radius;

        for (Map.Entry<UUID, Furniture> entry : InteractibleFurniture.getInstance()
                .getFurnitureManager()
                .getPlacedFurniture()
                .entrySet()) {
            Furniture furniture = entry.getValue();
            if (furniture.isCarried() || furniture.isAttached()) {
                continue;
            }

            double distance = furniture.getLoc().distance(player.getLocation());
            if (distance <= nearestDistance) {
                nearestDistance = distance;
                nearest = furniture;
            }
        }

        return nearest;
    }
}
