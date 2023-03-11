package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.common.commands.arguments.*;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ArgumentSerializer;

public class ModCommandArguments {

    public static final ArgumentSerializer<TraderArgument> TRADER_ARGUMENT = new TraderArgument.Info();


    public static void registerCommandArguments() {
        ArgumentTypes.register("lightmanscurrency:trader_argument", TraderArgument.class, TRADER_ARGUMENT);
    }

}