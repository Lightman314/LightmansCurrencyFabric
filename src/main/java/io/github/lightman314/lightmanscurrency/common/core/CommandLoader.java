package io.github.lightman314.lightmanscurrency.common.core;

import com.mojang.brigadier.CommandDispatcher;
import io.github.lightman314.lightmanscurrency.common.commands.*;
import net.minecraft.server.command.ServerCommandSource;

public class CommandLoader {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        CommandLCAdmin.register(dispatcher);
        CommandReloadData.register(dispatcher);
        CommandBalTop.register(dispatcher);
        CommandConfig.register(dispatcher);
        CommandBank.register(dispatcher);
    }

}
