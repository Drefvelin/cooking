package net.tfminecraft.cooking.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * Fired when a player successfully finishes cooking/crafting a food item,
 * after the result item has already been handed to them.
 */
public class DishCookedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final ItemStack result;
    private final String method;

    public DishCookedEvent(Player player, ItemStack result, String method) {
        this.player = player;
        this.result = result.clone();
        this.method = method;
    }

    public Player getPlayer() {
        return player;
    }

    public ItemStack getResult() {
        return result;
    }

    public String getMethod() {
        return method;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
