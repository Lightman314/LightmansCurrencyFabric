package io.github.lightman314.lightmanscurrency.common.menu.slots;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

public class WalletSlot extends Slot {

    public static final Identifier EMPTY_WALLET_SLOT = new Identifier(LightmansCurrency.MODID, "item/empty_wallet_slot");
    public static final Pair<Identifier,Identifier> BACKGROUND = Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, EMPTY_WALLET_SLOT);

    private final List<Runnable> listeners = Lists.newArrayList();

    Inventory blacklistInventory;
    int blacklistIndex;

    public WalletSlot(Inventory inventory, int index, int x, int y)
    {
        super(inventory, index, x, y);
    }

    public WalletSlot addListener(Runnable listener)
    {
        if(!listeners.contains(listener))
            listeners.add(listener);
        return this;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        if(this.blacklistIndex >= 0 && this.blacklistInventory != null)
        {
            if(stack == this.getBlacklistedItem())
                return false;
        }
        return isValidWallet(stack);
    }

    public static boolean isValidWallet(ItemStack stack) {
        return stack.getItem() instanceof WalletItem && !stack.isEmpty();
    }

    @Override
    public Pair<Identifier,Identifier> getBackgroundSprite() { return BACKGROUND; }

    public void markDirty() {
        super.markDirty();
        this.listeners.forEach(listener -> listener.run());
    }

    public void setBlacklist(Inventory blacklistInventory, int blacklistIndex)
    {
        this.blacklistInventory = blacklistInventory;
        this.blacklistIndex = blacklistIndex;
    }

    public ItemStack getBlacklistedItem()
    {
        return this.blacklistInventory.getStack(this.blacklistIndex);
    }

}