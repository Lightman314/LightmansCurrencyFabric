package io.github.lightman314.lightmanscurrency.config.options;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.util.MathUtil;

import java.util.List;

public class IntegerOption extends ConfigOption<Integer> {

    private final int defaultValue;
    private int value;
    private final int minValue;
    private final int maxValue;
    private IntegerOption(int defaultValue, int minValue, int maxValue) { this.defaultValue = defaultValue; this.minValue = minValue; this.maxValue = maxValue; this.resetToDefaultValue(); }
    public static IntegerOption create(int defaultValue) { return create(defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE); }
    public static IntegerOption create(int defaultValue, int minValue, int maxValue) { return new IntegerOption(defaultValue, minValue, maxValue); }

    @Override
    public void appendPendingComments(List<String> pendingComments) {
        if(this.minValue != Integer.MIN_VALUE || this.maxValue != Integer.MAX_VALUE)
        {
            String minText = this.minValue != Integer.MIN_VALUE ? String.valueOf(this.minValue) : "-Infinity";
            String maxText = this.maxValue != Integer.MAX_VALUE ? String.valueOf(this.maxValue) : "Infinity";
            pendingComments.add("Range: " + minText + " -> " + maxText);
        }
    }
    @Override
    protected void readValue(JsonElement element) { this.value = MathUtil.clamp(element.getAsInt(), this.minValue, this.maxValue); }
    @Override
    protected void resetToDefaultValue() { this.value = MathUtil.clamp(this.defaultValue, this.minValue, this.maxValue); }
    @Override
    protected void writeValue(JsonObject jsonObject, String name) { jsonObject.addProperty(name, this.value); }
    @Override
    protected Integer getInternal() { return this.value; }
    @Override
    public Integer getDefault() { return MathUtil.clamp(this.defaultValue, this.minValue, this.maxValue); }

}
