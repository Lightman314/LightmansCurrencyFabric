package io.github.lightman314.lightmanscurrency.config.options.custom;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.config.options.custom.values.ScreenPosition;
import io.github.lightman314.lightmanscurrency.util.MathUtil;

public class ScreenPositionOption extends ConfigOption<ScreenPosition> {

    private final ScreenPosition defaultValue;
    private ScreenPosition value;
    private final int minValue;
    private final int maxValue;
    private ScreenPositionOption(int defaultX, int defaultY, int minValue, int maxValue) { this.defaultValue = ScreenPosition.of(defaultX, defaultY); this.minValue = minValue; this.maxValue = maxValue; this.resetToDefaultValue(); }


    public static ScreenPositionOption create(int defaultX, int defaultY) { return create(defaultX, defaultY, Integer.MIN_VALUE, Integer.MAX_VALUE); }
    public static ScreenPositionOption create(int defaultX, int defaultY, int minValue, int maxValue) { return new ScreenPositionOption(defaultX, defaultY, minValue, maxValue); }

    @Override
    protected void readValue(JsonElement element) {
        JsonObject section = element.getAsJsonObject();
        int x = section.get("x").getAsInt();
        int y = section.get("y").getAsInt();
        this.value = ScreenPosition.of(MathUtil.clamp(x, this.minValue, this.maxValue), MathUtil.clamp(y, this.minValue, this.maxValue));
    }
    @Override
    protected void resetToDefaultValue() { this.value = ScreenPosition.of(MathUtil.clamp(this.defaultValue.x, this.minValue, this.maxValue), MathUtil.clamp(this.defaultValue.y, this.minValue, this.maxValue)); }
    @Override
    protected void writeValue(JsonObject jsonObject, String name) {
        JsonObject section = new JsonObject();
        section.addProperty("x", this.value.x);
        section.addProperty("y", this.value.y);
        jsonObject.add(name, section);
    }
    @Override
    protected ScreenPosition getInternal() { return this.value; }
    @Override
    public ScreenPosition getDefault() { return this.defaultValue; }

}
