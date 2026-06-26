package net.tfminecraft.cooking.item.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import net.tfminecraft.cooking.utils.ItemRef;
import net.tfminecraft.furniture.data.DisplayData;

public class ModelData {
    private final int weight;
    private List<String> tags = new ArrayList<>();

    private final String guiRef;
    private final String displayRef;

    private HashMap<String, String> overrides = new HashMap<>();

    private DisplayData displayData;

    private final ItemStack directItem;

    /** Load from YAML state section */
    public ModelData(ConfigurationSection stateConfig) {
        ConfigurationSection gui = stateConfig.getConfigurationSection("gui");
        ConfigurationSection display = stateConfig.getConfigurationSection("display");
        weight = stateConfig.getInt("weight", 0);
        directItem = null;
        if (stateConfig.contains("tags")) tags = stateConfig.getStringList("tags");
        if (gui != null) {
            this.guiRef = gui.getString("item", "v.air");
        } else {
            this.guiRef = "v.air";
        }

        if (display != null) {
            this.displayRef = display.getString("item", "v.air");
            if (display.contains("furniture")) {
                for (String s : display.getStringList("furniture")) {
                    String[] parts = s.split("\\s+", 2);
                    if (parts.length < 2) continue;
                    overrides.put(parts[0], parts[1]);
                }
            }
        } else {
            this.displayRef = "v.air";
        }
        if (stateConfig.isConfigurationSection("display-data")) {
            this.displayData = new DisplayData(stateConfig.getConfigurationSection("display-data"));
        } else {
            this.displayData = new DisplayData();
        }
    }

    /** Deep copy constructor */
    public ModelData(ModelData other) {
        this.guiRef = other.guiRef;
        this.displayRef = other.displayRef;
        this.weight = other.weight;
        this.displayData = other.displayData;
        this.overrides = new HashMap<>(other.overrides);
        this.tags.addAll(other.tags);
        this.directItem = other.directItem != null ? other.directItem.clone() : null;
    }

    public ModelData(ItemStack item) {
        this.guiRef = null;
        this.displayRef = null;
        this.weight = 0;
        this.directItem = item.clone();
        this.displayData = new DisplayData();
        displayData.setxRot(90f);
        displayData.setzRot(90f);
        displayData.setyPos(-0.25f);
    }

    public ItemStack apply(String furniture, ItemStack source) {
        if (directItem != null) {
            ItemStack out = directItem.clone();
            ItemRef.mergeMeta(out, source);
            return out;
        }
        String ref;
        if (furniture != null && overrides.containsKey(furniture)) {
            ref = overrides.get(furniture);
        } else if (furniture != null) {
            ref = displayRef;
        } else {
            ref = guiRef;
        }
        return ItemRef.apply(ref, source);
    }

    public int getWeight() { return weight; }
    public List<String> getTags() { return tags; }

    public DisplayData getDisplayData() {
        return displayData;
    }
}
