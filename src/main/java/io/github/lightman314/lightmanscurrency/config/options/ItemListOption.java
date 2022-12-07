package io.github.lightman314.lightmanscurrency.config.options;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;

public class ItemListOption extends ConfigOption<List<Item>> {

    private final List<Item> defaultValue;
    private List<Identifier> value;
    private ItemListOption(List<Item> defaultValue) { this.defaultValue = defaultValue; this.resetToDefaultValue(); }
    public static ItemListOption create() { return create(new ArrayList<>()); }
    public static ItemListOption create(List<Item> defaultValue) { return new ItemListOption(defaultValue); }

    @Override
    protected void readValue(JsonElement element) {
        this.value = new ArrayList<>();
        JsonArray list = element.getAsJsonArray();
        for(int i = 0; i < list.size(); ++i)
            this.value.add(new Identifier(list.get(i).getAsString()));
    }
    @Override
    protected void resetToDefaultValue() {
        this.value = new ArrayList<>();
        for(Item item : this.defaultValue)
        {
            if(item != null)
                this.value.add(Registry.ITEM.getId(item));
        }
    }
    @Override
    protected void writeValue(JsonObject jsonObject, String name) {
        JsonArray list = new JsonArray();
        for(Identifier item : this.value)
            list.add(item.toString());
    }
    @Override
    protected List<Item> getInternal() {
        List<Item> val = new ArrayList<>();
        for(Identifier id : this.value)
            Registry.ITEM.getOrEmpty(id).ifPresent(val::add);
        return val;
    }
    @Override
    public List<Item> getDefault() { return new ArrayList<>(this.defaultValue); }

}
