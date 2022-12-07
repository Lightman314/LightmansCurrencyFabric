package io.github.lightman314.lightmanscurrency.common.menu.traderstorage.paygate;

import java.util.UUID;
import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.paygate.PaygateTradeEditClientTab;
import io.github.lightman314.lightmanscurrency.common.menu.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menu.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.tradedata.PaygateTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;

public class PaygateTradeEditTab extends TraderStorageTab {

    public PaygateTradeEditTab(TraderStorageMenu menu) { super(menu); }

    @Override
    @Environment(EnvType.CLIENT)
    public TraderStorageClientTab<?> createClientTab(TraderStorageScreen screen) { return new PaygateTradeEditClientTab(screen, this); }

    @Override
    public boolean canOpen(PlayerEntity player) { return this.menu.getTrader().hasPermission(player, Permissions.EDIT_TRADES); }

    private int tradeIndex = -1;
    public int getTradeIndex() { return this.tradeIndex; }
    public PaygateTradeData getTrade() {
        if(this.menu.getTrader() instanceof PaygateTraderData paygate)
        {
            if(this.tradeIndex >= paygate.getTradeCount() || this.tradeIndex < 0)
            {
                this.menu.changeTab(TraderStorageTab.TAB_TRADE_BASIC);
                this.menu.sendMessage(this.menu.createTabChangeMessage(TraderStorageTab.TAB_TRADE_BASIC, null));
                return null;
            }
            return paygate.getTrade(this.tradeIndex);
        }
        return null;
    }

    @Override
    public void onTabOpen() { }

    @Override
    public void onTabClose() { }

    @Override
    public void addStorageMenuSlots(Function<Slot, Slot> addSlot) { }

    public void setTradeIndex(int tradeIndex) { this.tradeIndex = tradeIndex; }

    public void setPrice(CoinValue price) {
        PaygateTradeData trade = this.getTrade();
        if(trade != null)
        {
            trade.setCost(price);
            this.menu.getTrader().markTradesDirty();
            if(this.menu.isClient())
            {
                NbtCompound message = new NbtCompound();
                price.save(message, "NewPrice");
                this.menu.sendMessage(message);
            }
        }
    }

    public void setTicket(UUID ticketID) {
        PaygateTradeData trade = this.getTrade();
        if(trade != null)
        {
            trade.setTicketID(ticketID);
            this.menu.getTrader().markTradesDirty();
            if(this.menu.isClient())
            {
                NbtCompound message = new NbtCompound();
                message.putBoolean("NewTicket", true);
                if(ticketID != null)
                    message.putUuid("TicketID", ticketID);
                this.menu.sendMessage(message);
            }
        }
    }

    public void setDuration(int duration) {
        PaygateTradeData trade = this.getTrade();
        if(trade != null)
        {
            trade.setDuration(duration);
            this.menu.getTrader().markTradesDirty();
            if(this.menu.isClient())
            {
                NbtCompound message = new NbtCompound();
                message.putInt("NewDuration", duration);
                this.menu.sendMessage(message);
            }
        }
    }

    @Override
    public void receiveMessage(NbtCompound message) {
        if(message.contains("TradeIndex"))
        {
            this.tradeIndex = message.getInt("TradeIndex");
        }
        else if(message.contains("NewPrice"))
        {
            CoinValue price = new CoinValue();
            price.load(message, "NewPrice");
            this.setPrice(price);
        }
        else if(message.contains("NewTicket"))
        {
            UUID ticketID = null;
            if(message.contains("TicketID"))
                ticketID = message.getUuid("TicketID");
            this.setTicket(ticketID);
        }
        else if(message.contains("NewDuration"))
        {
            this.setDuration(message.getInt("NewDuration"));
        }
    }

}