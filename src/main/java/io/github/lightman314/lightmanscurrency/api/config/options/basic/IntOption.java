package io.github.lightman314.lightmanscurrency.api.config.options.basic;

import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class IntOption extends ConfigOption<Integer> {

    public static ConfigParser<Integer> makeParser(int lowerLimit, int upperLimit) { return new Parser(lowerLimit, upperLimit); }

    private final int lowerLimit;
    private final int upperLimit;
    private final ConfigParser<Integer> parser;

    protected IntOption(@NotNull Supplier<Integer> defaultValue, int lowerLimit, int upperLimit) {
        super(defaultValue);
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.parser = makeParser(lowerLimit,upperLimit);
    }
    @NotNull
    @Override
    protected ConfigParser<Integer> getParser() { return this.parser; }
    @Nullable
    @Override
    protected String bonusComment() { return "Range: " + this.lowerLimit + " -> " + this.upperLimit; }

    public static IntOption create(int defaultValue) { return new IntOption(() -> defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE); }
    public static IntOption create(int defaultValue, int lowerLimit) { return new IntOption(() -> defaultValue, lowerLimit, Integer.MAX_VALUE); }
    public static IntOption create(int defaultValue, int lowerLimit, int upperLimit) { return new IntOption(() -> defaultValue, lowerLimit, upperLimit); }
    public static IntOption create(@NotNull Supplier<Integer> defaultValue) { return new IntOption(defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE); }
    public static IntOption create(@NotNull Supplier<Integer> defaultValue, int lowerLimit) { return new IntOption(defaultValue, lowerLimit, Integer.MAX_VALUE); }
    public static IntOption create(@NotNull Supplier<Integer> defaultValue, int lowerLimit, int upperLimit) { return new IntOption(defaultValue, lowerLimit, upperLimit); }



    private static class Parser implements ConfigParser<Integer>
    {
        private final int lowerLimit;
        private final int upperLimit;

        private Parser(int lowerLimit, int upperLimit) {
            this.lowerLimit = lowerLimit;
            this.upperLimit = upperLimit;
        }

        @NotNull
        @Override
        public Integer tryParse(@NotNull String cleanLine) throws ConfigParsingException {
            try { return MathUtil.clamp(Integer.parseInt(cleanLine), this.lowerLimit, this.upperLimit);
            } catch (NumberFormatException e) { throw new ConfigParsingException("Error parsing integer!", e); }
        }
        @NotNull
        @Override
        public String write(@NotNull Integer value) { return value.toString(); }
    }


}
