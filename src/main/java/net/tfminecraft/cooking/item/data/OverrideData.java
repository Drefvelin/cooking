package net.tfminecraft.cooking.item.data;

import me.Plugins.TLibs.Objects.API.SubAPI.StringFormatter;

public class OverrideData {
    private String name;
    private String model;
    private String carveSequence;

    public OverrideData(String name, String model, String carveSequence) {
        if(name != null) this.name = StringFormatter.formatHex(name);
        this.model = model;
        this.carveSequence = carveSequence;
    }

    public String getName() {
        return name;
    }
    public String getModel() {
        return model;
    }
    public String getCarveSequence() {
        return carveSequence;
    }
}
