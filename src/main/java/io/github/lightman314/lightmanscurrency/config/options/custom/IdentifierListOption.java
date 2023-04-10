package io.github.lightman314.lightmanscurrency.config.options.custom;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.config.options.ConfigOption;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class IdentifierListOption extends ConfigOption<List<Identifier>> {

    private final List<Identifier> defaultValue;
    private List<Identifier> value;
    private IdentifierListOption(List<Identifier> defaultValue) { this.defaultValue = defaultValue; this.resetToDefaultValue(); }

    public static IdentifierListOption create(Identifier... defaultValue) { return create(Lists.newArrayList(defaultValue)); }
    public static IdentifierListOption create(List<Identifier> defaultValue) { return new IdentifierListOption(defaultValue); }

    @Override
    protected void readValue(JsonElement entry) {
        JsonArray list = entry.getAsJsonArray();
        this.value = new ArrayList<>();
        for(int i = 0; i < list.size(); ++i)
        {
            try{ this.value.add(new Identifier(list.get(i).getAsString()));
            } catch(Throwable t) {
                LightmansCurrency.LogError("Error reading value '" + this.getName() + "[" + i + "]' from the config file.", t);
            }
        }
    }

    @Override
    protected void resetToDefaultValue() { this.value = new ArrayList<>(this.defaultValue); }
    @Override
    protected void writeValue(JsonObject json, String name) {
        JsonArray list = new JsonArray();
        for(Identifier val : this.value)
            list.add(val.toString());
        json.add(name, list);
    }
    @Override
    protected List<Identifier> getInternal() { return new ArrayList<>(this.value); }
    @Override
    public List<Identifier> getDefault() { return new ArrayList<>(this.defaultValue); }

}
