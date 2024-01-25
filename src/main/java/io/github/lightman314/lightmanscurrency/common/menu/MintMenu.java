package io.github.lightman314.lightmanscurrency.common.menu;

import io.github.lightman314.lightmanscurrency.common.blockentity.CoinMintBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.menu.slots.OutputSlot;
import io.github.lightman314.lightmanscurrency.common.menu.slots.mint.MintSlot;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;

public class MintMenu extends Menu {

    public final CoinMintBlockEntity coinMint;

    public MintMenu(int windowId, PlayerInventory inventory, BlockPos blockPos)
    {
        super(ModMenus.MINT, windowId);
        BlockEntity be = inventory.player.getWorld().getBlockEntity(blockPos);
        if(be instanceof CoinMintBlockEntity cm)
            this.coinMint = cm;
        else
            this.coinMint = null;

        if(this.coinMint == null)
            return;

        //Slots
        this.addSlot(new MintSlot(this.coinMint.getStorage(), 0, 56, 21, this.coinMint));
        this.addSlot(new OutputSlot(this.coinMint.getStorage(), 1, 116, 21));

        //Player inventory
        for(int y = 0; y < 3; y++)
        {
            for(int x = 0; x < 9; x++)
            {
                this.addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 56 + y * 18));
            }
        }
        //Player hotbar
        for(int x = 0; x < 9; x++)
        {
            this.addSlot(new Slot(inventory, x, 8 + x * 18, 114));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) { return this.coinMint != null && !this.coinMint.isRemoved(); }

    @Override
    public void onClosed(PlayerEntity player) { super.onClosed(player); }

    @Override
    public ItemStack quickMove(PlayerEntity playerEntity, int index)
    {

        ItemStack clickedStack = ItemStack.EMPTY;

        Slot slot = this.slots.get(index);

        if(slot != null && slot.hasStack())
        {
            ItemStack slotStack = slot.getStack();
            clickedStack = slotStack.copy();
            if(index < this.coinMint.getStorage().size())
            {
                if(!this.insertItem(slotStack, this.coinMint.getStorage().size(), this.slots.size(), true))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if(!this.insertItem(slotStack, 0, this.coinMint.getStorage().size() - 1, false))
            {
                return ItemStack.EMPTY;
            }

            if(slotStack.isEmpty())
            {
                slot.setStack(ItemStack.EMPTY);
            }
            else
            {
                slot.markDirty();
            }
        }

        return clickedStack;

    }

}