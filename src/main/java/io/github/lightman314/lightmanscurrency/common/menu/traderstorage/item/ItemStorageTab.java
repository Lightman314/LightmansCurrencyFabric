package io.github.lightman314.lightmanscurrency.common.menu.traderstorage.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.item.ItemStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menu.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menu.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.common.menu.slots.trader.UpgradeInputSlot;
import io.github.lightman314.lightmanscurrency.common.menu.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.storage.TraderItemStorage;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.slot.Slot;

public class ItemStorageTab extends TraderStorageTab {

    public ItemStorageTab(TraderStorageMenu menu) { super(menu); }

    @Override
    @Environment(EnvType.CLIENT)
    public TraderStorageClientTab<?> createClientTab(TraderStorageScreen screen) { return new ItemStorageClientTab(screen, this); }

    @Override
    public boolean canOpen(PlayerEntity player) { return this.menu.getTrader() instanceof ItemTraderData; }

    List<SimpleSlot> slots = new ArrayList<>();
    public List<? extends Slot> getSlots() { return this.slots; }

    @Override
    public void addStorageMenuSlots(Function<Slot, Slot> addSlot) {
        //Upgrade Slots
        if(this.menu.getTrader() instanceof ItemTraderData trader)
        {
            for(int i = 0; i < trader.getUpgrades().size(); ++i)
            {
                SimpleSlot upgradeSlot = new UpgradeInputSlot(trader.getUpgrades(), i, 176, 18 + 18 * i, trader);
                upgradeSlot.active = false;
                addSlot.apply(upgradeSlot);
                this.slots.add(upgradeSlot);
            }
        }
    }

    @Override
    public void onTabOpen() { SimpleSlot.SetActive(this.slots); }

    @Override
    public void onTabClose() { SimpleSlot.SetInactive(this.slots); }


    @Override
    public boolean quickMoveStack(ItemStack stack) {
        if(this.menu.getTrader() instanceof ItemTraderData trader) {
            TraderItemStorage storage = trader.getStorage();
            if(storage.getFittableAmount(stack) > 0)
            {
                storage.tryAddItem(stack);
                trader.markStorageDirty();
                return true;
            }
        }
        return super.quickMoveStack(stack);
    }

    public void clickedOnSlot(int storageSlot, boolean isShiftHeld, boolean leftClick) {
        if(this.menu.getTrader() instanceof ItemTraderData trader)
        {
            TraderItemStorage storage = trader.getStorage();
            ItemStack heldItem = this.menu.getCursorStack();
            if(heldItem.isEmpty())
            {
                //Move item out of storage
                List<ItemStack> storageContents = storage.getContents();
                if(storageSlot >= 0 && storageSlot < storageContents.size())
                {
                    ItemStack stackToRemove = storageContents.get(storageSlot).copy();
                    ItemStack removeStack = stackToRemove.copy();

                    //Assume we're moving a whole stack for now
                    int tempAmount = Math.min(stackToRemove.getMaxCount(), stackToRemove.getCount());
                    stackToRemove.setCount(tempAmount);
                    int removedAmount = 0;

                    //Right-click, attempt to cut the stack in half
                    if(!leftClick)
                    {
                        if(tempAmount > 1)
                            tempAmount = tempAmount / 2;
                        stackToRemove.setCount(tempAmount);
                    }

                    if(isShiftHeld)
                    {
                        //Put the item in the players inventory. Will not throw overflow on the ground, so it will safely stop if the players inventory is full
                        this.menu.player.getInventory().insertStack(stackToRemove);
                        //Determine the amount actually added to the players inventory
                        removedAmount = tempAmount - stackToRemove.getCount();
                    }
                    else
                    {
                        //Put the item into the players hand
                        this.menu.setCursorStack(stackToRemove);
                        removedAmount = tempAmount;
                    }
                    //Remove the correct amount from storage
                    if(removedAmount > 0)
                    {
                        removeStack.setCount(removedAmount);
                        storage.removeItem(removeStack);
                        //Mark the storage dirty
                        trader.markStorageDirty();
                    }
                }
            }
            else
            {
                //Move from hand to storage
                if(leftClick)
                {
                    storage.tryAddItem(heldItem);
                    //Mark the storage dirty
                    trader.markStorageDirty();
                }
                else
                {
                    //Right click, only attempt to add 1 from the hand
                    ItemStack addItem = heldItem.copy();
                    addItem.setCount(1);
                    if(storage.addItem(addItem))
                    {
                        heldItem.decrement(1);
                        if(heldItem.isEmpty())
                            this.menu.setCursorStack(ItemStack.EMPTY);
                    }
                    //Mark the storage dirty
                    trader.markStorageDirty();
                }
            }
            if(this.menu.isClient())
                this.sendStorageClickMessage(storageSlot, isShiftHeld, leftClick);
        }
    }

    private void sendStorageClickMessage(int storageSlot, boolean isShiftHeld, boolean leftClick) {
        NbtCompound message = new NbtCompound();
        message.putInt("ClickedSlot", storageSlot);
        message.putBoolean("HeldShift", isShiftHeld);
        message.putBoolean("LeftClick", leftClick);
        this.menu.sendMessage(message);
    }

    public void quickTransfer(int type) {
        if(this.menu.getTrader() instanceof ItemTraderData trader) {
            TraderItemStorage storage = trader.getStorage();
            PlayerInventory inv = this.menu.player.getInventory();
            boolean changed = false;
            if(type == 0)
            {
                //Quick Deposit
                for(int i = 0; i < 36; ++i)
                {
                    ItemStack stack = inv.getStack(i);
                    int fillAmount = storage.getFittableAmount(stack);
                    if(fillAmount > 0)
                    {
                        //Remove the item from the players inventory
                        ItemStack fillStack = inv.removeStack(i, fillAmount);
                        //Put the item into storage
                        storage.forceAddItem(fillStack);
                    }
                }
            }
            else if(type == 1)
            {
                //Quick Extract
                List<ItemStack> itemList = InventoryUtil.copyList(storage.getContents());
                for(ItemStack stack : itemList)
                {
                    boolean keepTrying = true;
                    while(storage.getItemCount(stack) > 0 && keepTrying)
                    {
                        ItemStack transferStack = stack.copy();
                        int transferCount = Math.min(storage.getItemCount(stack), stack.getMaxCount());
                        transferStack.setCount(transferCount);
                        //Attempt to move the stack into the players inventory
                        int removedCount = InventoryUtil.safeGiveToPlayer(inv, transferStack);
                        if(removedCount > 0)
                        {
                            changed = true;
                            //Remove the transferred amount from storage
                            ItemStack removeStack = stack.copy();
                            removeStack.setCount(removedCount);
                            storage.removeItem(removeStack);
                        }
                        else
                            keepTrying = false;
                    }
                }
            }

            if(changed)
                trader.markStorageDirty();

            if(this.menu.isClient())
            {
                NbtCompound message = new NbtCompound();
                message.putInt("QuickTransfer", type);
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
            boolean leftClick = message.getBoolean("LeftClick");
            this.clickedOnSlot(storageSlot, isShiftHeld, leftClick);
        }
        if(message.contains("QuickTransfer"))
        {
            this.quickTransfer(message.getInt("QuickTransfer"));
        }
    }

}