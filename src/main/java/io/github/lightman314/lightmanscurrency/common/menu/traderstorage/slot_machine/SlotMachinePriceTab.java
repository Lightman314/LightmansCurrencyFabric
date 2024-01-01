package io.github.lightman314.lightmanscurrency.common.menu.traderstorage.slot_machine;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.slot_machine.SlotMachinePriceClientTab;
import io.github.lightman314.lightmanscurrency.common.menu.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menu.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;

import java.util.function.Function;

public class SlotMachinePriceTab extends TraderStorageTab {


    public SlotMachinePriceTab(TraderStorageMenu menu) { super(menu); }

    @Override
    @Environment(EnvType.CLIENT)
    public TraderStorageClientTab<?> createClientTab(TraderStorageScreen screen) { return new SlotMachinePriceClientTab(screen, this); }

    @Override
    public boolean canOpen(PlayerEntity player) { return this.menu.hasPermission(Permissions.EDIT_TRADES); }

    @Override
    public void onTabOpen() { }

    @Override
    public void onTabClose() { }

    @Override
    public void addStorageMenuSlots(Function<Slot, Slot> addSlot) { }

    public void SetPrice(CoinValue newPrice)
    {
        if(this.menu.hasPermission(Permissions.EDIT_TRADES) && this.menu.getTrader() instanceof SlotMachineTraderData trader)
        {
            trader.setPrice(newPrice);
            if(this.menu.isClient())
            {
                NbtCompound message = new NbtCompound();
                newPrice.save(message, "SetPrice");
                this.menu.sendMessage(message);
            }
        }
    }

    @Override
    public void receiveMessage(NbtCompound message) {
        if(message.contains("SetPrice"))
        {
            CoinValue price = new CoinValue();
            price.load(message, "SetPrice");
            this.SetPrice(price);
        }
    }

}