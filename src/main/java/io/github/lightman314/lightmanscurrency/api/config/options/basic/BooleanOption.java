package io.github.lightman314.lightmanscurrency.api.config.options.basic;

import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class BooleanOption extends ConfigOption<Boolean> {

    public static final ConfigParser<Boolean> PARSER = new Parser();

    protected BooleanOption(@NotNull Supplier<Boolean> defaultValue) { super(defaultValue); }

    @NotNull
    @Override
    protected ConfigParser<Boolean> getParser() { return PARSER; }

    public static BooleanOption createTrue() { return create(() -> true); }
    public static BooleanOption createFalse() { return create(() -> false); }
    public static BooleanOption create(@NotNull Supplier<Boolean> defaultValue) { return new BooleanOption(defaultValue); }

    private static class Parser implements ConfigParser<Boolean>
    {
        @NotNull
        @Override
        public Boolean tryParse(@NotNull String cleanLine) throws ConfigParsingException { return Boolean.parseBoolean(cleanLine); }
        @NotNull
        @Override
        public String write(@NotNull Boolean value) { return Boolean.toString(value); }
    }

}
