package net.tfminecraft.cooking.item.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.tfminecraft.furniture.data.DisplayData;

public class ModelData {
    private final int weight;
    private List<String> tags = new ArrayList<>();

    private final String guiItem;
    private final int guiModelData;

    private final String displayItem;
    private final int displayModelData;

    private HashMap<String, Integer> overrides = new HashMap<>();

    private DisplayData displayData;

    /** Load from YAML state section */
    public ModelData(ConfigurationSection stateConfig) {
        ConfigurationSection gui = stateConfig.getConfigurationSection("gui");
        ConfigurationSection display = stateConfig.getConfigurationSection("display");
        weight = stateConfig.getInt("weight", 0);
        if(stateConfig.contains("tags")) tags = stateConfig.getStringList("tags");
        if (gui != null) {
            this.guiItem = gui.getString("item", "minecraft:air");
            this.guiModelData = gui.getInt("model-data", 0);
        } else {
            this.guiItem = "minecraft:air";
            this.guiModelData = 0;
        }

        if (display != null) {
            this.displayItem = display.getString("item", "minecraft:air");
            this.displayModelData = display.getInt("model-data", 0);
            if(display.contains("furniture")) {
            for(String s : display.getStringList("furniture")) {
                String[] parts = s.split("\\.");
                if(parts.length < 2) continue;
                try {
                    overrides.put(parts[0], Integer.parseInt(parts[1]));
                } catch (Exception e) {
                    Bukkit.getLogger().info("[Cooking] Couldnt load ModelData: "+s);
                }
            }
        }
        } else {
            this.displayItem = "minecraft:air";
            this.displayModelData = 0;
        }
        if(stateConfig.isConfigurationSection("display-data")) {
            this.displayData = new DisplayData(stateConfig.getConfigurationSection("display-data"));
        } else {
            this.displayData = new DisplayData();
        }
    }

    /** Deep copy constructor */
    public ModelData(ModelData other) {
        this.guiItem = other.guiItem;
        this.guiModelData = other.guiModelData;
        this.displayItem = other.displayItem;
        this.displayModelData = other.displayModelData;
        this.weight = other.weight;
        this.displayData = other.displayData;
        this.overrides = new HashMap<>(other.overrides);
        this.tags.addAll(other.tags);
    }

    public ModelData(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        this.guiItem = item.getType().toString();
        this.guiModelData = meta.hasCustomModelData() ? meta.getCustomModelData() : 0;
        this.displayItem = item.getType().toString();
        this.displayModelData = meta.hasCustomModelData() ? meta.getCustomModelData() : 0;
        this.weight = 0;
        this.displayData = new DisplayData();
        displayData.setxRot(90f);
        displayData.setzRot(90f);
        displayData.setyPos(-0.25f);
    }

    public ItemStack apply(String furniture, ItemStack i) {
        if(furniture == null) {
            Bukkit.getPlayerExact("drefvelin").sendMessage("aaaaaa");
            try {
                i.setType(Material.valueOf(guiItem.toUpperCase()));
                ItemMeta m = i.getItemMeta();
                m.setCustomModelData(guiModelData);
                i.setItemMeta(m);
            } catch (Exception e) {
                // TODO: handle exception
            }
            
        } else {
            try {
                i.setType(Material.valueOf(displayItem.toUpperCase()));
                ItemMeta m = i.getItemMeta();
                m.setCustomModelData(getDisplayModelData(furniture));
                i.setItemMeta(m);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        return i;
    }

    public int getWeight() { return weight; }
    public List<String> getTags() { return tags; }

    public String getGuiItem() { return guiItem; }
    public int getGuiModelData() { return guiModelData; }

    public String getDisplayItem() { return displayItem; }
    public int getDisplayModelData(String furniture) {
        if(furniture == null) return displayModelData;
        if(overrides.containsKey(furniture)) return overrides.get(furniture);
        return displayModelData;
    }

    public DisplayData getDisplayData() {
        return displayData;
    }
}
