package io.github.lightman314.lightmanscurrency.config.options;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.util.MathUtil;

import java.util.List;

public class FloatOption extends ConfigOption<Float> {

    private final float defaultValue;
    private float value;
    private final float minValue;
    private final float maxValue;
    private FloatOption(float defaultValue, float minValue, float maxValue) { this.defaultValue = defaultValue; this.minValue = minValue; this.maxValue = maxValue; this.resetToDefaultValue(); }
    public static FloatOption create(float defaultValue) { return create(defaultValue, Float.MIN_VALUE, Float.MAX_VALUE); }
    public static FloatOption create(float defaultValue, float minValue, float maxValue) { return new FloatOption(defaultValue, minValue, maxValue); }

    @Override
    public void appendPendingComments(List<String> pendingComments) {
        if(this.minValue != Float.MIN_VALUE || this.maxValue != Float.MAX_VALUE)
        {
            String minText = this.minValue != Float.MIN_VALUE ? String.valueOf(this.minValue) : "-Infinity";
            String maxText = this.maxValue != Float.MAX_VALUE ? String.valueOf(this.maxValue) : "Infinity";
            pendingComments.add("Range: " + minText + " -> " + maxText);
        }
    }
    @Override
    protected void readValue(JsonElement element) { this.value = MathUtil.clamp(element.getAsFloat(), this.minValue, this.maxValue); }
    @Override
    protected void resetToDefaultValue() { this.value = MathUtil.clamp(this.defaultValue, this.minValue, this.maxValue);}
    @Override
    protected void writeValue(JsonObject jsonObject, String name) { jsonObject.addProperty(name, this.value);}
    @Override
    protected Float getInternal() { return this.value; }
    @Override
    public Float getDefault() { return MathUtil.clamp(this.defaultValue, this.minValue, this.maxValue); }
}
