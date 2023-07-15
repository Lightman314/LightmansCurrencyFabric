package io.github.lightman314.lightmanscurrency.common.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import io.github.lightman314.lightmanscurrency.common.blockentity.trader.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces.ITraderBlock;
import io.github.lightman314.lightmanscurrency.common.commands.arguments.TraderArgument;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.PlayerWhitelist;
import io.github.lightman314.lightmanscurrency.common.traders.terminal.filters.TraderSearchFilter;
import io.github.lightman314.lightmanscurrency.network.client.messages.admin.SMessageSyncAdminList;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CommandLCAdmin {


    private static List<UUID> adminPlayers = new ArrayList<>();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        LiteralArgumentBuilder<ServerCommandSource> lcAdminCommand
                = CommandManager.literal("lcadmin")
                .requires((commandSource) -> commandSource.hasPermissionLevel(2))
                .then(CommandManager.literal("toggleadmin")
                        .requires((commandSource) -> commandSource.getEntity() instanceof ServerPlayerEntity)
                        .executes(CommandLCAdmin::toggleAdmin))
                .then(CommandManager.literal("traderdata")
                        .then(CommandManager.literal("list")
                                .executes(CommandLCAdmin::listTraderData))
                        .then(CommandManager.literal("search")
                                .then(CommandManager.argument("searchText", TextArgumentType.text())
                                        .executes(CommandLCAdmin::searchTraderData)))
                        .then(CommandManager.literal("delete")
                                .then(CommandManager.argument("traderID", TraderArgument.trader())
                                        .executes(CommandLCAdmin::deleteTraderData)))
                        .then(CommandManager.literal("debug")
                                .then(CommandManager.argument("traderID", TraderArgument.trader())
                                        .executes(CommandLCAdmin::debugTraderData)))
                        .then(CommandManager.literal("addToWhitelist")
                                .then(CommandManager.argument("traderID", TraderArgument.traderWithPersistent())
                                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                                .executes(CommandLCAdmin::addToTraderWhitelist)))))
                .then(CommandManager.literal("prepareForStructure")
                        .then(CommandManager.argument("traderPos", BlockPosArgumentType.blockPos())
                                .executes(CommandLCAdmin::setCustomTrader)));

        dispatcher.register(lcAdminCommand);

    }

    static int toggleAdmin(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException{

        ServerCommandSource source = commandContext.getSource();
        ServerPlayerEntity sourcePlayer = source.getPlayerOrThrow();

        ToggleAdminPlayer(sourcePlayer);
        Text enabledDisabled = isAdminPlayer(sourcePlayer) ? Text.translatable("command.lightmanscurrency.lcadmin.toggleadmin.enabled").formatted(Formatting.GREEN) : Text.translatable("command.lightmanscurrency.lcadmin.toggleadmin.disabled").formatted(Formatting.RED);
        source.sendFeedback(() -> Text.translatable("command.lightmanscurrency.lcadmin.toggleadmin", enabledDisabled), true);

        return 1;
    }

    private static final SimpleCommandExceptionType ERROR_BLOCK_NOT_FOUND = new SimpleCommandExceptionType(Text.translatable("command.trader.block.notfound"));

    static int setCustomTrader(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException {

        ServerCommandSource source = commandContext.getSource();

        BlockPos pos = BlockPosArgumentType.getLoadedBlockPos(commandContext, "traderPos");

        World level = source.getWorld();

        BlockState state = level.getBlockState(pos);
        BlockEntity be;
        if(state.getBlock() instanceof ITraderBlock)
            be = ((ITraderBlock)state.getBlock()).getBlockEntity(state, level, pos);
        else
            be = level.getBlockEntity(pos);

        if(be instanceof TraderBlockEntity<?> trader)
        {
            trader.saveCurrentTraderAsCustomTrader();
            source.sendFeedback(() -> Text.translatable("command.lightmanscurrency.lcadmin.setCustomTrader.success"), true);
            return 1;
        }

        throw ERROR_BLOCK_NOT_FOUND.create();

    }

    static int listTraderData(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException{

        ServerCommandSource source = commandContext.getSource();
        List<TraderData> allTraders = TraderSaveData.GetAllTraders(false);

        if(allTraders.size() > 0)
        {

            source.sendFeedback(() -> Text.translatable("command.lightmanscurrency.lcadmin.universaldata.list.title"), true);

            for(int i = 0; i < allTraders.size(); i++)
            {
                TraderData thisTrader = allTraders.get(i);
                //Spacer
                if(i > 0) //No spacer on the first output
                    source.sendFeedback(Text::empty, true);

                sendTraderDataFeedback(thisTrader, source);

            }
        }
        else
        {
            source.sendFeedback(() -> Text.translatable("command.lightmanscurrency.lcadmin.universaldata.list.none"), true);
        }

        return 1;
    }

    static int searchTraderData(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException{

        ServerCommandSource source = commandContext.getSource();

        String searchText = MessageArgumentType.getMessage(commandContext, "searchText").getString();

        List<TraderData> results = TraderSaveData.GetAllTraders(false).stream().filter(trader -> TraderSearchFilter.CheckFilters(trader, searchText)).toList();
        if(results.size() > 0)
        {

            source.sendFeedback(() -> Text.translatable("command.lightmanscurrency.lcadmin.universaldata.list.title"), true);
            for(int i = 0; i < results.size(); i++)
            {
                TraderData thisTrader = results.get(i);
                //Spacer
                if(i > 0) //No spacer on the first output
                    source.sendFeedback(Text::empty, true);

                sendTraderDataFeedback(thisTrader, source);
            }
        }
        else
        {
            source.sendFeedback(() -> Text.translatable("command.lightmanscurrency.lcadmin.universaldata.list.search.none"), true);
        }

        return 1;
    }

    private static void sendTraderDataFeedback(TraderData thisTrader, ServerCommandSource source)
    {
        //Trader ID
        String traderID = String.valueOf(thisTrader.getID());
        source.sendFeedback(() -> Text.translatable("command.lightmanscurrency.lcadmin.universaldata.list.traderid", Text.literal(traderID).setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, traderID)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("command.lightmanscurrency.lcadmin.universaldata.list.traderid.copytooltip"))))), false);
        //Persistent ID
        if(thisTrader.isPersistent())
            source.sendFeedback(() -> Text.translatable("command.lightmanscurrency.lcadmin.universaldata.list.persistentid", thisTrader.getPersistentID()), false);

        //Type
        source.sendFeedback(() -> Text.translatable("command.lightmanscurrency.lcadmin.universaldata.list.type", thisTrader.type), false);

        //Ignore everything else for auction houses
        if(thisTrader instanceof AuctionHouseTrader)
            return;

        //Team / Team ID
        if(thisTrader.getOwner().hasTeam())
        {
            source.sendFeedback(() -> Text.translatable("command.lightmanscurrency.lcadmin.universaldata.list.owner.team", thisTrader.getOwner().getTeam().getName(), thisTrader.getOwner().getTeam().getID()), false);
        }
        //Owner / Owner ID
        else if(thisTrader.getOwner().hasPlayer())
        {
            source.sendFeedback(() -> Text.translatable("command.lightmanscurrency.lcadmin.universaldata.list.owner", thisTrader.getOwner().getPlayer().getName(false), thisTrader.getOwner().getPlayer().id.toString()), false);
        }
        else
            source.sendFeedback(() -> Text.translatable("command.lightmanscurrency.lcadmin.universaldata.list.owner.custom", thisTrader.getOwner().getOwnerName(false)), false);

        if(!thisTrader.isPersistent())
        {
            //Dimension
            String dimension = thisTrader.getLevel().getValue().toString();
            source.sendFeedback(() -> Text.translatable("command.lightmanscurrency.lcadmin.universaldata.list.dimension", dimension), false);
            //Position
            BlockPos pos = thisTrader.getPos();
            String position = pos.getX() + " " + pos.getY() + " " + pos.getZ();
            String teleportPosition = pos.getX() + " " + (pos.getY() + 1) + " " + pos.getZ();
            source.sendFeedback(() -> Text.translatable("command.lightmanscurrency.lcadmin.universaldata.list.position", Text.literal(position).setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/execute in " + dimension + " run tp @s " + teleportPosition)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("command.lightmanscurrency.lcadmin.universaldata.list.position.teleporttooltip"))))), true);
        }
        //Custom Name (if applicable)
        if(thisTrader.hasCustomName())
            source.sendFeedback(() -> Text.translatable("command.lightmanscurrency.lcadmin.universaldata.list.name", thisTrader.getName()), true);
    }

    static int deleteTraderData(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException
    {

        ServerCommandSource source = commandContext.getSource();

        TraderData trader = TraderArgument.getTrader(commandContext, "traderID");

        //Remove the trader
        TraderSaveData.DeleteTrader(trader.getID());
        //Send success message
        source.sendFeedback(() -> Text.translatable("command.lightmanscurrency.lcadmin.universaldata.delete.success", trader.getName()), true);
        return 1;

    }

    static int debugTraderData(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException
    {
        ServerCommandSource source = commandContext.getSource();

        TraderData trader = TraderArgument.getTrader(commandContext, "traderID");
        source.sendFeedback(() -> Text.literal(trader.save().asString()), false);

        return 1;
    }

    static int addToTraderWhitelist(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException
    {

        ServerCommandSource source = commandContext.getSource();

        TraderData trader = TraderArgument.getTrader(commandContext, "traderID");

        TradeRule rule = TradeRule.getRule(PlayerWhitelist.TYPE, trader.getRules());
        if(rule instanceof PlayerWhitelist whitelist)
        {
            Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(commandContext, "player");
            int count = 0;
            for(ServerPlayerEntity player : players)
            {
                if(whitelist.addToWhitelist(player))
                    count++;
            }
            final int c = count;
            source.sendFeedback(() -> Text.translatable("command.lightmanscurrency.lcadmin.traderdata.add_whitelist.success", c, trader.getName()), true);

            if(count > 0)
                trader.markRulesDirty();

            return count;
        }
        else
        {
            source.sendError(Text.translatable("command.lightmanscurrency.lcadmin.traderdata.add_whitelist.missingrule"));
            return 0;
        }

    }

    public static boolean isAdminPlayer(PlayerEntity player) { return adminPlayers.contains(player.getUuid()) && player.hasPermissionLevel(2); }

    private static void ToggleAdminPlayer(ServerPlayerEntity player) {
        UUID playerID = player.getUuid();
        if(adminPlayers.contains(playerID))
            adminPlayers.remove(playerID);
        else
            adminPlayers.add(playerID);
        new SMessageSyncAdminList(adminPlayers).sendToAll();
    }

    public static void SendAdminList(PacketSender channel) { new SMessageSyncAdminList(adminPlayers).sendTo(channel); }

    public static void loadAdminPlayers(List<UUID> serverAdminList) { adminPlayers = serverAdminList; }

}