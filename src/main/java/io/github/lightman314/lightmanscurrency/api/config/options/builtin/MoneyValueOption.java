package io.github.lightman314.lightmanscurrency.api.config.options.builtin;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.StringOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.util.CoinValueParser;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class MoneyValueOption extends ConfigOption<CoinValue> {

    public static ConfigParser<CoinValue> createParser(@NotNull Predicate<CoinValue> allowed) { return new Parser(allowed); }

    private final ConfigParser<CoinValue> parser;

    protected MoneyValueOption(@NotNull Supplier<CoinValue> defaultValue, @NotNull Predicate<CoinValue> allowed) { super(defaultValue); this.parser = createParser(allowed); }

    @NotNull
    @Override
    protected ConfigParser<CoinValue> getParser() { return this.parser; }

    public static MoneyValueOption create(@NotNull Supplier<CoinValue> defaultValue) { return create(defaultValue, v -> true); }
    public static MoneyValueOption createNonEmpty(@NotNull Supplier<CoinValue> defaultValue) { return create(defaultValue, v -> v.isValid() && !v.isFree()); }
    public static MoneyValueOption create(@NotNull Supplier<CoinValue> defaultValue, @NotNull Predicate<CoinValue> allowed) { return new MoneyValueOption(defaultValue, allowed); }

    private static class Parser implements ConfigParser<CoinValue>
    {
        private final Predicate<CoinValue> allowed;
        private Parser(@NotNull Predicate<CoinValue> allowed) { this.allowed = allowed; }
        @NotNull
        @Override
        public CoinValue tryParse(@NotNull String cleanLine) throws ConfigParsingException {
            try {
                CoinValue result = CoinValueParser.parse(new StringReader(StringOption.PARSER.tryParse(cleanLine)), true);
                if(!this.allowed.test(result))
                    throw new ConfigParsingException(cleanLine + " is not an allowed Money Value input!");
                return result;
            } catch (CommandSyntaxException e) { throw new ConfigParsingException(e); }
        }
        @NotNull
        @Override
        public String write(@NotNull CoinValue value) { return StringOption.PARSER.write(CoinValueParser.writeParsable(value)); }
    }

}
