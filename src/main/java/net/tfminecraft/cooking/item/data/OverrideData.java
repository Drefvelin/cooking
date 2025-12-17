package net.tfminecraft.cooking.item.data;

import me.Plugins.TLibs.Objects.API.SubAPI.StringFormatter;

public class OverrideData {
    private String name;
    private String model;

    public OverrideData(String name, String model) {
        if(name != null) this.name = StringFormatter.formatHex(name);
        this.model = model;
    }

    public String getName() {
        return name;
    }
    public String getModel() {
        return model;
    }
}

