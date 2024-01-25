package io.github.lightman314.lightmanscurrency.api.config.options.builtin;

import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ItemOption extends ConfigOption<Item> {

    public static final ConfigParser<Item> PARSER = new Parser(true);
    public static final ConfigParser<Item> PARSER_NO_AIR = new Parser(false);

    private final boolean allowAir;

    protected ItemOption(@NotNull Supplier<Item> defaultValue, boolean allowAir) { super(defaultValue); this.allowAir = allowAir; }
    @NotNull
    @Override
    protected ConfigParser<Item> getParser() { return this.allowAir ? PARSER : PARSER_NO_AIR; }

    public static ItemOption create(@NotNull ItemConvertible defaultValue) { return new ItemOption(defaultValue::asItem,true); }
    public static ItemOption create(@NotNull ItemConvertible defaultValue, boolean allowAir) { return new ItemOption(defaultValue::asItem,allowAir); }
    public static ItemOption create(@NotNull Supplier<Item> defaultValue) { return new ItemOption(defaultValue,true); }
    public static ItemOption create(@NotNull Supplier<Item> defaultValue, boolean allowAir) { return new ItemOption(defaultValue,allowAir); }

    private static class Parser implements ConfigParser<Item>
    {
        private final boolean allowAir;
        private Parser(boolean allowAir) { this.allowAir = allowAir; }
        @NotNull
        @Override
        public Item tryParse(@NotNull String cleanLine) throws ConfigParsingException {
            //Manually parse empty string as air
            if(cleanLine.isBlank() && this.allowAir)
                return Items.AIR;
            Identifier itemID = ResourceOption.PARSER.tryParse(cleanLine);
            if(!Registries.ITEM.containsId(itemID))
                throw new ConfigParsingException("No item found with id " + itemID + "!");
            Item item = Registries.ITEM.get(itemID);
            if(item == Items.AIR && !this.allowAir)
                throw new ConfigParsingException("Air is not an allowed item!");
            return item;
        }
        @NotNull
        @Override
        public String write(@NotNull Item value) { return ResourceOption.PARSER.write(Registries.ITEM.getId(value)); }
    }

}
