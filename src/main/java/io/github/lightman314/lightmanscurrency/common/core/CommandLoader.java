package io.github.lightman314.lightmanscurrency.common.core;

import com.mojang.brigadier.CommandDispatcher;
import io.github.lightman314.lightmanscurrency.common.commands.CommandBalTop;
import io.github.lightman314.lightmanscurrency.common.commands.CommandLCAdmin;
import io.github.lightman314.lightmanscurrency.common.commands.CommandReloadData;
import io.github.lightman314.lightmanscurrency.secrets.Secret;
import net.minecraft.server.command.ServerCommandSource;

public class CommandLoader {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        CommandLCAdmin.register(dispatcher);
        CommandReloadData.register(dispatcher);
        CommandBalTop.register(dispatcher);
        Secret.register(dispatcher);
    }

}
