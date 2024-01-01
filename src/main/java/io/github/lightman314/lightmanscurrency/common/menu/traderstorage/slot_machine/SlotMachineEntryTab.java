package io.github.lightman314.lightmanscurrency.common.menu.traderstorage.slot_machine;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.slot_machine.SlotMachineEntryClientTab;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menu.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menu.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.util.DebugUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;

import java.util.List;
import java.util.function.Function;

public class SlotMachineEntryTab extends TraderStorageTab {

    public SlotMachineEntryTab(TraderStorageMenu menu) { super(menu); }

    @Override
    @Environment(EnvType.CLIENT)
    public TraderStorageClientTab<?> createClientTab(TraderStorageScreen screen) { return new SlotMachineEntryClientTab(screen, this); }

    @Override
    public boolean canOpen(PlayerEntity player) { return true; }

    @Override
    public void onTabOpen() { }

    @Override
    public void onTabClose() { }

    @Override
    public void addStorageMenuSlots(Function<Slot, Slot> addSlot) { }

    public void AddEntry()
    {
        if(!this.menu.hasPermission(Permissions.EDIT_TRADES))
            return;
        if(this.menu.getTrader() instanceof SlotMachineTraderData trader)
        {
            trader.addEntry();
            if(this.menu.isClient())
            {
                NbtCompound message = new NbtCompound();
                message.putBoolean("AddEntry", true);
                this.menu.sendMessage(message);
            }
        }
    }

    public void RemoveEntry(int entryIndex)
    {
        if(!this.menu.hasPermission(Permissions.EDIT_TRADES))
            return;
        if(this.menu.getTrader() instanceof SlotMachineTraderData trader)
        {
            trader.removeEntry(entryIndex);
            if(this.menu.isClient())
            {
                NbtCompound message = new NbtCompound();
                message.putInt("RemoveEntry", entryIndex);
                this.menu.sendMessage(message);
            }
        }
    }

    private void markEntriesDirty()
    {
        if(this.menu.getTrader() instanceof SlotMachineTraderData trader)
            trader.markEntriesDirty();
    }

    private SlotMachineEntry getEntry(int entryIndex)
    {
        if(this.menu.getTrader() instanceof SlotMachineTraderData trader)
        {
            List<SlotMachineEntry> entries = trader.getAllEntries();
            if(entryIndex < 0 || entryIndex >= entries.size())
                return null;
            return entries.get(entryIndex);
        }
        return null;
    }

    public void AddEntryItem(int entryIndex, ItemStack item)
    {
        if(!this.menu.hasPermission(Permissions.EDIT_TRADES))
        {
            Permissions.PermissionWarning(this.menu.player, "edit slot machine trade", Permissions.EDIT_TRADES);
            return;
        }
        SlotMachineEntry entry = this.getEntry(entryIndex);
        if(entry != null)
        {
            //Use TryAddItem to enforce item limit
            entry.TryAddItem(item);
            entry.validateItems();
            this.markEntriesDirty();
            if(this.menu.isClient())
            {
                NbtCompound message = new NbtCompound();
                message.putInt("EditEntry", entryIndex);
                message.put("AddItem", item.writeNbt(new NbtCompound()));
                this.menu.sendMessage(message);
            }
        }
    }

    public void EditEntryItem(int entryIndex, int itemIndex, ItemStack item)
    {
        if(!this.menu.hasPermission(Permissions.EDIT_TRADES))
        {
            Permissions.PermissionWarning(this.menu.player, "edit slot machine trade", Permissions.EDIT_TRADES);
            return;
        }
        if(item.isEmpty())
        {
            this.RemoveEntryItem(entryIndex, itemIndex);
            return;
        }
        SlotMachineEntry entry = this.getEntry(entryIndex);
        if(entry != null)
        {
            if(itemIndex < 0 || itemIndex >= entry.items.size())
                return;
            entry.items.set(itemIndex, item.copy());
            entry.validateItems();
            this.markEntriesDirty();
            if(this.menu.isClient())
            {
                NbtCompound message = new NbtCompound();
                message.putInt("EditEntry", entryIndex);
                message.putInt("ItemIndex", itemIndex);
                message.put("EditItem", item.writeNbt(new NbtCompound()));
                this.menu.sendMessage(message);
            }
        }
    }

    public void RemoveEntryItem(int entryIndex, int itemIndex)
    {
        if(!this.menu.hasPermission(Permissions.EDIT_TRADES))
        {
            Permissions.PermissionWarning(this.menu.player, "edit slot machine trade", Permissions.EDIT_TRADES);
            return;
        }
        SlotMachineEntry entry = this.getEntry(entryIndex);
        if(entry != null)
        {
            if(itemIndex < 0 || itemIndex >= entry.items.size())
                return;
            entry.items.remove(itemIndex);
            entry.validateItems();
            this.markEntriesDirty();
            if(this.menu.isClient())
            {
                NbtCompound message = new NbtCompound();
                message.putInt("EditEntry", entryIndex);
                message.putInt("RemoveItem", itemIndex);
                this.menu.sendMessage(message);
            }
        }
    }

    public void ChangeEntryWeight(int entryIndex, int newWeight)
    {
        if(!this.menu.hasPermission(Permissions.EDIT_TRADES))
        {
            Permissions.PermissionWarning(this.menu.player, "edit slot machine trade", Permissions.EDIT_TRADES);
            return;
        }
        SlotMachineEntry entry = this.getEntry(entryIndex);
        if(entry != null)
        {
            entry.setWeight(newWeight);
            this.markEntriesDirty();
            LightmansCurrency.LogDebug("Changed entry[" + entryIndex + "]'s weight on the " + DebugUtil.getSideText(this.menu.player) + "!");
            if(this.menu.isClient())
            {
                NbtCompound message = new NbtCompound();
                message.putInt("EditEntry", entryIndex);
                message.putInt("SetWeight", newWeight);
                this.menu.sendMessage(message);
            }
        }
    }

    @Override
    public void receiveMessage(NbtCompound message) {
        if(message.contains("AddEntry"))
            this.AddEntry();
        if(message.contains("RemoveEntry"))
            this.RemoveEntry(message.getInt("RemoveEntry"));
        if(message.contains("EditEntry"))
        {
            //LightmansCurrency.LogDebug("Received edit message from client:\n" + message.asString());
            int entryIndex = message.getInt("EditEntry");
            if(message.contains("AddItem"))
            {
                this.AddEntryItem(entryIndex, ItemStack.fromNbt(message.getCompound("AddItem")));
            }
            else if(message.contains("EditItem") && message.contains("ItemIndex"))
            {
                this.EditEntryItem(entryIndex, message.getInt("ItemIndex"), ItemStack.fromNbt(message.getCompound("EditItem")));
            }
            else if(message.contains("RemoveItem"))
            {
                this.RemoveEntryItem(entryIndex, message.getInt("RemoveItem"));
            }
            else if(message.contains("SetWeight"))
            {
                this.ChangeEntryWeight(entryIndex, message.getInt("SetWeight"));
            }
        }
    }


}