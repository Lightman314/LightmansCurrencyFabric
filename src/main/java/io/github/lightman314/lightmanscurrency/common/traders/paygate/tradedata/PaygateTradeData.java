package io.github.lightman314.lightmanscurrency.common.traders.paygate.tradedata;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil.TextFormatting;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.common.menu.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menu.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.menu.traderstorage.trades_basic.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.AlertData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class PaygateTradeData extends TradeData {

    public PaygateTradeData() { super(true); }

    int duration = PaygateTraderData.DURATION_MIN;
    public int getDuration() { return Math.max(this.duration, PaygateTraderData.DURATION_MIN); }
    public void setDuration(int duration) { this.duration = Math.max(duration, PaygateTraderData.DURATION_MIN); }
    UUID ticketID = null;
    public boolean isTicketTrade() { return this.ticketID != null; }
    public UUID getTicketID() { return this.ticketID; }
    public void setTicketID(UUID ticketID) { this.ticketID = ticketID; }

    @Override
    public TradeDirection getTradeDirection() { return TradeDirection.SALE; }

    public boolean canAfford(TradeContext context) {
        if(this.isTicketTrade())
            return context.hasTicket(this.ticketID);
        else
            return context.hasFunds(this.cost);
    }

    @Override
    public boolean isValid() {
        return this.getDuration() >= PaygateTraderData.DURATION_MIN && (this.isTicketTrade() || super.isValid());
    }

    public static NbtCompound saveAllData(NbtCompound nbt, List<PaygateTradeData> data)
    {
        return saveAllData(nbt, data, DEFAULT_KEY);
    }

    public static NbtCompound saveAllData(NbtCompound nbt, List<PaygateTradeData> data, String key)
    {
        NbtList listNBT = new NbtList();

        for(int i = 0; i < data.size(); i++)
        {
            listNBT.add(data.get(i).getAsNBT());
        }

        if(listNBT.size() > 0)
            nbt.put(key, listNBT);

        return nbt;
    }

    public static PaygateTradeData loadData(NbtCompound nbt) {
        PaygateTradeData trade = new PaygateTradeData();
        trade.loadFromNBT(nbt);
        return trade;
    }

    public static List<PaygateTradeData> loadAllData(NbtCompound nbt)
    {
        return loadAllData(DEFAULT_KEY, nbt);
    }

    public static List<PaygateTradeData> loadAllData(String key, NbtCompound nbt)
    {
        NbtList listNBT = nbt.getList(key, NbtElement.COMPOUND_TYPE);

        List<PaygateTradeData> data = listOfSize(listNBT.size());

        for(int i = 0; i < listNBT.size(); i++)
        {
            data.get(i).loadFromNBT(listNBT.getCompound(i));
        }

        return data;
    }

    public static List<PaygateTradeData> listOfSize(int tradeCount)
    {
        List<PaygateTradeData> data = Lists.newArrayList();
        while(data.size() < tradeCount)
            data.add(new PaygateTradeData());
        return data;
    }

    @Override
    public NbtCompound getAsNBT() {
        NbtCompound compound = super.getAsNBT();

        compound.putInt("Duration", this.getDuration());
        if(this.ticketID != null)
            compound.putUuid("Ticket", this.ticketID);

        return compound;
    }

    @Override
    protected void loadFromNBT(NbtCompound compound) {
        super.loadFromNBT(compound);

        this.duration = compound.getInt("Duration");

        if(compound.contains("Ticket"))
            this.ticketID = compound.getUuid("Ticket");
        else
            this.ticketID = null;

    }

    @Override
    public TradeComparisonResult compare(TradeData otherTrade) {
        LightmansCurrency.LogWarning("Attempting to compare paygate trades, but paygate trades do not support this interaction.");
        return new TradeComparisonResult();
    }

    @Override
    public boolean AcceptableDifferences(TradeComparisonResult result) {
        LightmansCurrency.LogWarning("Attempting to determine if the paygate trades differences are acceptable, but paygate trades do not support this interaction.");
        return false;
    }

    @Override
    public List<Text> GetDifferenceWarnings(TradeComparisonResult differences) {
        LightmansCurrency.LogWarning("Attempting to get warnings for different paygate trades, but paygate trades do not support this interaction.");
        return new ArrayList<>();
    }

    @Override
    public int tradeButtonWidth(TradeContext context) { return 94; }

    @Override
    public DisplayData inputDisplayArea(TradeContext context) { return new DisplayData(1, 1, 34, 16); }

    @Override
    public DisplayData outputDisplayArea(TradeContext context) { return new DisplayData(58, 1, 34, 16); }

    @Override
    public Pair<Integer,Integer> arrowPosition(TradeContext context) { return Pair.of(36, 1); }

    @Override
    public Pair<Integer,Integer> alertPosition(TradeContext context) { return Pair.of(36, 1); }

    @Override
    public List<DisplayEntry> getInputDisplays(TradeContext context) {
        if(this.isTicketTrade())
            return Lists.newArrayList(DisplayEntry.of(TicketItem.CreateTicket(this.ticketID), 1, Lists.newArrayList(Text.translatable("tooltip.lightmanscurrency.ticket.id", this.ticketID))));
        else
            return Lists.newArrayList(DisplayEntry.of(this.getCost(context), context.isStorageMode ? Lists.newArrayList(Text.translatable("tooltip.lightmanscurrency.trader.price_edit")) : null));
    }

    @Override
    public List<DisplayEntry> getOutputDisplays(TradeContext context) {
        return Lists.newArrayList(DisplayEntry.of(formatDurationDisplay(this.duration), TextFormatting.create(), Lists.newArrayList(formatDuration(this.getDuration()))));
    }

    public static MutableText formatDurationShort(int duration) {

        int ticks = duration % 20;
        int seconds = (duration / 20) % 60;
        int minutes = (duration / 1200 ) % 60;
        int hours = (duration / 72000);
        MutableText result = Text.empty();
        if(hours > 0)
            result.append(Text.translatable("tooltip.lightmanscurrency.paygate.duration.hours.short", hours));
        if(minutes > 0)
            result.append(Text.translatable("tooltip.lightmanscurrency.paygate.duration.minutes.short", minutes));
        if(seconds > 0)
            result.append(Text.translatable("tooltip.lightmanscurrency.paygate.duration.seconds.short", seconds));
        if(ticks > 0 || result.getString().isBlank())
            result.append(Text.translatable("tooltip.lightmanscurrency.paygate.duration.ticks.short", ticks));
        return result;
    }

    public static MutableText formatDurationDisplay(int duration) {

        int ticks = duration % 20;
        int seconds = (duration / 20) % 60;
        int minutes = (duration / 1200 ) % 60;
        int hours = (duration / 72000);
        if(hours > 0)
            return Text.translatable("tooltip.lightmanscurrency.paygate.duration.hours.short", hours);
        if(minutes > 0)
            return Text.translatable("tooltip.lightmanscurrency.paygate.duration.minutes.short", minutes);
        if(seconds > 0)
            return Text.translatable("tooltip.lightmanscurrency.paygate.duration.seconds.short", seconds);
        return Text.translatable("tooltip.lightmanscurrency.paygate.duration.ticks.short", ticks);
    }

    public static MutableText formatDuration(int duration) {

        int ticks = duration % 20;
        int seconds = (duration / 20) % 60;
        int minutes = (duration / 1200 ) % 60;
        int hours = (duration / 72000);
        MutableText result = Text.empty();
        boolean addSpacer = false;
        if(hours > 0)
        {
            if(addSpacer)
                result.append(Text.literal(" "));
            addSpacer = true;
            if(hours > 1)
                result.append(Text.translatable("tooltip.lightmanscurrency.paygate.duration.hours", hours));
            else
                result.append(Text.translatable("tooltip.lightmanscurrency.paygate.duration.hours.singular", hours));
        }
        if(minutes > 0)
        {
            if(addSpacer)
                result.append(Text.literal(" "));
            addSpacer = true;
            if(minutes > 1)
                result.append(Text.translatable("tooltip.lightmanscurrency.paygate.duration.minutes", minutes));
            else
                result.append(Text.translatable("tooltip.lightmanscurrency.paygate.duration.minutes.singular", minutes));
        }
        if(seconds > 0)
        {
            if(addSpacer)
                result.append(Text.literal(" "));
            addSpacer = true;
            if(seconds > 1)
                result.append(Text.translatable("tooltip.lightmanscurrency.paygate.duration.seconds", seconds));
            else
                result.append(Text.translatable("tooltip.lightmanscurrency.paygate.duration.seconds.singular", seconds));
        }
        if(ticks > 0)
        {
            if(addSpacer)
                result.append(Text.literal(" "));
            addSpacer = true;
            if(ticks > 1)
                result.append(Text.translatable("tooltip.lightmanscurrency.paygate.duration.ticks", ticks));
            else
                result.append(Text.translatable("tooltip.lightmanscurrency.paygate.duration.ticks.singular", ticks));
        }
        return result;
    }

    @Override
    protected void getAdditionalAlertData(TradeContext context, List<AlertData> alerts) {
        if(context.hasTrader() && context.getTrader() instanceof PaygateTraderData)
        {
            PaygateTraderData paygate = (PaygateTraderData)context.getTrader();
            //Check whether the paygate is currently active
            if(paygate.isActive())
                alerts.add(AlertData.warn(Text.translatable("tooltip.lightmanscurrency.paygate.active")));
            //Check whether they can afford the costs
            if(!this.canAfford(context))
                alerts.add(AlertData.warn(Text.translatable("tooltip.lightmanscurrency.cannotafford")));
        }
    }

    @Override
    public void onInputDisplayInteraction(BasicTradeEditTab tab, TraderStorageMenu.IClientMessage clientHandler, int index, int button, ItemStack heldItem) {
        if(tab.menu.getTrader() instanceof PaygateTraderData)
        {
            PaygateTraderData paygate = (PaygateTraderData)tab.menu.getTrader();
            int tradeIndex = paygate.getAllTrades().indexOf(this);
            if(tradeIndex < 0)
                return;
            if(heldItem.getItem() == ModItems.TICKET_MASTER)
            {
                this.setTicketID(TicketItem.GetTicketID(heldItem));
                //Only send message on client, otherwise we get an infinite loop
                if(tab.menu.isClient())
                    tab.sendInputInteractionMessage(tradeIndex, 0, button, heldItem);
            }
            else
            {
                NbtCompound extraData = new NbtCompound();
                extraData.putInt("TradeIndex", tradeIndex);
                tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
            }
        }
    }

    @Override
    public void onOutputDisplayInteraction(BasicTradeEditTab tab, TraderStorageMenu.IClientMessage clientHandler, int index, int button, ItemStack heldItem) {
        if(tab.menu.getTrader() instanceof PaygateTraderData)
        {
            PaygateTraderData paygate = (PaygateTraderData)tab.menu.getTrader();
            int tradeIndex = paygate.getAllTrades().indexOf(this);
            if(tradeIndex < 0)
                return;
            NbtCompound extraData = new NbtCompound();
            extraData.putInt("TradeIndex", tradeIndex);
            tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
        }
    }

    @Override
    public void onInteraction(BasicTradeEditTab tab, TraderStorageMenu.IClientMessage clientHandler, int mouseX, int mouseY, int button, ItemStack heldItem) {

        if(tab.menu.getTrader() instanceof PaygateTraderData)
        {
            PaygateTraderData paygate = (PaygateTraderData)tab.menu.getTrader();
            int tradeIndex = paygate.getAllTrades().indexOf(this);
            if(tradeIndex < 0)
                return;
            NbtCompound extraData = new NbtCompound();
            extraData.putInt("TradeIndex", tradeIndex);
            tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
        }

    }

}