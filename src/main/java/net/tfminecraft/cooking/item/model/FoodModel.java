package net.tfminecraft.cooking.item.model;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import net.tfminecraft.cooking.item.FoodItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoodModel {
    private String id;
    private Map<String, ModelData> states = new HashMap<>();

    /** Load from the model section (e.g. steak_default) */
    public FoodModel(String key, ConfigurationSection config) {
        id = key;
        for (String stateId : config.getKeys(false)) {
            if(stateId.equals("colour")) continue;
            ConfigurationSection stateConfig = config.getConfigurationSection(stateId);
            if (stateConfig != null) {
                ModelData model = new ModelData(stateConfig);
                states.put(stateId, model);
            }
        }
    }

    public String getId() {
        return id;
    }

    /** Deep copy constructor */
    public FoodModel(FoodModel other) {
        this.id = other.id;
        this.states = new HashMap<>();
        for (Map.Entry<String, ModelData> entry : other.states.entrySet()) {
            this.states.put(entry.getKey(), new ModelData(entry.getValue()));
        }
    }

    public FoodModel(ItemStack item) {
        this.id = "custom_model";
        this.states = new HashMap<>();
        ModelData modelData = new ModelData(item);
        this.states.put("default", modelData);
    }

    public ModelData getModel(FoodItem item) {
        List<String> tags = item.getCurrentTagIds();
        ModelData bestMatch = null;

        for (ModelData data : states.values()) {
            boolean tagMatch = true;

            // check if all tags in data are present in the item's tags
            for (String tag : data.getTags()) {
                if (!tags.contains(tag)) {
                    tagMatch = false;
                    break;
                }
            }

            if (tagMatch) {
                // if this is the first match or has higher weight than current best
                if (bestMatch == null || data.getWeight() > bestMatch.getWeight()) {
                    bestMatch = data;
                }
            }
        }
        if(bestMatch == null) return getFirstModel();
        return bestMatch;
    }


    public Map<String, ModelData> getStates() {
        return states;
    }

    private ModelData getFirstModel() {
        return states.values().iterator().next();
    }
}
