package io.github.lightman314.lightmanscurrency.common.menu.traderinterface.base;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.base.InfoClientTab;
import io.github.lightman314.lightmanscurrency.common.blockentity.traderinterface.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.menu.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.common.menu.traderinterface.TraderInterfaceTab;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;

import java.util.function.Function;

public class InfoTab extends TraderInterfaceTab {

    public InfoTab(TraderInterfaceMenu menu) { super(menu); }

    @Override
    @Environment(EnvType.CLIENT)
    public TraderInterfaceClientTab<?> createClientTab(TraderInterfaceScreen screen) { return new InfoClientTab(screen, this); }

    @Override
    public boolean canOpen(PlayerEntity player) { return true; }

    @Override
    public void onTabOpen() { }

    @Override
    public void onTabClose() { }

    @Override
    public void addStorageMenuSlots(Function<Slot, Slot> addSlot) { }

    public void acceptTradeChanges() {
        if(this.menu.getTraderInterface().canAccess(this.menu.player))
        {
            this.menu.getTraderInterface().acceptTradeChanges();
            if(this.menu.isClient())
            {
                NbtCompound message = new NbtCompound();
                message.putBoolean("AcceptTradeChanges", true);
                this.menu.sendMessage(message);
            }
        }
    }

    public void changeInteractionType(TraderInterfaceBlockEntity.InteractionType newType) {
        if(this.menu.getTraderInterface().canAccess(this.menu.player))
        {
            this.menu.getTraderInterface().setInteractionType(newType);
            if(this.menu.isClient())
            {
                NbtCompound message = new NbtCompound();
                message.putInt("NewInteractionType", newType.index);
                this.menu.sendMessage(message);
            }
        }
    }

    @Override
    public void receiveMessage(NbtCompound message) {
        if(message.contains("NewInteractionType"))
        {
            TraderInterfaceBlockEntity.InteractionType newType = TraderInterfaceBlockEntity.InteractionType.fromIndex(message.getInt("NewInteractionType"));
            this.changeInteractionType(newType);
        }
        if(message.contains("AcceptTradeChanges"))
        {
            this.acceptTradeChanges();
        }
    }

}