package io.github.lightman314.lightmanscurrency.api.config.options.basic;

import io.github.lightman314.lightmanscurrency.api.config.options.ListOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class StringListOption extends ListOption<String> {

    protected StringListOption(@NotNull Supplier<List<String>> defaultValue) { super(defaultValue); }

    @Override
    protected ConfigParser<String> getPartialParser() { return StringOption.PARSER; }

    public static StringListOption create(@NotNull List<String> defaultValue) { return new StringListOption(() -> defaultValue); }
    public static StringListOption create(@NotNull Supplier<List<String>> list) { return new StringListOption(list); }

}
