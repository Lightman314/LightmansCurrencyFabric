package io.github.lightman314.lightmanscurrency.common.traders.slot_machine.trade_data;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menu.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menu.traderstorage.trades_basic.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.AlertData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SlotMachineTrade extends TradeData {

    public final SlotMachineTraderData trader;
    public SlotMachineTrade(SlotMachineTraderData trader) { super(false); this.trader = trader; }

    @Override
    public CoinValue getCost() { return this.trader.getPrice(); }

    @Override
    public boolean isValid() { return this.trader.hasValidTrade(); }

    @Override
    public TradeDirection getTradeDirection() { return TradeDirection.SALE; }

    @Override
    public TradeComparisonResult compare(TradeData otherTrade) { return new TradeComparisonResult(); }

    @Override
    public boolean AcceptableDifferences(TradeComparisonResult result) { return false; }

    @Override
    public List<Text> GetDifferenceWarnings(TradeComparisonResult differences) { return new ArrayList<>(); }

    @Override
    @Environment(EnvType.CLIENT)
    public int tradeButtonWidth(TradeContext context) { return 128; }

    @Override
    @Environment(EnvType.CLIENT)
    public Pair<Integer, Integer> arrowPosition(TradeContext context) { return Pair.of(36,1); }

    @Override
    @Environment(EnvType.CLIENT)
    public DisplayData inputDisplayArea(TradeContext context) { return new DisplayData(1, 1, 34, 16); }

    @Override
    @Environment(EnvType.CLIENT)
    public List<DisplayEntry> getInputDisplays(TradeContext context) { return Lists.newArrayList(DisplayEntry.of(this.getCost(context))); }

    @Override
    @Environment(EnvType.CLIENT)
    public DisplayData outputDisplayArea(TradeContext context) { return new DisplayData(59, 1, 68, 16); }

    @Environment(EnvType.CLIENT)
    private SlotMachineEntry getTimedEntry()
    {
        List<SlotMachineEntry> entries = this.trader.getValidEntries();
        if(entries.size() == 0)
            return null;
        return entries.get((int)System.currentTimeMillis()/1000 % entries.size());
    }

    @Override
    @Environment(EnvType.CLIENT)
    public List<DisplayEntry> getOutputDisplays(TradeContext context) {
        SlotMachineEntry entry = this.getTimedEntry();
        if(entry == null)
            return new ArrayList<>();
        List<DisplayEntry> entries = new ArrayList<>();
        String odds = this.trader.getOdds(entry.getWeight());
        for(ItemStack item : entry.items)
            entries.add(DisplayEntry.of(item, item.getCount(), this.getTooltip(item, entry.getWeight(), odds)));
        return entries;
    }

    @Environment(EnvType.CLIENT)
    private List<Text> getTooltip(ItemStack stack, int weight, String odds)
    {
        if(stack.isEmpty())
            return null;

        List<Text> tooltips = Screen.getTooltipFromItem(MinecraftClient.getInstance(), stack);
        tooltips.add(0, EasyText.translatable("tooltip.lightmanscurrency.slot_machine.weight", weight));
        tooltips.add(0, EasyText.translatable("tooltip.lightmanscurrency.slot_machine.odds", odds));

        return tooltips;
    }

    @Override
    protected void getAdditionalAlertData(TradeContext context, List<AlertData> alerts) {
        if(context.hasTrader() && context.getTrader() instanceof SlotMachineTraderData t)
        {
            if(!t.isCreative())
            {
                //Check Stock
                if(!t.hasStock())
                    alerts.add(AlertData.warn(EasyText.translatable("tooltip.lightmanscurrency.outofstock")));
            }
            //Check whether they can afford the price
            if(!context.hasFunds(this.getCost(context)))
                alerts.add(AlertData.warn(EasyText.translatable("tooltip.lightmanscurrency.cannotafford")));
        }
    }

    @Override
    public void onInputDisplayInteraction(BasicTradeEditTab tab, @Nullable TraderStorageMenu.IClientMessage clientHandler, int index, int button, ItemStack heldItem) { }

    @Override
    public void onOutputDisplayInteraction(BasicTradeEditTab tab, @Nullable TraderStorageMenu.IClientMessage clientHandler, int index, int button, ItemStack heldItem) { }

    @Override
    public void onInteraction(BasicTradeEditTab tab, @Nullable TraderStorageMenu.IClientMessage clientHandler, int mouseX, int mouseY, int button, ItemStack heldItem) { }

    @Override
    public boolean allowTradeRule(TradeRule rule) { return false; }

}
