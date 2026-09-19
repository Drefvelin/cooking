package net.tfminecraft.cooking.husbandry;

import org.bukkit.NamespacedKey;

import net.tfminecraft.cooking.Cooking;

public final class HusbandryKeys {

    public static final NamespacedKey MANAGED =
            new NamespacedKey(Cooking.plugin, "managed");

    public static final NamespacedKey TAME_NAME =
            new NamespacedKey(Cooking.plugin, "tame_name");

    public static final NamespacedKey LINKED_ANIMAL =
            new NamespacedKey(Cooking.plugin, "linked_animal");

    private HusbandryKeys() {}
}
