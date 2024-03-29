package io.github.lightman314.lightmanscurrency.common.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.common.commands.arguments.MoneyValueArgument;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.money.bank.BankSaveData;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.List;

public class CommandBank {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {

        LiteralArgumentBuilder<ServerCommandSource> bankCommand
                = CommandManager.literal("lcbank")
                .requires(stack -> stack.hasPermissionLevel(2))
                .then(CommandManager.literal("give")
                        .then(CommandManager.literal("allPlayers")
                                .then(CommandManager.argument("amount", MoneyValueArgument.argument(registryAccess))
                                        .executes(CommandBank::giveAllPlayers)))
                        .then(CommandManager.literal("allTeams")
                                .then(CommandManager.argument("amount", MoneyValueArgument.argument(registryAccess))
                                        .executes(CommandBank::giveAllTeams)))
                        .then(CommandManager.literal("players")
                                .then(CommandManager.argument("players", EntityArgumentType.players())
                                        .then(CommandManager.argument("amount", MoneyValueArgument.argument(registryAccess))
                                                .executes(CommandBank::givePlayers))))
                        .then(CommandManager.literal("team")
                                .then(CommandManager.argument("teamID", LongArgumentType.longArg(0))
                                        .then(CommandManager.argument("amount", MoneyValueArgument.argument(registryAccess))
                                                .executes(CommandBank::giveTeam)))))
                .then(CommandManager.literal("take")
                        .then(CommandManager.literal("allPlayers")
                                .then(CommandManager.argument("amount", MoneyValueArgument.argument(registryAccess))
                                        .executes(CommandBank::takeAllPlayers)))
                        .then(CommandManager.literal("allTeams")
                                .then(CommandManager.argument("amount", MoneyValueArgument.argument(registryAccess))
                                        .executes(CommandBank::takeAllTeams)))
                        .then(CommandManager.literal("players")
                                .then(CommandManager.argument("players", EntityArgumentType.players())
                                        .then(CommandManager.argument("amount", MoneyValueArgument.argument(registryAccess))
                                                .executes(CommandBank::takePlayers))))
                        .then(CommandManager.literal("team")
                                .then(CommandManager.argument("teamID", LongArgumentType.longArg(0))
                                        .then(CommandManager.argument("amount", MoneyValueArgument.argument(registryAccess))
                                                .executes(CommandBank::takeTeam)))));

        dispatcher.register(bankCommand);

    }

    static int giveAllPlayers(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException
    {
        CoinValue amount = MoneyValueArgument.getMoneyValue(commandContext,"amount");
        return giveTo(commandContext.getSource(), BankSaveData.GetPlayerBankAccounts(), amount);
    }

    static int giveAllTeams(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException
    {
        CoinValue amount = MoneyValueArgument.getMoneyValue(commandContext,"amount");
        return giveTo(commandContext.getSource(), TeamSaveData.GetAllTeams(false).stream().filter(Team::hasBankAccount).map(t -> BankAccount.GenerateReference(false,t)).toList(), amount);
    }

    static int givePlayers(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException
    {
        CoinValue amount = MoneyValueArgument.getMoneyValue(commandContext, "amount");
        return giveTo(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "players").stream().map(BankAccount::GenerateReference).toList(), amount);
    }

    static int giveTeam(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException
    {
        long teamID = LongArgumentType.getLong(commandContext, "teamID");
        CoinValue amount = MoneyValueArgument.getMoneyValue(commandContext, "amount");
        ServerCommandSource source = commandContext.getSource();
        Team team = TeamSaveData.GetTeam(false, teamID);
        if(team == null)
        {
            EasyText.sendCommandFail(source, EasyText.translatable("command.lightmanscurrency.lcbank.team.noteam", teamID));
            return 0;
        }
        else if(!team.hasBankAccount())
        {
            EasyText.sendCommandFail(source, EasyText.translatable("command.lightmanscurrency.lcbank.team.nobank", teamID));
            return 0;
        }
        return giveTo(source, Lists.newArrayList(BankAccount.GenerateReference(false, team)), amount);
    }

    static int giveTo(ServerCommandSource source, List<BankAccount.AccountReference> accounts, CoinValue amount)
    {
        int count = 0;
        Text bankName = null;
        for(BankAccount.AccountReference account : accounts)
        {
            if(BankAccount.ServerGiveCoins(account.get(), amount))
            {
                count++;
                if(count == 1)
                    bankName = account.get().getName();
            }

        }
        if(count < 1)
            EasyText.sendCommandFail(source, EasyText.translatable("command.lightmanscurrency.lcbank.give.fail"));
        else
        {
            if(count == 1)
                EasyText.sendCommandSuccess(source, EasyText.translatable("command.lightmanscurrency.lcbank.give.success.single", amount.getComponent("NULL"), bankName), true);
            else
                EasyText.sendCommandSuccess(source, EasyText.translatable("command.lightmanscurrency.lcbank.give.success", amount.getComponent("NULL"), count), true);
        }
        return count;
    }

    static int takeAllPlayers(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException
    {
        CoinValue amount = MoneyValueArgument.getMoneyValue(commandContext,"amount");
        return takeFrom(commandContext.getSource(), BankSaveData.GetPlayerBankAccounts(), amount);
    }

    static int takeAllTeams(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException
    {
        CoinValue amount = MoneyValueArgument.getMoneyValue(commandContext,"amount");
        return takeFrom(commandContext.getSource(), TeamSaveData.GetAllTeams(false).stream().filter(Team::hasBankAccount).map(t -> BankAccount.GenerateReference(false,t)).toList(), amount);
    }

    static int takePlayers(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException
    {
        CoinValue amount = MoneyValueArgument.getMoneyValue(commandContext, "amount");
        return takeFrom(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "players").stream().map(BankAccount::GenerateReference).toList(), amount);
    }

    static int takeTeam(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException
    {
        long teamID = LongArgumentType.getLong(commandContext, "teamID");
        CoinValue amount = MoneyValueArgument.getMoneyValue(commandContext, "amount");
        ServerCommandSource source = commandContext.getSource();
        Team team = TeamSaveData.GetTeam(false, teamID);
        if(team == null)
        {
            EasyText.sendCommandFail(source, EasyText.translatable("command.lightmanscurrency.lcbank.team.noteam", teamID));
            return 0;
        }
        else if(!team.hasBankAccount())
        {
            EasyText.sendCommandFail(source, EasyText.translatable("command.lightmanscurrency.lcbank.team.nobank", teamID));
            return 0;
        }
        return takeFrom(commandContext.getSource(), Lists.newArrayList(BankAccount.GenerateReference(false, team)), amount);
    }

    static int takeFrom(ServerCommandSource source, List<BankAccount.AccountReference> accounts, CoinValue amount)
    {
        int count = 0;
        Text bankName = null;
        CoinValue largestAmount = new CoinValue();
        for(BankAccount.AccountReference account : accounts)
        {
            Pair<Boolean, CoinValue> result = BankAccount.ServerTakeCoins(account.get(), amount);
            if(result.getFirst())
            {
                count++;
                if(count == 1)
                    bankName = account.get().getName();
                if(result.getSecond().getRawValue() > largestAmount.getRawValue())
                    largestAmount = result.getSecond();
            }
        }
        if(count < 1)
            EasyText.sendCommandFail(source, EasyText.translatable("command.lightmanscurrency.lcbank.take.fail"));
        else
        {
            if(count == 1)
                EasyText.sendCommandSuccess(source, EasyText.translatable("command.lightmanscurrency.lcbank.take.success.single", largestAmount.getComponent("NULL"), bankName), true);
            else
                EasyText.sendCommandSuccess(source, EasyText.translatable("command.lightmanscurrency.lcbank.take.success", largestAmount.getComponent("NULL"), count), true);
        }
        return count;
    }

}