package io.github.lightman314.lightmanscurrency.common.menu.slots.mint;

import io.github.lightman314.lightmanscurrency.common.blockentity.CoinMintBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class MintSlot extends Slot {

    CoinMintBlockEntity tileEntity;

    public MintSlot(Inventory inventory, int index, int x, int y, CoinMintBlockEntity tileEntity)
    {
        super(inventory, index, x, y);
        this.tileEntity = tileEntity;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return this.tileEntity.validMintInput(stack);
    }

}