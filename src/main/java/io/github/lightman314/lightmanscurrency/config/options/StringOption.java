package io.github.lightman314.lightmanscurrency.config.options;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class StringOption extends ConfigOption<String> {

    private final String defaultValue;
    private String value;
    private StringOption(String defaultValue) { this.defaultValue = this.value = defaultValue; }
    public static StringOption create(String defaultValue) { return new StringOption(defaultValue); }
    @Override
    protected void readValue(JsonElement element) { this.value = element.getAsString(); }
    @Override
    protected void resetToDefaultValue() { this.value = this.defaultValue; }
    @Override
    protected void writeValue(JsonObject jsonObject, String name) { jsonObject.addProperty(name, this.value); }
    @Override
    protected String getInternal() { return this.value; }
    @Override
    public String getDefault() { return this.defaultValue; }

}
