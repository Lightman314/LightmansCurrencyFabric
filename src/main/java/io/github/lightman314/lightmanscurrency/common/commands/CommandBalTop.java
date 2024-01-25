package io.github.lightman314.lightmanscurrency.common.commands;

import java.util.Comparator;
import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.money.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.money.bank.BankSaveData;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class CommandBalTop {

    public static final int ENTRIES_PER_PAGE = 10;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        LiteralArgumentBuilder<ServerCommandSource> lcAdminCommand
                = CommandManager.literal("lcbaltop")
                .executes(context -> CommandBalTop.execute(context, 1))
                .then(CommandManager.argument("page", IntegerArgumentType.integer(1))
                        .executes(CommandBalTop::executePage));

        dispatcher.register(lcAdminCommand);

    }

    static int executePage(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException {

        return execute(commandContext, IntegerArgumentType.getInteger(commandContext, "page"));

    }

    static int execute(CommandContext<ServerCommandSource> commandContext, int page) throws CommandSyntaxException {

        ServerCommandSource source = commandContext.getSource();

        //Get and sort all the bank accounts
        //Get player bank accounts
        List<BankAccount.AccountReference> allAccounts = BankSaveData.GetPlayerBankAccounts();
        //Get team bank accounts
        List<Team> allTeams = TeamSaveData.GetAllTeams(false);
        for(Team team : allTeams) {
            if(team.hasBankAccount())
                allAccounts.add(BankAccount.GenerateReference(false, team));
        }
        //Remove any null or empty entries from the list
        allAccounts.removeIf(bar -> {
            if(bar == null)
                return true;
            BankAccount ba = bar.get();
            if(ba == null)
                return true;
            return ba.getCoinStorage().getRawValue() <= 0;
        });
        if(allAccounts.size() == 0)
        {
            EasyText.sendCommandFail(source, EasyText.translatable("command.lightmanscurrency.lcbaltop.no_results"));
            return 0;
        }

        //Sort the bank account by balance (and name if balance is tied).
        allAccounts.sort(new AccountSorter());


        int startIndex = (page - 1) * ENTRIES_PER_PAGE;

        if(startIndex >= allAccounts.size())
        {
            EasyText.sendCommandFail(source, Text.translatable("command.lightmanscurrency.lcbaltop.error.page"));
            return 0;
        }


        source.sendFeedback(() -> Text.translatable("command.lightmanscurrency.lcbaltop.title").formatted(Formatting.BOLD).formatted(Formatting.GOLD), false);
        source.sendFeedback(() -> Text.translatable("command.lightmanscurrency.lcbaltop.page", page, getMaxPage(allAccounts.size())).formatted(Formatting.BOLD).formatted(Formatting.GOLD), false);
        for(int i = startIndex; i < startIndex + ENTRIES_PER_PAGE && i < allAccounts.size(); ++i)
        {
            try {
                BankAccount account = allAccounts.get(i).get();
                Text name = account.getName();
                String amount = account.getCoinStorage().getString("0");
                final int index = i;
                source.sendFeedback(() -> Text.translatable("command.lightmanscurrency.lcbaltop.entry", index + 1, name, amount), false);
            } catch(Exception ignored) { }
        }

        return 1;
    }

    private static int getMaxPage(int listSize) {
        return ((listSize - 1) / ENTRIES_PER_PAGE) + 1;
    }

    private static class AccountSorter implements Comparator<BankAccount.AccountReference> {

        @Override
        public int compare(BankAccount.AccountReference o1, BankAccount.AccountReference o2) {
            BankAccount a1 = o1 == null ? null : o1.get();
            BankAccount a2 = o2 == null ? null : o2.get();
            if(o1 == o2)
                return 0;
            if(o1 == null)
                return 1;
            if(o2 == null)
                return -1;
            long bal1 = a1.getCoinStorage().getRawValue();
            long bal2 = a2.getCoinStorage().getRawValue();

            if(bal1 > bal2)
                return -1;
            if(bal2 > bal1)
                return 1;

            //Sort by name
            return a1.getName().getString().toLowerCase().compareTo(a2.getName().getString().toLowerCase());
        }

    }

}