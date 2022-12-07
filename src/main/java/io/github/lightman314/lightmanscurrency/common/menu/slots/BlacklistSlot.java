package io.github.lightman314.lightmanscurrency.common.menu.slots;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class BlacklistSlot extends Slot {

    Inventory blacklistInventory;
    int blacklistIndex;

    public BlacklistSlot(Inventory inventory, int index, int x, int y, Inventory blacklistInventory, int blacklistIndex)
    {
        super(inventory, index, x, y);
        setBlacklist(blacklistInventory, blacklistIndex);
    }

    public void setBlacklist(Inventory blacklistInventory, int blacklistIndex)
    {
        this.blacklistInventory = blacklistInventory;
        this.blacklistIndex = blacklistIndex;
    }

    public ItemStack getBlacklistedItem()  { return this.blacklistInventory.getStack(this.blacklistIndex); }

    @Override
    public boolean canInsert(ItemStack item)
    {
        if(this.blacklistIndex >= 0)
            return item != this.getBlacklistedItem();
        return true;
    }

}