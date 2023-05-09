package io.github.lightman314.lightmanscurrency.common.traders.item.storage;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class TraderItemStorage {

    private final ITraderItemFilter filter;
    private final List<ItemStack> storage = new ArrayList<>();

    public TraderItemStorage(@NotNull ITraderItemFilter filter) { this.filter = filter; }

    public NbtCompound save(NbtCompound compound, String tag) {
        NbtList list = new NbtList();
        for(int i = 0; i < this.storage.size(); ++i)
        {
            ItemStack item = this.storage.get(i);
            if(!item.isEmpty())
            {
                NbtCompound itemTag = new NbtCompound();
                item.writeNbt(itemTag);
                itemTag.putInt("Count", item.getCount());
                list.add(itemTag);
            }
        }
        compound.put(tag, list);
        return compound;
    }

    public void load(NbtCompound compound, String tag) {
        if(compound.contains(tag, NbtElement.LIST_TYPE))
        {
            NbtList list = compound.getList(tag, NbtElement.COMPOUND_TYPE);
            this.storage.clear();
            for(int i = 0; i < list.size(); ++i)
            {
                NbtCompound itemTag = list.getCompound(i);
                ItemStack item = ItemStack.fromNbt(itemTag);
                item.setCount(itemTag.getInt("Count"));
                if(!item.isEmpty())
                    this.storage.add(item);
            }
        }
    }

    public List<ItemStack> getContents() { return this.storage; }

    public List<ItemStack> getSplitContents() {
        List<ItemStack> contents = new ArrayList<>();
        for(ItemStack s : this.storage)
        {
            //Interact with a copy to preserve the original storage data
            ItemStack stack = s.copy();
            int maxCount = stack.getMaxCount();
            while(stack.getCount() > maxCount)
                contents.add(stack.split(maxCount));
            contents.add(stack);
        }
        return contents;
    }

    public final boolean allowExternalInput(ItemStack stack) {
        if(this.filter instanceof ITraderInputOutputFilter f)
            return this.filter.isItemRelevant(stack) && f.allowInput(stack);
        return this.filter.isItemRelevant(stack);
    }

    public final boolean allowExternalOutput(ItemStack stack) {
        if(this.filter instanceof ITraderInputOutputFilter f)
            return !this.filter.isItemRelevant(stack) || f.allowOutput(stack);
        return !this.filter.isItemRelevant(stack);
    }

    public int getSlotCount() { return this.storage.size(); }

    /**
     * Returns whether the item storage has the given item.
     */
    public boolean hasItem(ItemStack item) {
        for(ItemStack stack : this.storage)
        {
            if(InventoryUtil.ItemMatches(stack, item))
            {
                return stack.getCount() >= item.getCount();
            }
        }
        return false;
    }

    /**
     * Returns whether the item storage has the given item.
     */
    public boolean hasItems(ItemStack... items)
    {
        for(ItemStack item : InventoryUtil.combineQueryItems(items))
        {
            if(!this.hasItem(item))
                return false;
        }
        return true;
    }

    /**
     * Returns whether the item storage is allowed to be given this item.
     */
    public boolean allowItem(ItemStack item) {
        if(item.isEmpty())
            return false;
        return this.filter.isItemRelevant(item);
    }

    /**
     * Returns the maximum count of the given item that is allowed to be placed in storage.
     */
    public int getMaxAmount() { return this.filter.getStorageStackLimit(); }

    /**
     * Returns the amount of the given item within the storage.
     * @param item
     * @return
     */
    public int getItemCount(ItemStack item) {
        List<ItemStack> storageCopy = ImmutableList.copyOf(this.storage);
        for(ItemStack stack : storageCopy)
        {
            if(InventoryUtil.ItemMatches(item, stack))
                return stack.getCount();
        }
        return 0;
    }

    /**
     * Returns the amount of the given items containing the given item tag within the storage.
     * Ignores any items listed on the given blacklist.
     */
    public int getItemTagCount(Identifier itemTag, Item... blacklistItems) {

        List<Item> blacklist = Lists.newArrayList(blacklistItems);
        int count = 0;
        List<ItemStack> storageCopy = ImmutableList.copyOf(this.storage);
        for(ItemStack stack : storageCopy)
        {
            if(InventoryUtil.ItemHasTag(stack, itemTag) && !blacklist.contains(stack.getItem()))
                count += stack.getCount();
        }
        return count;
    }

    public int getFittableAmount(ItemStack item) {
        if(!this.allowItem(item))
            return 0;
        return this.getMaxAmount() - this.getItemCount(item);
    }

    /**
     * Returns the amount of the given item that this storage can fit.
     */
    public boolean canFitItem(ItemStack item) { return this.getFittableAmount(item) >= item.getCount(); }

    /**
     * Returns the amount of the given item that this storage can fit.
     */
    public boolean canFitItems(ItemStack... items) {
        for(ItemStack item : InventoryUtil.combineQueryItems(items))
        {
            if(!this.canFitItem(item))
                return false;
        }
        return true;
    }

    /**
     * Attempts to add the entire item stack to storage.
     * @return Whether the item was added. If false, no partial stack was added to storage.
     */
    public boolean addItem(ItemStack item) {
        if(!this.canFitItem(item))
            return false;
        this.forceAddItem(item);
        return true;
    }

    /**
     * Attempts to add as much of the item stack to storage as possible.
     * The input item stack will be shrunk based on the amount that is added.
     * Use this for player interactions where they attempt to place an item in storage.
     */
    public void tryAddItem(ItemStack item) {
        if(!this.allowItem(item))
            return;
        int amountToAdd = Math.min(item.getCount(), this.getFittableAmount(item));
        if(amountToAdd > 0)
        {
            ItemStack addStack = item.split(amountToAdd);
            this.forceAddItem(addStack);
        }
    }

    /**
     * Adds the item without performing any checks on maximum quantity or trade verification.
     * Used to add item to storage from older systems.
     * @param item
     */
    public void forceAddItem(ItemStack item) {
        if(item.isEmpty())
            return;
        for(int i = 0; i < this.storage.size(); ++i)
        {
            ItemStack stack = this.storage.get(i);
            if(InventoryUtil.ItemMatches(stack, item))
            {
                stack.increment(item.getCount());
                return;
            }
        }
        this.storage.add(item.copy());
    }

    /**
     * Removes the requested item from storage. Limits the amount removed by the stacks maximum stack size.
     * @return The item that was removed successfully.
     */
    public ItemStack removeItem(ItemStack item) {
        if(!this.hasItem(item))
            return ItemStack.EMPTY;
        for(int i = 0; i < this.storage.size(); ++i)
        {
            ItemStack stack = this.storage.get(i);
            if(InventoryUtil.ItemMatches(item, stack))
            {
                int amountToRemove = Math.min(item.getCount(), item.getMaxCount());
                ItemStack output = stack.split(amountToRemove);
                if(stack.isEmpty())
                    this.storage.remove(i);
                return output;
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Removes the requested amount of items with the given item tag from storage.
     * Ignores items within the given blacklist.
     * @return Whether the items were removed successfully.
     */
    public void removeItemTagCount(Identifier itemTag, int count, List<ItemStack> ignoreIfPossible, Item... blacklistItems) {
        List<Item> blacklist = Lists.newArrayList(blacklistItems);
        //First pass, honoring the "ignoreIfPossible" list
        for(int i = 0; i < this.storage.size() && count > 0; ++i)
        {
            ItemStack stack = this.storage.get(i);
            if(InventoryUtil.ItemHasTag(stack, itemTag) && !blacklist.contains(stack.getItem()) && !ListContains(ignoreIfPossible, stack))
            {
                int amountToTake = Math.min(count, stack.getCount());
                count-= amountToTake;
                stack.decrement(amountToTake);
                if(stack.isEmpty())
                {
                    this.storage.remove(i);
                    i--;
                }
            }
        }
        //Second pass, ignoring the "ignoreIfPossible" list
        for(int i = 0; i < this.storage.size() && count > 0; ++i)
        {
            ItemStack stack = this.storage.get(i);
            if(InventoryUtil.ItemHasTag(stack, itemTag) && !blacklist.contains(stack.getItem()))
            {
                int amountToTake = Math.min(count, stack.getCount());
                count-= amountToTake;
                stack.decrement(amountToTake);
                if(stack.isEmpty())
                {
                    this.storage.remove(i);
                    i--;
                }
            }
        }

    }

    private static boolean ListContains(List<ItemStack> list, ItemStack stack) {
        for(ItemStack item : list)
        {
            if(InventoryUtil.ItemMatches(item, stack))
                return true;
        }
        return false;
    }

    public static class LockedTraderStorage extends TraderItemStorage {

        public LockedTraderStorage(ITraderItemFilter  filter) { super(filter); }

        @Override
        public boolean allowItem(ItemStack item) { return false; }

    }

    public interface ITraderItemFilter
    {
        boolean isItemRelevant(ItemStack item);
        int getStorageStackLimit();
    }

    public interface ITraderInputOutputFilter extends ITraderItemFilter
    {
        boolean allowInput(ItemStack item);
        boolean allowOutput(ItemStack item);
    }

    public int getSlots() { return this.storage.size(); }

    public ItemStack getStackInSlot(int slot) {
        if(slot >= 0 && slot < this.storage.size())
            return this.storage.get(slot);
        return ItemStack.EMPTY;
    }

}