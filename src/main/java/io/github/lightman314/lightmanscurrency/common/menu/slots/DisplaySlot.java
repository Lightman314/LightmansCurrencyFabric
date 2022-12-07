package io.github.lightman314.lightmanscurrency.common.menu.slots;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class DisplaySlot extends SimpleSlot {

    public DisplaySlot(Inventory inventory, int index, int x, int y) { super(inventory, index, x, y); }

    @Override
    public boolean canInsert(ItemStack item) { return false; }

    @Override
    public boolean canTakeItems(PlayerEntity player) { return false; }

    @Override
    public void setStack(ItemStack stack) { }

}