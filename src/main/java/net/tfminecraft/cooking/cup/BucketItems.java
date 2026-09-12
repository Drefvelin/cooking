package net.tfminecraft.cooking.cup;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class BucketItems {
    private BucketItems() {}

    public static ItemStack empty() {
        return new ItemStack(Material.BUCKET, 1);
    }
}
