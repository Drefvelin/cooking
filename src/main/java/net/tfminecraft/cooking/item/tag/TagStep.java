package net.tfminecraft.cooking.item.tag;

import org.bukkit.configuration.ConfigurationSection;
import net.tfminecraft.cooking.enums.Tag;

import me.Plugins.TLibs.Objects.API.SubAPI.StringFormatter;

public class TagStep {
    private final String id;
    private Tag tag;
    private final String name;
    private final long requiredValue;

    private final double foodMultiplier;
    private final double nutritionMultiplier;

    public TagStep(String key, ConfigurationSection config) {
        this.id = key;
        this.name = StringFormatter.formatHex(config.getString("name", "Tag"));
        try {
            this.tag = Tag.valueOf(key.toUpperCase());
        } catch (Exception e) {
            this.tag = Tag.CUSTOM;
        }
        this.requiredValue = config.getInt("value", 0);
        this.foodMultiplier = config.getDouble("food-mult", 1.0);
        this.nutritionMultiplier = config.getDouble("nutrition-mult", 1.0);
    }

    public Tag getTag() {
        return tag;
    }

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public long getRequiredValue() {
        return requiredValue;
    }


    public double getFoodMultiplier() { return foodMultiplier; }
    public double getNutritionMultiplier() { return nutritionMultiplier; }
}

