package net.tfminecraft.cooking.item;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import me.Plugins.TLibs.Objects.API.SubAPI.StringFormatter;
import net.tfminecraft.cooking.item.data.CookData;
import net.tfminecraft.cooking.item.data.OverrideData;
import net.tfminecraft.cooking.item.model.FoodModel;
import net.tfminecraft.cooking.item.model.ModelData;
import net.tfminecraft.cooking.item.tag.TagStep;
import net.tfminecraft.cooking.item.tag.TagTrack;
import net.tfminecraft.cooking.loader.ModelLoader;
import net.tfminecraft.cooking.loader.TrackLoader;
import net.tfminecraft.cooking.utils.FoodParser;
import net.tfminecraft.cooking.utils.Keys;
import net.tfminecraft.cooking.enums.Tag;

import java.util.*;

public class FoodItem {

    private final String id;
    private final String name;
    private long lastUpdate;
    private String category = "";

    private Map<String, TagTrack> tags = new HashMap<>();

    private String origin = null;

    private int amount = 1;
    private int qualityMin = 0;
    private int qualityMax = 0;

    private double baseFood;
    private double baseNutrition;

    private FoodModel model = null;

    private Map<String, OverrideData> overrides = new HashMap<>();
    private List<String> ingredients = new ArrayList<>();
    public CookData cookData;

    private String sauceName;
    private FoodItem sauce;

    public int _parsedQualMin, _parsedQualMax;


    // --------------------------------------------------------------
    // CONSTRUCTOR FROM CONFIG
    // --------------------------------------------------------------
    public FoodItem(String key, ConfigurationSection config) {
        this.id = key;
        this.name = StringFormatter.formatHex(config.getString("name", "Unknown Food"));

        this.baseFood = config.getDouble("food", 1.0);
        this.baseNutrition = config.getDouble("nutrition", 1.0);

        String modelId = config.getString("model", null);
        if (modelId != null)
            this.model = ModelLoader.getById(modelId);

        // Load device-specific cooking states
        ConfigurationSection cooking = config.getConfigurationSection("cooking-options");
        if (cooking != null) {
            cookData = new CookData(key, cooking);
        } else {
            cookData = new CookData();
        }

        // Overrides (origin-based)
        ConfigurationSection overSec = config.getConfigurationSection("overrides");
        if (overSec != null) {
            for (String originKey : overSec.getKeys(false)) {
                ConfigurationSection o = overSec.getConfigurationSection(originKey);
                if (o != null) {
                    overrides.put(
                        originKey.toUpperCase(),
                        new OverrideData(
                            o.getString("name", null),
                            o.getString("model", null)
                        )
                    );
                }
            }
        }
    }


    // --------------------------------------------------------------
    // DEEP COPY
    // --------------------------------------------------------------
    public FoodItem(FoodItem other) {
        this.id = other.id;
        this.name = other.name;
        this.category = other.category;

        this.origin = other.origin;

        this.amount = other.amount;
        this.qualityMin = other.qualityMin;
        this.qualityMax = other.qualityMax;
        this.baseFood = other.baseFood;
        this.baseNutrition = other.baseNutrition;

        this.overrides = new HashMap<>(other.overrides);
        this.ingredients = new ArrayList<>(other.ingredients);

        this.model = (other.model == null ? null : new FoodModel(other.model));

        this.cookData = new CookData(this, other.cookData);
        if (other.sauce != null) {
            this.sauce = new FoodItem(other.sauce);
            this.sauceName = other.sauceName; // copy name
        }


        this._parsedQualMin = other._parsedQualMin;
        this._parsedQualMax = other._parsedQualMax;

        this.lastUpdate = System.currentTimeMillis();

        for (TagTrack t : other.tags.values())
            this.tags.put(t.getId(), new TagTrack(t));
        if(this.canBeCooked() && !hasTagTrack("cooked")) addOrModifyTrack(TrackLoader.getByString("cooked"));
    }


    // --------------------------------------------------------------
    // AGE UPDATE
    // --------------------------------------------------------------
    public ItemStack updateAge(ItemStack item) {
        updateAge();
        ItemMeta m = item.getItemMeta();
        var pdc = m.getPersistentDataContainer();
        pdc.set(Keys.LAST_UPDATE, PersistentDataType.LONG, System.currentTimeMillis());
        item.setItemMeta(m);
        return item;
    }
    public void updateAge() {
        long now = System.currentTimeMillis();

        long elapsed = now - lastUpdate;
        if (elapsed <= 0) return;

        int seconds = (int) (elapsed / 1000);

        for (TagTrack track : tags.values()) {
            if (!track.isAgeable()) continue;
            track.setValue(track.getValue() + seconds);
        }

        lastUpdate = now;
    }


    // --------------------------------------------------------------
    // GETTERS / SETTERS
    // --------------------------------------------------------------
    public String getId() { return id; }
    public String getName() { return name; }

    public String getCategory() { return category; }
    public void setCategory(String cat) { this.category = cat; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
    public CookData getCookData() { return cookData; }

    public int getQualityMin() { return qualityMin; }
    public int getQualityMax() { return qualityMax; }
    public void setQualityRange(int min, int max) {
        this.qualityMin = min;
        this.qualityMax = max;
    }

    public double getBaseFood() { return baseFood; }
    public double getBaseNutrition() { return baseNutrition; }

    public long getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(long ts) { this.lastUpdate = ts; }

    public void setSauceName(String name) { this.sauceName = name; }
    public String getSauceName() { return sauceName; }
    public boolean hasSauceName() { return sauceName != null && !sauceName.isEmpty(); }

    public void setSauce(FoodItem s) { this.sauce = s; }
    public FoodItem getSauce() { return sauce; }
    public boolean hasSauce() { return sauce != null; }


    public FoodModel getModel() {
        if(overrides.isEmpty()) return model;
        for(Map.Entry<String, OverrideData> override : overrides.entrySet()) {
            if(override.getValue() == null) continue;
            if(!override.getKey().equalsIgnoreCase(origin)) continue;
            FoodModel ov = ModelLoader.getById(override.getValue().getModel());
            if (ov != null) {
                return ov;
            }
        }
        return model;
    }

    public void setModel(FoodModel foodModel) {
        model = foodModel;
    }

    public ModelData getModelData() {
        ModelData data = getModel().getModel(this);
        return data != null ? data : model.getModel(this);
    }

    public Map<String, OverrideData> getOverrides() { return overrides; }
    public List<String> getIngredients() { return ingredients; }

    public void addIngredient(String ingredient) {
        if(ingredients.contains(ingredient)) return;
        ingredients.add(ingredient); 
    }

    public void setOrigin(String o) { origin = o; }
    public String getOrigin() { return origin; }

    public List<TagTrack> getTagTracks() {
        List<TagTrack> list = new ArrayList<>(tags.values());
        list.sort(new TagSort());
        return list;
    }

    private class TagSort implements Comparator<TagTrack> {
        public int compare(TagTrack t1, TagTrack t2) {
            if (t1.getIndex() < t2.getIndex()) return -1;
            if (t1.getIndex() > t2.getIndex()) return 1;
            return 0;
        }
    }

    public TagTrack getTagTrack(String s) { 
        return tags.get(s);
    }

    public void addOrModifyTrack(TagTrack t) {
        updateAge();
        if(hasTagTrack(t.getId())) {
            getTagTrack(t.getId()).setValue(t.getValue());
        } else {
            tags.put(t.getId(), new TagTrack(t));
        }
    }

    public void addTagTrack(String trackId) {
        updateAge();
        tags.put(trackId, new TagTrack(TrackLoader.getByString(trackId)));
    }

    public boolean hasTagTrack(String tag) {
        return tags.containsKey(tag);
    }

    public List<TagStep> getCurrentTags() {
        List<TagStep> current = new ArrayList<>();
        for(TagTrack track : tags.values()) {
            current.add(track.getCurrentStep());
        }
        return current;
    }
    public List<String> getCurrentTagIds() {
        List<String> current = new ArrayList<>();
        for(TagTrack track : tags.values()) {
            current.add(track.getCurrentStep().getId());
        }
        return current;
    }

    public boolean sameTags(FoodItem other) {
        List<String> myTags = getCurrentTagIds();
        List<String> otherTags = other.getCurrentTagIds();
        if(myTags.size() != otherTags.size()) return false;
        for(String t : myTags) {
            if(!otherTags.contains(t)) return false;
        }
        return true;
    }

    public boolean hasTag(Tag tag) {
        for(TagStep t : getCurrentTags()) {
            if(t.getTag().equals(tag)) return true;
        }
        return false;
    }

    //state stuff
    public boolean canBeCooked() {
        return !cookData.getParameters().isEmpty();
    }


    // --------------------------------------------------------------
    // FINAL VALUES (QUALITY + TAGS + COOK STATE)
    // --------------------------------------------------------------
    public double getFinalFood() {
        return applyMultipliers(baseFood + (hasSauce() ? getSauce().getFinalFood() : 0), 0);
    }

    public double getFinalNutrition() {
        return applyMultipliers(baseNutrition + (hasSauce() ? getSauce().getFinalNutrition() : 0), 1);
    }

    private double applyMultipliers(double base, int type) {

        double qualityMultiplier = 1.0 + (getQualityMin() - 1) * 0.20;

        double result = base * qualityMultiplier;

        for (TagTrack t : tags.values()) {
            TagStep step = t.getCurrentStep();
            if (step != null) {
                if (type == 1) result *= step.getNutritionMultiplier();
                else           result *= step.getFoodMultiplier();
            }
        }

        return Math.round(result * 10.0) / 10.0;
    }


    // --------------------------------------------------------------
    // LOAD FROM PDC
    // --------------------------------------------------------------
    public static FoodItem fromItem(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return null;

        ItemMeta meta = stack.getItemMeta();
        var pdc = meta.getPersistentDataContainer();

        String id = pdc.get(Keys.FOOD_ID, PersistentDataType.STRING);
        if (id == null) return null;

        FoodItem base = net.tfminecraft.cooking.loader.FoodLoader.getByString(id);
        if (base == null) return null;

        FoodItem out = new FoodItem(base);

        String cat = pdc.get(Keys.CATEGORY, PersistentDataType.STRING);
        if (cat != null) out.setCategory(cat);

        String originStr = pdc.get(Keys.ORIGIN, PersistentDataType.STRING);
        if (originStr != null) out.setOrigin(originStr);

        Integer qual = pdc.get(Keys.QUALITY, PersistentDataType.INTEGER);
        if (qual != null) out.setQualityRange(qual, qual);

        // Tag tracks
        String tagData = pdc.get(Keys.TAGS, PersistentDataType.STRING);
        if (tagData != null && !tagData.isEmpty()) {

            for (String entry : tagData.split(";")) {
                if (!entry.contains(".")) continue;

                String[] kv = entry.split("\\.");
                if (kv.length != 2) continue;

                String trackId = kv[0];
                int value;
                try { value = Integer.parseInt(kv[1]); }
                catch (Exception e) { continue; }

                TagTrack baseTrack = TrackLoader.getByString(trackId);
                if (baseTrack == null) continue;

                TagTrack t = new TagTrack(baseTrack);
                t.setValue(value);
                out.addOrModifyTrack(t);
            }
        }

        Long lastUpdate = pdc.get(Keys.LAST_UPDATE, PersistentDataType.LONG);
        if (lastUpdate != null)
            out.setLastUpdate(lastUpdate);
        else {
            long now = System.currentTimeMillis();
            out.setLastUpdate(now);
            pdc.set(Keys.LAST_UPDATE, PersistentDataType.LONG, now);
            stack.setItemMeta(meta);
        }

        // --- Load sauce ---
        String sauceData = pdc.get(Keys.SAUCE, PersistentDataType.STRING);
        if (sauceData != null && !sauceData.isEmpty()) {

            // Full nested parse
            FoodParser.Result sauceResult = FoodParser.parse(sauceData);

            if (sauceResult != null && sauceResult.template != null) {
                FoodItem sauceItem = sauceResult.template;

                // Restore sauce category explicitly (FoodParser.parse preserves it)
                out.setSauce(sauceItem);

                // Debug optional
                // Bukkit.getLogger().info("Loaded sauce: " + sauceItem.getId());
            }
        }

        // --- Load sauce name ---
        String sauceNameData = pdc.get(Keys.SAUCE_NAME, PersistentDataType.STRING);
        if (sauceNameData != null && !sauceNameData.isEmpty()) {
            out.setSauceName(sauceNameData);
        }


        if(out.getModel() == null) {
            out.model = new FoodModel(stack);
        }

        return out;
    }
}
