package net.tfminecraft.cooking.item.tag;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

public class TagTrack {
    private String id;
    private boolean ageable;
    private int value;
    private int index;
    private final List<TagStep> steps = new ArrayList<>();

    public TagTrack(String key, int i, ConfigurationSection config) {
        this.id = key;
        this.value = 0;
        index = i;
        this.ageable = config.getBoolean("ageable", false);
        for (String id : config.getKeys(false)) {
            if(id.equalsIgnoreCase("ageable")) continue;
            ConfigurationSection section = config.getConfigurationSection(id);
            steps.add(new TagStep(id, section));
        }
    }

    public TagTrack(TagTrack other) {
        this.id = other.id;
        this.value = other.value;
        this.ageable = other.ageable;
        this.index = other.index;
        this.steps.addAll(other.steps);
    }

    public int getIndex() { return index; }
    public String getId() { return id; }
    public int getValue() { return value; }
    public List<TagStep> getSteps() { return steps; }
    public void setValue(int value) {
        if (ageable && steps.size() > 0) {
            TagStep last = steps.get(steps.size() - 1);
            if (this.value >= last.getRequiredValue()) return;
        }
        this.value = value;
    }
    public boolean isAgeable() { return ageable; }

    public TagStep getCurrentStep() {
        TagStep current = null;
        for (TagStep step : steps) {
            if (value >= step.getRequiredValue()) {
                current = step;
            } else {
                break;
            }
        }
        if(current == null) return steps.get(0);
        return current;
    }
}
