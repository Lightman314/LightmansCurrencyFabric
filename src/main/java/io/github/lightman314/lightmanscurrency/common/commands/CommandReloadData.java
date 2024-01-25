package io.github.lightman314.lightmanscurrency.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.common.atm.ATMData;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class CommandReloadData {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        LiteralArgumentBuilder<ServerCommandSource> lcReloadCommand
                = CommandManager.literal("lcreload")
                .requires((commandSource) -> commandSource.hasPermissionLevel(2))
                .executes(CommandReloadData::execute);

        dispatcher.register(lcReloadCommand);

    }

    static int execute(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException{

        TraderSaveData.ReloadPersistentTraders();
        MoneyUtil.reloadMoneyData();
        ATMData.reloadATMData();
        commandContext.getSource().sendFeedback(() -> Text.translatable("command.lightmanscurrency.lcreload"), true);
        return 1;

    }

}