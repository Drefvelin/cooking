package net.tfminecraft.cooking.nutrition;

import java.lang.reflect.Method;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

public final class RegenBlocker implements Listener {

    private static final Method FAST_REGEN_METHOD = resolveFastRegenMethod();

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onRegainHealth(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        if (event.getRegainReason() == RegainReason.SATIATED || isFastRegen(event)) {
            event.setCancelled(true);
        }
    }

    private static Method resolveFastRegenMethod() {
        try {
            return EntityRegainHealthEvent.class.getMethod("isFastRegen");
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    private static boolean isFastRegen(EntityRegainHealthEvent event) {
        if (FAST_REGEN_METHOD == null) {
            return false;
        }
        try {
            return (boolean) FAST_REGEN_METHOD.invoke(event);
        } catch (ReflectiveOperationException ex) {
            return false;
        }
    }
}
