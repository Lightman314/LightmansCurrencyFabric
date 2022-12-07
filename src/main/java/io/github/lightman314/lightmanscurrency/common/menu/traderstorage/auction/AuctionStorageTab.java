package io.github.lightman314.lightmanscurrency.common.menu.traderstorage.auction;

import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.auction.AuctionStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menu.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menu.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionPlayerStorage;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.slot.Slot;

public class AuctionStorageTab extends TraderStorageTab {

    public AuctionStorageTab(TraderStorageMenu menu) { super(menu); }

    @Override
    @Environment(EnvType.CLIENT)
    public TraderStorageClientTab<?> createClientTab(TraderStorageScreen screen) { return new AuctionStorageClientTab(screen, this); }

    @Override
    public boolean canOpen(PlayerEntity player) { return this.menu.getTrader() instanceof AuctionHouseTrader; }

    @Override
    public void addStorageMenuSlots(Function<Slot, Slot> addSlot) { }

    @Override
    public void onTabOpen() { }

    @Override
    public void onTabClose() { }

    public void clickedOnSlot(int storageSlot, boolean isShiftHeld)
    {
        TraderData t = this.menu.getTrader();
        if(t instanceof AuctionHouseTrader trader)
        {
            AuctionPlayerStorage storage = trader.getStorage(this.menu.player);
            if(storageSlot >= 0 && storageSlot < storage.getStoredItems().size())
            {
                ItemStack storedItem = storage.getStoredItems().get(storageSlot);
                if(storedItem.isEmpty())
                {
                    storage.getStoredItems().remove(storageSlot);
                    trader.markStorageDirty();
                }
                else
                {
                    ItemStack heldItem = this.menu.getCursorStack();
                    if(isShiftHeld)
                    {
                        //Move as much of the stored item from the slot into the players inventory
                        this.menu.player.getInventory().insertStack(storedItem);
                        if(storedItem.isEmpty())
                            storage.getStoredItems().remove(storageSlot);
                        trader.markStorageDirty();
                    }
                    else if(heldItem.isEmpty())
                    {
                        this.menu.setCursorStack(storedItem);
                        storage.getStoredItems().remove(storageSlot);
                        trader.markStorageDirty();
                    }
                    else if(InventoryUtil.ItemMatches(storedItem, heldItem))
                    {
                        int transferCount = Math.min(heldItem.getMaxCount() - heldItem.getCount(), storedItem.getCount());
                        if(transferCount > 0)
                        {
                            //Add to the held item
                            heldItem.increment(transferCount);
                            this.menu.setCursorStack(heldItem);
                            //Shrink the storage count
                            storedItem.decrement(transferCount);
                            if(storedItem.isEmpty())
                                storage.getStoredItems().remove(storageSlot);
                            trader.markStorageDirty();
                        }
                    }
                }
            }
            if(this.menu.isClient())
            {
                NbtCompound message = new NbtCompound();
                message.putInt("ClickedSlot", storageSlot);
                message.putBoolean("HeldShift", isShiftHeld);
                this.menu.sendMessage(message);
            }
        }
    }

    public void quickTransfer() {
        TraderData t = this.menu.getTrader();
        if(t instanceof AuctionHouseTrader trader)
        {
            AuctionPlayerStorage storage = trader.getStorage(this.menu.player);
            storage.collectItems(this.menu.player);
            trader.markStorageDirty();

            if(this.menu.isClient())
            {
                NbtCompound message = new NbtCompound();
                message.putBoolean("QuickTransfer", true);
                this.menu.sendMessage(message);
            }
        }
    }

    public void collectCoins() {
        TraderData t = this.menu.getTrader();
        if(t instanceof AuctionHouseTrader trader)
        {
            AuctionPlayerStorage storage = trader.getStorage(this.menu.player);
            storage.collectedMoney(this.menu.player);
            trader.markStorageDirty();

            if(this.menu.isClient())
            {
                NbtCompound message = new NbtCompound();
                message.putBoolean("CollectMoney", true);
                this.menu.sendMessage(message);
            }
        }
    }

    @Override
    public void receiveMessage(NbtCompound message) {
        if(message.contains("ClickedSlot", NbtElement.INT_TYPE))
        {
            int storageSlot = message.getInt("ClickedSlot");
            boolean isShiftHeld = message.getBoolean("HeldShift");
            this.clickedOnSlot(storageSlot, isShiftHeld);
        }
        if(message.contains("QuickTransfer"))
        {
            this.quickTransfer();
        }
        if(message.contains("CollectMoney"))
        {
            this.collectCoins();
        }
    }

}