package io.github.lightman314.lightmanscurrency.api.config.options.parsing;

import org.jetbrains.annotations.NotNull;

public interface ConfigParser<T> {

    @NotNull
    T tryParse(@NotNull String cleanLine) throws ConfigParsingException;
    @NotNull
    String write(@NotNull T value);

}
