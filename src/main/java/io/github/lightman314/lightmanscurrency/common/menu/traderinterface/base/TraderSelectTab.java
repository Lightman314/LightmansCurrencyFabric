package io.github.lightman314.lightmanscurrency.common.menu.traderinterface.base;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.base.TraderSelectClientTab;
import io.github.lightman314.lightmanscurrency.common.menu.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.common.menu.traderinterface.TraderInterfaceTab;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;

import java.util.function.Function;

public class TraderSelectTab extends TraderInterfaceTab {

    public TraderSelectTab(TraderInterfaceMenu menu) { super(menu); }

    @Override
    @Environment(EnvType.CLIENT)
    public TraderInterfaceClientTab<?> createClientTab(TraderInterfaceScreen screen) { return new TraderSelectClientTab(screen, this); }

    @Override
    public boolean canOpen(PlayerEntity player) { return true; }

    @Override
    public void onTabOpen() { }

    @Override
    public void onTabClose() { }

    @Override
    public void addStorageMenuSlots(Function<Slot, Slot> addSlot) { }

    public void setTrader(long traderID) {
        this.menu.getTraderInterface().setTrader(traderID);
        //Don't need to mark dirty, as that's done on the BE's side automatically
        if(this.menu.isClient())
        {
            NbtCompound message = new NbtCompound();
            if(traderID >= 0)
                message.putLong("NewTrader", traderID);
            else
                message.putBoolean("NullTrader", true);
            this.menu.sendMessage(message);
        }
    }

    @Override
    public void receiveMessage(NbtCompound message) {
        if(message.contains("NewTrader"))
        {
            this.setTrader(message.getLong("NewTrader"));
        }
        else if(message.contains("NullTrader"))
        {
            this.setTrader(-1);
        }
    }

}