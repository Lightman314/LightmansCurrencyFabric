package io.github.lightman314.lightmanscurrency.api.config.options.basic;

import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class StringOption extends ConfigOption<String> {

    public static final ConfigParser<String> PARSER = new Parser();

    protected StringOption(@NotNull Supplier<String> defaultValue) { super(defaultValue); }

    @NotNull
    public static StringOption create(@NotNull String defaultValue) { return new StringOption(() -> defaultValue); }
    public static StringOption create(@NotNull Supplier<String> defaultValue) { return new StringOption(defaultValue); }

    @NotNull
    @Override
    protected ConfigParser<String> getParser() { return PARSER; }

    private static class Parser implements ConfigParser<String>
    {
        @NotNull
        @Override
        public String tryParse(@NotNull String cleanLine) throws ConfigParsingException {
            if(cleanLine.startsWith("\"") && cleanLine.endsWith("\""))
                return cleanLine.substring(1,cleanLine.length() - 1);
            return cleanLine;
        }
        @NotNull
        @Override
        public String write(@NotNull String value) { return '"' + value + '"'; }
    }

}
