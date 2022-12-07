package io.github.lightman314.lightmanscurrency.config.pathing;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;

import java.util.ArrayList;
import java.util.List;

public class JsonStack {

    private final List<JsonObject> blocks = new ArrayList<>();

    private final boolean reading;
    /**
     * Constructor for when in writing mode.
     */
    public JsonStack() { this.reading = false; this.blocks.add(new JsonObject()); }

    /**
     * Constructor for when in loading mode.
     */
    public JsonStack(JsonObject root) { this.reading = true; this.blocks.add(root); }

    public final JsonObject getRoot() { return this.blocks.get(0); }

    public final JsonObject get() { return this.blocks.get(this.blocks.size() - 1); }
    public final JsonStack push(String path) {
        JsonObject current = this.get();
        if(this.reading)
        {
            //If loading, try to find the actual child section
            if(current.has(path))
            {
                try{
                    JsonObject newJson = current.get(path).getAsJsonObject();
                    this.blocks.add(newJson);
                } catch(Throwable t) {
                    this.blocks.add(new JsonObject());
                }
            }
            else
                this.blocks.add(new JsonObject());
        }
        else
        {
            JsonObject newJson = new JsonObject();
            current.add(path, newJson);
            this.blocks.add(newJson);
        }
        return this;
    }
    public final int level() { return this.blocks.size() - 1; }
    public final JsonStack pop() {
        if(this.level() > 0)
            this.blocks.remove(this.blocks.size() - 1);
        else
            LightmansCurrency.LogWarning("Tried to pop past the root json block.");
        return this;
    }



}
