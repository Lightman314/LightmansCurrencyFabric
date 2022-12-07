package io.github.lightman314.lightmanscurrency.common.menu.traderinterface.base;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.base.TradeSelectClientTab;
import io.github.lightman314.lightmanscurrency.common.menu.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.common.menu.traderinterface.TraderInterfaceTab;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;

import java.util.function.Function;


public class TradeSelectTab extends TraderInterfaceTab {

    public TradeSelectTab(TraderInterfaceMenu menu) { super(menu); }

    @Override
    @Environment(EnvType.CLIENT)
    public TraderInterfaceClientTab<?> createClientTab(TraderInterfaceScreen screen) { return new TradeSelectClientTab(screen, this); }

    @Override
    public boolean canOpen(PlayerEntity player) {
        return this.menu.getTraderInterface().getInteractionType().trades && this.menu.getTraderInterface().getTrader() != null;
    }

    @Override
    public void onTabOpen() { }

    @Override
    public void onTabClose() { }

    @Override
    public void addStorageMenuSlots(Function<Slot, Slot> addSlot) { }

    public void setTradeIndex(int tradeIndex) {
        //LightmansCurrency.LogInfo("Setting trade index to " + tradeIndex + " on the " + DebugUtil.getSideText(this.menu.player));
        if(this.menu.getTraderInterface().canAccess(this.menu.player))
        {
            this.menu.getTraderInterface().setTradeIndex(tradeIndex);
            if(this.menu.isClient())
            {
                NbtCompound message = new NbtCompound();
                message.putInt("NewTradeIndex", tradeIndex);
                this.menu.sendMessage(message);
            }
        }
    }

    @Override
    public void receiveMessage(NbtCompound message) {
        if(message.contains("NewTradeIndex"))
        {
            this.setTradeIndex(message.getInt("NewTradeIndex"));
        }
    }

}