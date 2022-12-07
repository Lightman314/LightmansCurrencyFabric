package io.github.lightman314.lightmanscurrency.common.menu.slots;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class OutputSlot extends SimpleSlot {

    public OutputSlot(Inventory inventory, int index, int x, int y)
    {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }

}