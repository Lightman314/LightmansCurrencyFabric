package io.github.lightman314.lightmanscurrency.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.network.client.messages.config.SPacketReloadConfig;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class CommandConfig {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        LiteralArgumentBuilder<ServerCommandSource> configReloadCommand
                = CommandManager.literal("lcconfig")
                .then(CommandManager.literal("reload")
                        .executes(CommandConfig::reload));

        dispatcher.register(configReloadCommand);
    }

    static int reload(CommandContext<ServerCommandSource> commandContext) {
        int result = 0;
        boolean involveAdmins = false;
        if(commandContext.getSource().hasPermissionLevel(2))
        {
            involveAdmins = true;
            ConfigFile.reloadServerFiles();
            result++;
        }
        ServerPlayerEntity player = commandContext.getSource().getPlayer();
        if(player != null)
        {
            SPacketReloadConfig.INSTANCE.sendTo(player);
            result++;
        }
        if(result > 0)
            EasyText.sendCommandSuccess(commandContext.getSource(), EasyText.translatable("command.lightmanscurrency.lcconfig.reload"), involveAdmins);
        return result;
    }

}
