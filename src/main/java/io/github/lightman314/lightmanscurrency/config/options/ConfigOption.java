package io.github.lightman314.lightmanscurrency.config.options;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.config.Config;
import io.github.lightman314.lightmanscurrency.config.pathing.IConfigAction;
import io.github.lightman314.lightmanscurrency.config.pathing.JsonStack;

import java.util.List;
import java.util.function.Supplier;

public abstract class ConfigOption<T> implements IConfigAction, Supplier<T> {

    private String name = null;
    protected String getName() { return this.name; }
    private Config parent;
    public final void init(String name, Config parent) { if(this.name == null) this.name = name; if(this.parent == null) this.parent = parent; }

    @Override
    public void writeAction(JsonStack json) { this.PlaceValue(json.get()); }
    @Override
    public void readAction(JsonStack json) { this.CollectValue(json.get()); }

    public void appendPendingComments(List<String> pendingComments) { }

    public final void CollectValue(JsonObject json)
    {
        if(json.has(this.name))
        {
            try{ this.readValue(json.get(this.name));
            } catch(Throwable t) {LightmansCurrency.LogError("Error reading value '" + this.name + "' from the config file.", t); this.resetToDefaultValue(); }
        }
        else
            this.resetToDefaultValue();
    }

    protected abstract void readValue(JsonElement entry);
    protected abstract void resetToDefaultValue();
    public final void PlaceValue(JsonObject json)
    {
        try{ this.writeValue(json, this.name);
        } catch (Throwable t) { LightmansCurrency.LogError("Error writing value '" + this.name + "' to the config file!", t); }
    }
    protected abstract void writeValue(JsonObject json, String name);
    public final T get() {
        if(this.parent != null && !this.parent.isLoaded())
            this.parent.reloadFromFile();
        return this.getInternal();
    }
    protected abstract T getInternal();

    public abstract T getDefault();

}
