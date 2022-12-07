package io.github.lightman314.lightmanscurrency.config.options;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ItemOption extends ConfigOption<Item> {

    private final Item defaultValue;
    private Identifier value;
    private ItemOption(Item defaultValue) { this.defaultValue = defaultValue; this.resetToDefaultValue(); }
    public static ItemOption create(ItemConvertible defaultValue) { return new ItemOption(defaultValue.asItem()); }

    @Override
    protected void readValue(JsonElement element) { this.value = new Identifier(element.getAsString()); }
    @Override
    protected void resetToDefaultValue() { this.value = Registry.ITEM.getId(this.defaultValue); }
    @Override
    protected void writeValue(JsonObject jsonObject, String name) { jsonObject.addProperty(name, this.value.toString()); }
    @Override
    protected Item getInternal() { return Registry.ITEM.getOrEmpty(this.value).orElse(this.defaultValue); }
    @Override
    public Item getDefault() { return this.defaultValue; }

}
