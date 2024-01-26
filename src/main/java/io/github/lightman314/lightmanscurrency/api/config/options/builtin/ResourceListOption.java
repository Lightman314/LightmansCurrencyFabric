package io.github.lightman314.lightmanscurrency.api.config.options.builtin;

import io.github.lightman314.lightmanscurrency.api.config.options.ListOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class ResourceListOption extends ListOption<Identifier> {

    protected ResourceListOption(@NotNull Supplier<List<Identifier>> defaultValue) { super(defaultValue); }
    @Override
    protected ConfigParser<Identifier> getPartialParser() { return ResourceOption.PARSER; }

    public static ResourceListOption create(@NotNull List<Identifier> defaultValue) { return new ResourceListOption(() -> defaultValue); }
    public static ResourceListOption create(@NotNull Supplier<List<Identifier>> defaultValue) { return new ResourceListOption(defaultValue); }
}
