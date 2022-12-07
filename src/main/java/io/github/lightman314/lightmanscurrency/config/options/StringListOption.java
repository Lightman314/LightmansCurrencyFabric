package io.github.lightman314.lightmanscurrency.config.options;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class StringListOption extends ConfigOption<List<String>> {

    private final List<String> defaultValue;
    private List<String> value;
    private StringListOption(List<String> defaultValue) { this.defaultValue = defaultValue; this.resetToDefaultValue(); }
    public static StringListOption create() { return create(new ArrayList<>()); }
    public static StringListOption create(List<String> defaultValue) { return new StringListOption(defaultValue); }

    @Override
    protected void readValue(JsonElement element) {
        JsonArray list = element.getAsJsonArray();
        this.value = new ArrayList<>();
        for(int i = 0; i < list.size(); ++i)
            this.value.add(list.get(i).getAsString());
    }
    @Override
    protected void resetToDefaultValue() {
        this.value = new ArrayList<>();
        this.value.addAll(this.defaultValue);
    }
    @Override
    protected void writeValue(JsonObject jsonObject, String name) {
        JsonArray list = new JsonArray();
        for(String val : this.value)
            list.add(val);
    }
    @Override
    protected List<String> getInternal() { return new ArrayList<>(this.value); }
    @Override
    public List<String> getDefault() { return new ArrayList<>(this.defaultValue); }

}
