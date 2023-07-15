package io.github.lightman314.lightmanscurrency.common.menu.slots;

import java.util.ArrayList;
import java.util.List;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;

public class CoinSlot extends SimpleSlot{

    public static final Identifier EMPTY_COIN_SLOT = new Identifier(LightmansCurrency.MODID, "item/empty_coin_slot");

    private boolean acceptHiddenCoins;

    private boolean lockInput = false;
    public void LockInput() { this.lockInput = true; }
    public void UnlockInput() { this.lockInput = false; }
    private boolean lockOutput = false;
    public void LockOutput() { this.lockOutput = true; }
    public void UnlockOutput() { this.lockOutput = false; }
    public void Lock() { this.lockInput = this.lockOutput = true; }
    public void Unlock() { this.lockInput = this.lockOutput = false; }

    private List<Runnable> listeners = new ArrayList<>();

    public CoinSlot(Inventory inventory, int index, int x, int y)
    {
        this(inventory, index, x, y, true);
    }

    public CoinSlot(Inventory inventory, int index, int x, int y, boolean acceptHiddenCoins)
    {
        super(inventory, index, x, y);
        this.acceptHiddenCoins = acceptHiddenCoins;
    }

    public CoinSlot addListener(Runnable listener)
    {
        if(!listeners.contains(listener))
            listeners.add(listener);
        return this;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        if(lockInput)
            return false;
        if(acceptHiddenCoins)
            return MoneyUtil.isCoin(stack.getItem());
        else
            return MoneyUtil.isCoin(stack.getItem()) && !MoneyUtil.isCoinHidden(stack.getItem());
    }

    @Override
    public void setStack(ItemStack stack) {
        if(this.lockInput && !stack.isEmpty())
            return;
        super.setStack(stack);
    }

    @Override
    public ItemStack takeStack(int amount) {
        if(this.lockOutput)
            return ItemStack.EMPTY;
        return super.takeStack(amount);
    }

    @Override
    public boolean canTakeItems(PlayerEntity player) {
        if(this.lockOutput)
            return false;
        return super.canTakeItems(player);
    }

    @Override
    public Pair<Identifier,Identifier> getBackgroundSprite() { return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, EMPTY_COIN_SLOT); }

    @Override
    public void markDirty() {
        super.markDirty();
        this.listeners.forEach(listener -> listener.run());
    }

}