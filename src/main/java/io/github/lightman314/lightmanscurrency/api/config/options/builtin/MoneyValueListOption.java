package io.github.lightman314.lightmanscurrency.api.config.options.builtin;

import io.github.lightman314.lightmanscurrency.api.config.options.ListOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class MoneyValueListOption extends ListOption<CoinValue> {

    private final ConfigParser<CoinValue> parser;

    protected MoneyValueListOption(@NotNull Supplier<List<CoinValue>> defaultValue, @NotNull Predicate<CoinValue> allowed) { super(defaultValue); this.parser = MoneyValueOption.createParser(allowed); }

    @Override
    protected ConfigParser<CoinValue> getPartialParser() { return this.parser; }

    public static MoneyValueListOption create(@NotNull Supplier<List<CoinValue>> defaultValue) { return new MoneyValueListOption(defaultValue, v -> true); }
    public static MoneyValueListOption createNonEmpty(@NotNull Supplier<List<CoinValue>> defaultValue) { return new MoneyValueListOption(defaultValue, v -> v.isValid() && !v.isFree()); }
    public static MoneyValueListOption create(@NotNull Supplier<List<CoinValue>> defaultValue, @NotNull Predicate<CoinValue> allowed) { return new MoneyValueListOption(defaultValue, allowed); }
}
