package io.github.lightman314.lightmanscurrency.config.options.custom;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.config.options.custom.values.VillagerItemOverride;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class VillagerItemOverrideListOption extends ConfigOption<List<VillagerItemOverride>> {

    private final List<VillagerItemOverride> defaultValue;
    private List<VillagerItemOverride> value;
    private VillagerItemOverrideListOption(List<VillagerItemOverride> defaultValue) { this.defaultValue = defaultValue; this.resetToDefaultValue(); }
    public static VillagerItemOverrideListOption create(VillagerItemOverride... defaultValues) { return new VillagerItemOverrideListOption(Lists.newArrayList(defaultValues)); }

    @Override
    protected void readValue(JsonElement element) {
        JsonArray list = element.getAsJsonArray();
        this.value = new ArrayList<>();
        for(int i = 0; i < list.size(); ++i)
        {
            JsonObject entry = list.get(i).getAsJsonObject();
            Identifier villagerID = new Identifier(entry.get("Villager").getAsString());
            Identifier newItem = new Identifier(entry.get("Coin").getAsString());
            this.value.add(VillagerItemOverride.of(villagerID, newItem));
        }
    }
    @Override
    protected void resetToDefaultValue() { this.value = new ArrayList<>(this.defaultValue); }
    @Override
    protected void writeValue(JsonObject jsonObject, String name) {
        JsonArray list = new JsonArray();
        for(VillagerItemOverride val : this.value)
        {
            JsonObject entry = new JsonObject();
            entry.addProperty("Villager", val.villagerType.toString());
            entry.addProperty("Coin", val.newItemID.toString());
            list.add(entry);
        }
        jsonObject.add(name, list);
    }
    @Override
    protected List<VillagerItemOverride> getInternal() {
        return new ArrayList<>(this.value);
    }
    @Override
    public List<VillagerItemOverride> getDefault() {
        return new ArrayList<>(this.defaultValue);
    }

}
