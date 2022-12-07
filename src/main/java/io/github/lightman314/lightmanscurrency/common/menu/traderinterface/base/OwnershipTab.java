package io.github.lightman314.lightmanscurrency.common.menu.traderinterface.base;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.base.OwnershipClientTab;
import io.github.lightman314.lightmanscurrency.common.menu.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.common.menu.traderinterface.TraderInterfaceTab;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;

import java.util.function.Function;

public class OwnershipTab extends TraderInterfaceTab {

    public OwnershipTab(TraderInterfaceMenu menu) { super(menu); }

    @Override
    @Environment(EnvType.CLIENT)
    public TraderInterfaceClientTab<?> createClientTab(TraderInterfaceScreen screen) { return new OwnershipClientTab(screen, this); }

    private boolean isAdmin() { return this.menu.getTraderInterface().isOwner(this.menu.player); }

    @Override
    public boolean canOpen(PlayerEntity player) { return this.isAdmin(); }

    @Override
    public void onTabOpen() { }

    @Override
    public void onTabClose() { }

    @Override
    public void addStorageMenuSlots(Function<Slot, Slot> addSlot) { }

    public void setNewOwner(String newOwner) {
        if(this.isAdmin())
        {
            if(this.menu.isClient())
            {
                NbtCompound message = new NbtCompound();
                message.putString("NewOwner", newOwner);
                this.menu.sendMessage(message);
            }
            else
                this.menu.getTraderInterface().setOwner(newOwner);
        }
    }

    public void setNewTeam(long team) {
        if(this.isAdmin() && team >= 0)
        {
            this.menu.getTraderInterface().setTeam(team);
            if(this.menu.isClient())
            {
                NbtCompound message = new NbtCompound();
                message.putLong("NewTeam", team);
                this.menu.sendMessage(message);
            }
        }
    }

    @Override
    public void receiveMessage(NbtCompound message) {
        if(message.contains("NewOwner"))
        {
            this.setNewOwner(message.getString("NewOwner"));
        }
        if(message.contains("NewTeam"))
        {
            this.setNewTeam(message.getLong("NewTeam"));
        }
    }

}