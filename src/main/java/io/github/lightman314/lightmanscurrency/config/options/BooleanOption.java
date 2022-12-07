package io.github.lightman314.lightmanscurrency.config.options;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class BooleanOption extends ConfigOption<Boolean> {

    private final boolean defaultValue;
    private boolean value;
    private BooleanOption(boolean defaultValue) { this.defaultValue = this.value = defaultValue; }
    public static BooleanOption create(boolean defaultValue) { return new BooleanOption(defaultValue); }

    @Override
    protected void readValue(JsonElement element) { this.value = element.getAsBoolean();}
    @Override
    protected void resetToDefaultValue() { this.value = this.defaultValue; }
    @Override
    protected void writeValue(JsonObject jsonObject, String name) { jsonObject.addProperty(name, this.value); }
    @Override
    protected Boolean getInternal() { return this.value; }
    @Override
    public Boolean getDefault() { return this.defaultValue; }

}
