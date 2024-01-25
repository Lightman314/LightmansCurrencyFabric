package io.github.lightman314.lightmanscurrency.api.config.options.basic;

import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class EnumOption<T extends Enum<T>> extends ConfigOption<T> {

    public static <T extends Enum<T>> ConfigParser<T> buildParser(@NotNull Class<T> clazz) { return new EnumParser<>(clazz); }

    private final ConfigParser<T> parser;
    private final Class<T> clazz;
    protected EnumOption(@NotNull Supplier<T> defaultValue, Class<T> clazz) { super(defaultValue); this.parser = buildParser(clazz); this.clazz = clazz; }

    @NotNull
    @Override
    protected ConfigParser<T> getParser() { return this.parser; }

    @Nullable
    @Override
    protected String bonusComment() {
        StringBuilder builder = new StringBuilder("Options: ");
        boolean comma = false;
        for(T option : clazz.getEnumConstants())
        {
            if(comma)
                builder.append(", ");
            else
                comma = true;
            builder.append(option.name());
        }
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> EnumOption<T> create(@NotNull T defaultValue) { return new EnumOption<>(() -> defaultValue, (Class<T>)defaultValue.getClass()); }
    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> EnumOption<T> create(@NotNull Supplier<T> defaultValue, @NotNull Class<?> clazz) { return new EnumOption<>(defaultValue, (Class<T>)clazz); }

    private static class EnumParser<T extends Enum<T>> implements ConfigParser<T>
    {
        private final Class<T> clazz;
        private EnumParser(@NotNull Class<T> clazz) { this.clazz = clazz; }

        @NotNull
        @Override
        public T tryParse(@NotNull String cleanLine) throws ConfigParsingException {
            T result = EnumUtil.enumFromString(cleanLine, this.clazz.getEnumConstants(), null);
            if(result == null)
                throw new ConfigParsingException(cleanLine + " is not a valid enum option!");
            return result;
        }
        @NotNull
        @Override
        public String write(@NotNull T value) { return value.name(); }
    }

}
