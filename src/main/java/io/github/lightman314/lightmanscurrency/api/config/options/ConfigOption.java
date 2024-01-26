package io.github.lightman314.lightmanscurrency.api.config.options;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class ConfigOption<T> implements Supplier<T> {

    private List<String> comments = new ArrayList<>();
    public final void setComments(@NotNull List<String> comments) {
        if(this.comments instanceof ArrayList<String>)
            this.comments = ImmutableList.copyOf(comments);
        else
            LightmansCurrency.LogWarning("Attempted to define an options comments twice!");
    }
    private ConfigFile parent = null;
    public final void setParent(@NotNull ConfigFile parent) {
        if(this.parent == null)
            this.parent = parent;
        else
            LightmansCurrency.LogWarning("Attempted to define an options parent twice!");
    }
    @NotNull
    public final List<String> getComments() {
        String c = this.bonusComment();
        if(c == null)
            return this.comments;
        List<String> cl = new ArrayList<>(this.comments);
        cl.add(c);
        return cl;
    }

    private final Supplier<T> defaultValue;
    private T currentValue = null;
    private T syncedValue = null;

    protected ConfigOption(@NotNull Supplier<T> defaultValue) { this.defaultValue = defaultValue; }

    @Nullable
    protected String bonusComment() { return null; }

    @NotNull
    protected abstract ConfigParser<T> getParser();

    public final void load(@NotNull String optionID, @NotNull String line, boolean syncPacket) {
        line = cleanWhitespace(line);
        try {
            T val = this.getParser().tryParse(line);
            if(syncPacket)
                this.syncedValue = val;
            else
                this.currentValue = val;
        } catch (ConfigParsingException e) {
            LightmansCurrency.LogError("Error parsing " + optionID + "!", e);
            this.currentValue = this.defaultValue.get();
        }
    }

    public final void clear() { this.currentValue = null; }
    public final boolean isLoaded() { return this.currentValue != null; }
    public final void clearSyncedData() { this.syncedValue = null; }

    @NotNull
    public final String write() { return this.getParser().write(this.getCurrentValue()); }

    public final void write(@NotNull String name, @NotNull Consumer<String> writer) {
        ConfigFile.writeComments(this.getComments(), writer);
        writer.accept(name + "=" + this.write());
        writer.accept("");
    }

    @NotNull
    public static String cleanWhitespace(@NotNull String line) {
        StringBuilder result = new StringBuilder();
        boolean start = true;
        StringBuilder temp = new StringBuilder();
        for(int i = 0; i < line.length(); ++i)
        {
            char c = line.charAt(i);
            if(Character.isWhitespace(c))
            {
                if(!start)
                    temp.append(c);
            }
            else
            {
                if(start)
                    start = false;
                if(!temp.isEmpty())
                {
                    result.append(temp);
                    temp = new StringBuilder();
                }
                result.append(c);
            }
        }
        return result.toString();
    }

    @Override
    @NotNull
    public T get() {
        if(this.syncedValue != null)
            return this.syncedValue;
        return this.getCurrentValue();
    }

    public void set(@NotNull T newValue)
    {
        if(this.currentValue == newValue)
            return;
        this.currentValue = Objects.requireNonNull(newValue);
        if(this.parent != null)
            this.parent.writeToFile();
    }

    @NotNull
    protected final T getCurrentValue() {
        if(this.currentValue == null)
            return this.defaultValue.get();
        return this.currentValue;
    }

}
