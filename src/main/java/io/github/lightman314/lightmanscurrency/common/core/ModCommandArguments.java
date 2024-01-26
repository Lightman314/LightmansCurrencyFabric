package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.common.commands.arguments.*;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;

public class ModCommandArguments {

    public static final ArgumentSerializer<TraderArgument> TRADER_ARGUMENT = new TraderArgument.Info();
    public static final ArgumentSerializer<MoneyValueArgument> MONEY_ARGUMENT = new ConstantArgumentSerializer<>(MoneyValueArgument::argument);


    public static void registerCommandArguments() {
        ArgumentTypes.register("lightmanscurrency:trader_argument", TraderArgument.class, TRADER_ARGUMENT);
        ArgumentTypes.register("lightmanscurrency:money_argument", MoneyValueArgument.class, MONEY_ARGUMENT);
    }

}