package io.github.lightman314.lightmanscurrency.api.config.options.builtin;

import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.StringOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ResourceOption extends ConfigOption<Identifier> {

    public static final ConfigParser<Identifier> PARSER = new Parser();

    protected ResourceOption(@NotNull Supplier<Identifier> defaultValue) { super(defaultValue); }

    public static ResourceOption create(@NotNull Identifier defaultValue) { return new ResourceOption(() -> defaultValue); }
    public static ResourceOption create(@NotNull Supplier<Identifier> defaultValue) { return new ResourceOption(defaultValue); }

    @NotNull
    @Override
    protected ConfigParser<Identifier> getParser() { return PARSER; }

    private static class Parser implements ConfigParser<Identifier>
    {

        @NotNull
        @Override
        public Identifier tryParse(@NotNull String cleanLine) throws ConfigParsingException {
            String s = StringOption.PARSER.tryParse(cleanLine);
            try { return new Identifier(s);
            } catch (InvalidIdentifierException e) { throw new ConfigParsingException(s + " is not a valid Resource Location!", e); }
        }

        @NotNull
        @Override
        public String write(@NotNull Identifier value) { return value.toString(); }
    }
}
