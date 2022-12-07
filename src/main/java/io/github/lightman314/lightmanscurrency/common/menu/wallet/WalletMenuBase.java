package io.github.lightman314.lightmanscurrency.common.menu.wallet;

import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.menu.Menu;
import io.github.lightman314.lightmanscurrency.common.menu.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.common.menu.slots.DisplaySlot;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.common.money.wallet.WalletHandler;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.collection.DefaultedList;

public abstract class WalletMenuBase extends Menu {

    private static int maxWalletSlots = 0;
    public static int getMaxWalletSlots() { return maxWalletSlots; }
    public static void updateMaxWalletSlots(int slotCount) { maxWalletSlots = Math.max(maxWalletSlots, slotCount); }

    protected final Inventory dummyInventory = new SimpleInventory(1);

    protected final int walletStackIndex;
    public final boolean isEquippedWallet() { return this.walletStackIndex < 0; }
    public final int getWalletStackIndex() { return this.walletStackIndex; }

    protected final PlayerInventory inventory;
    public final boolean hasWallet() { ItemStack wallet = this.getWallet(); return !wallet.isEmpty() && wallet.getItem() instanceof WalletItem; }
    public final ItemStack getWallet()
    {
        if(this.isEquippedWallet())
            return WalletHandler.getWallet(this.player).getWallet();
        return this.inventory.getStack(this.walletStackIndex);
    }

    private boolean autoConvert;
    public boolean canConvert() { return WalletItem.CanExchange(this.walletItem); }
    public boolean canPickup() { return WalletItem.CanPickup(this.walletItem); }
    public boolean hasBankAccess() { return WalletItem.HasBankAccess(this.walletItem); }
    public boolean getAutoConvert() { return this.autoConvert; }
    public void ToggleAutoExchange() { this.autoConvert = !this.autoConvert; this.saveWalletContents(); }

    protected final Inventory coinInput;

    protected final WalletItem walletItem;

    public final PlayerEntity player;
    public PlayerEntity getPlayer() { return this.player; }

    protected WalletMenuBase(ScreenHandlerType<?> type, int windowID, PlayerInventory inventory, int walletStackIndex) {
        super(type, windowID);

        this.inventory = inventory;
        this.player = this.inventory.player;

        this.walletStackIndex = walletStackIndex;

        Item item = this.getWallet().getItem();
        if(item instanceof WalletItem)
            this.walletItem = (WalletItem)item;
        else
            this.walletItem = null;

        this.coinInput = new SimpleInventory(WalletItem.InventorySize(this.walletItem));
        this.reloadWalletContents();

        this.autoConvert = WalletItem.getAutoConvert(this.getWallet());

    }

    protected final void addCoinSlots(int yPosition) {
        for(int y = 0; (y * 9) < this.coinInput.size(); y++)
        {
            for(int x = 0; x < 9 && (x + y * 9) < this.coinInput.size(); x++)
            {
                this.addSlot(new CoinSlot(this.coinInput, x + y * 9, 8 + x * 18, yPosition + y * 18).addListener(this::saveWalletContents));
            }
        }
    }

    protected final void addDummySlots(int slotLimit) {
        while(this.slots.size() < slotLimit) {
            this.addSlot(new DisplaySlot(this.dummyInventory, 0, Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2));
        }
    }

    public final void reloadWalletContents() {
        DefaultedList<ItemStack> walletInventory = WalletItem.getWalletInventory(getWallet());
        for(int i = 0; i < this.coinInput.size() && i < walletInventory.size(); i++)
        {
            this.coinInput.setStack(i, walletInventory.get(i));
        }
    }

    public final int getRowCount() { return 1 + ((this.coinInput.size() - 1)/9); }

    public final int getSlotCount() { return this.coinInput.size(); }

    @Override
    public boolean canUse(PlayerEntity playerIn) { return this.hasWallet(); }

    public final void saveWalletContents()
    {
        if(!this.hasWallet())
            return;
        //Write the bag contents back into the item stack
        DefaultedList<ItemStack> walletInventory = DefaultedList.ofSize(WalletItem.InventorySize(this.walletItem), ItemStack.EMPTY);
        for(int i = 0; i < walletInventory.size() && i < this.coinInput.size(); i++)
        {
            walletInventory.set(i, this.coinInput.getStack(i));
        }
        WalletItem.putWalletInventory(this.getWallet(), walletInventory);

        if(this.autoConvert != WalletItem.getAutoConvert(this.getWallet()))
            WalletItem.toggleAutoConvert(this.getWallet());

    }

    public final void ExchangeCoins()
    {
        MoneyUtil.ConvertAllCoinsUp(this.coinInput);
        MoneyUtil.SortCoins(this.coinInput);
        this.saveWalletContents();
    }

    public final ItemStack PickupCoins(ItemStack stack)
    {

        ItemStack returnValue = stack.copy();

        for(int i = 0; i < this.coinInput.size() && !returnValue.isEmpty(); i++)
        {
            ItemStack thisStack = this.coinInput.getStack(i);
            if(thisStack.isEmpty())
            {
                this.coinInput.setStack(i, returnValue.copy());
                returnValue = ItemStack.EMPTY;
            }
            else if(InventoryUtil.ItemMatches(thisStack, returnValue))
            {
                int amountToAdd = MathUtil.clamp(returnValue.getCount(), 0, thisStack.getMaxCount() - thisStack.getCount());
                thisStack.setCount(thisStack.getCount() + amountToAdd);
                returnValue.setCount(returnValue.getCount() - amountToAdd);
            }
        }

        if(this.autoConvert)
            this.ExchangeCoins();
        else
            this.saveWalletContents();

        return returnValue;
    }


}