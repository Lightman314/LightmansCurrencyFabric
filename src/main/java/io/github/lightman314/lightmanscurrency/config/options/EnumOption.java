package io.github.lightman314.lightmanscurrency.config.options;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;

import java.util.List;

public class EnumOption<E extends Enum<E>> extends ConfigOption<E> {

    private final E[] possibleValues;
    private final E defaultValue;
    private E value;
    private EnumOption(E defaultValue, E[] possibleValues) { this.defaultValue = this.value = defaultValue; this.possibleValues = possibleValues; }
    public static <T extends Enum<T>> EnumOption<T> create(T defaultValue, T[] possibleValues) { return new EnumOption<>(defaultValue, possibleValues); }

    public void appendPendingComments(List<String> pendingComments) {
        StringBuilder possibleValues = new StringBuilder();
        for(E val : this.possibleValues)
        {
            if(!possibleValues.isEmpty())
                possibleValues.append(", ");
            possibleValues.append(val.name());
        }
        pendingComments.add("Options: " + possibleValues);
    }

    @Override
    protected void readValue(JsonElement element) { this.value = EnumUtil.enumFromString(element.getAsString(), this.possibleValues, this.defaultValue); }
    @Override
    protected void resetToDefaultValue() { this.value = this.defaultValue; }
    @Override
    protected void writeValue(JsonObject jsonObject, String name) { jsonObject.addProperty(name, this.value.toString()); }
    @Override
    protected E getInternal() { return this.value; }
    @Override
    public E getDefault() { return this.defaultValue; }

}
