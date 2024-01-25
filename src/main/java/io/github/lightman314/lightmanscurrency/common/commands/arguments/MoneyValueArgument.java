package io.github.lightman314.lightmanscurrency.common.commands.arguments;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.util.CoinValueParser;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class MoneyValueArgument implements ArgumentType<CoinValue> {

    private final RegistryWrapper<Item> items;

    private MoneyValueArgument(RegistryWrapper<Item> items) { this.items = items; }

    public static MoneyValueArgument argument(CommandRegistryAccess context) { return new MoneyValueArgument(context.createWrapper(Registries.ITEM.getKey())); }

    public static CoinValue getMoneyValue(CommandContext<ServerCommandSource> commandContext, String name) throws CommandSyntaxException {
        return commandContext.getArgument(name, CoinValue.class);
    }

    @Override
    public CoinValue parse(StringReader reader) throws CommandSyntaxException { return CoinValueParser.parse(reader, false); }

    @NotNull
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        return builder.buildFuture();
    }

    public Collection<String> getExamples() {
        return ImmutableList.of();
    }

}
