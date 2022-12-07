package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.commands.arguments.*;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.util.Identifier;

public class ModCommandArguments {

    public static final ArgumentSerializer<TraderArgument, ?> TRADER_ARGUMENT = new TraderArgument.Info();


    public static void registerCommandArguments() {
        ArgumentTypeRegistry.registerArgumentType(new Identifier(LightmansCurrency.MODID, "trader_argument"), TraderArgument.class, TRADER_ARGUMENT);
    }

}