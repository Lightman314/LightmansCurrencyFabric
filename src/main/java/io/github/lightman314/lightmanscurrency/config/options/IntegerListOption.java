package io.github.lightman314.lightmanscurrency.config.options;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;

import java.util.ArrayList;
import java.util.List;

public class IntegerListOption extends ConfigOption<List<Integer>> {

    private final List<Integer> defaultValue;
    private List<Integer> value;
    public IntegerListOption() { this(new ArrayList<>()); }
    private IntegerListOption(List<Integer> defaultValue) { this.defaultValue = defaultValue; this.resetToDefaultValue(); }
    public static IntegerListOption create() { return create(new ArrayList<>()); }
    public static IntegerListOption create(List<Integer> defaultValue) { return new IntegerListOption(defaultValue); }

    @Override
    protected void readValue(JsonElement element) {
        JsonArray array = element.getAsJsonArray();
        this.value = new ArrayList<>();
        for(int i = 0; i < array.size(); ++i)
        {
            try{ this.value.add(array.get(i).getAsInt());
            } catch (Throwable t) { LightmansCurrency.LogError("Error reading value '" + this.getName() + "[" + i + "]' from the config file.", t); }
        }
    }
    @Override
    protected void resetToDefaultValue() {
        this.value = new ArrayList<>();
        this.value.addAll(this.defaultValue);
    }
    @Override
    protected void writeValue(JsonObject jsonObject, String name) {
        JsonArray list = new JsonArray();
        for(Integer val : this.value)
            list.add(val);
        jsonObject.add(name, list);
    }
    @Override
    protected List<Integer> getInternal() { return new ArrayList<>(this.value); }
    @Override
    public List<Integer> getDefault() { return new ArrayList<>(this.defaultValue); }

}
