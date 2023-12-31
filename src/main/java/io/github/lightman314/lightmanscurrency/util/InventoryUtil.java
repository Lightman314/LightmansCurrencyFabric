package io.github.lightman314.lightmanscurrency.util;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class InventoryUtil {


    public static SimpleInventory buildInventory(List<ItemStack> list)
    {
        SimpleInventory inventory = new SimpleInventory(list.size());
        for(int i = 0; i < list.size(); i++)
        {
            inventory.setStack(i, list.get(i).copy());
        }
        return inventory;
    }

    public static SimpleInventory buildInventory(ItemStack stack)
    {
        SimpleInventory inventory = new SimpleInventory(1);
        inventory.setStack(0, stack);
        return inventory;
    }

    public static SimpleInventory copyInventory(Inventory inventory)
    {
        SimpleInventory copy = new SimpleInventory(inventory.size());
        for(int i = 0; i < inventory.size(); i++)
        {
            copy.setStack(i, inventory.getStack(i).copy());
        }
        return copy;
    }

    public static DefaultedList<ItemStack> buildList(Inventory inventory)
    {
        DefaultedList<ItemStack> list = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);
        for(int i = 0; i < inventory.size(); i++)
        {
            list.set(i, inventory.getStack(i).copy());
        }
        return list;
    }

    public static List<ItemStack> copyList(List<ItemStack> list) {
        List<ItemStack> result = new ArrayList<>();
        for(ItemStack stack : list)
            result.add(stack.copy());
        return result;
    }

    /**
     * Gets the quantity of a specific item in the given inventory
     * Ignores NBT data, as none is given
     */
    public static int GetItemCount(Inventory inventory, Item item)
    {
        int count = 0;
        for(int i = 0; i < inventory.size(); i++)
        {
            ItemStack stack = inventory.getStack(i);
            if(stack.getItem() == item)
            {
                count += stack.getCount();
            }
        }
        return count;
    }

    /**
     * Gets the quantity of a specific item in the given inventory validating NBT data where applicable
     */
    public static int GetItemCount(Inventory inventory, ItemStack item)
    {
        int count = 0;
        for(int i = 0; i < inventory.size(); i++)
        {
            ItemStack stack = inventory.getStack(i);
            if(ItemMatches(stack, item))
            {
                count += stack.getCount();
            }
        }
        return count;
    }

    /**
     * Removes the quantity of a specific item in the given inventory
     * Ignores NBT data as none is given
     * @return Whether the full amount of items were successfully taken.
     */
    public static boolean RemoveItemCount(Inventory inventory, Item item, int count)
    {
        for(int i = 0; i < inventory.size(); i++)
        {
            ItemStack stack = inventory.getStack(i);
            if(stack.getItem() == item)
            {
                if(stack.getCount() > count)
                {
                    stack.decrement(count);
                    return true;
                }
                else
                {
                    count -= stack.getCount();
                    inventory.setStack(i, ItemStack.EMPTY);
                }
            }
        }
        return count <= 0;
    }

    /**
     * Removes the given item stack from the given inventory, validating nbt data.
     * @return Whether the full amount of items were successfully taken.
     */
    public static boolean RemoveItemCount(Inventory inventory, ItemStack item)
    {
        if(GetItemCount(inventory, item) < item.getCount())
            return false;
        int count = item.getCount();
        for(int i = 0; i < inventory.size(); i++)
        {
            ItemStack stack = inventory.getStack(i);
            if(ItemMatches(stack, item))
            {
                int amountToTake = MathUtil.clamp(count, 0, stack.getCount());
                count -= amountToTake;
                if(amountToTake == stack.getCount())
                    inventory.setStack(i, ItemStack.EMPTY);
                else
                    stack.decrement(amountToTake);
            }
        }
        return true;
    }

    /**
     * Removes the given item stack from the given inventory, validating nbt data.
     * @return Whether the full amount of items were successfully taken.
     */
    /*public static boolean RemoveItemCount(IItemHandler itemHandler, ItemStack item)
    {
        if(!CanExtractItem(itemHandler, item))
            return false;
        int amountToRemove = item.getCount();
        for(int i = 0; i < itemHandler.getSlots() && amountToRemove > 0; i++)
        {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if(ItemMatches(stack, item))
            {
                ItemStack removedStack = itemHandler.extractItem(i, amountToRemove, false);
                if(ItemMatches(removedStack, item))
                    amountToRemove -= removedStack.getCount();
                else //Put the item back
                    itemHandler.insertItem(i, removedStack, false);
            }
        }
        return true;
    }*/

    /**
     * Returns whether the given item stack can be successfully removed from the item handler.
     */
    /*public static boolean CanExtractItem(IItemHandler itemHandler, ItemStack item) {
        int amountToRemove = item.getCount();
        for(int i = 0; i < itemHandler.getSlots() && amountToRemove > 0; ++i)
        {
            if(ItemMatches(itemHandler.getStackInSlot(i), item))
            {
                ItemStack removedStack = itemHandler.extractItem(i, amountToRemove, true);
                if(ItemMatches(removedStack, item))
                    amountToRemove -= removedStack.getCount();
            }
        }
        return amountToRemove == 0;
    }*/

    /**
     * Returns the number of the given item stack that will fit in the container.
     */
    public static int GetItemSpace(Inventory container, ItemStack item)
    {
        return GetItemSpace(container, item, 0, container.size());
    }

    /**
     * Returns the number of the given item stack that will fit into the given portion of the container.
     */
    public static int GetItemSpace(Inventory container, ItemStack item, int startingIndex, int stopIndex)
    {
        int count = 0;
        for(int i = startingIndex; i < stopIndex && i < container.size(); ++i)
        {
            ItemStack stack = container.getStack(i);
            if(ItemMatches(item, stack))
                count += stack.getMaxCount() - stack.getCount();
            else if(stack.isEmpty())
                count += stack.getMaxCount();
        }
        return count;
    }

    public static int GetItemTagCount(Inventory inventory, Identifier itemTag, Item... blacklistItems)
    {
        List<Item> blacklist = Lists.newArrayList(blacklistItems);
        int count = 0;
        for(int i = 0; i < inventory.size(); i++)
        {
            ItemStack stack = inventory.getStack(i);
            if(ItemHasTag(stack, itemTag) && !blacklist.contains(stack.getItem()))
                count += stack.getCount();
        }
        return count;
    }

    public static boolean RemoveItemTagCount(Inventory inventory, Identifier itemTag, int count, Item... blacklistItems)
    {
        if(GetItemTagCount(inventory, itemTag, blacklistItems) < count)
            return false;
        List<Item> blacklist = Lists.newArrayList(blacklistItems);
        for(int i = 0; i < inventory.size(); i++)
        {
            ItemStack stack = inventory.getStack(i);
            if(ItemHasTag(stack, itemTag) && !blacklist.contains(stack.getItem()))
            {
                int amountToTake = MathUtil.clamp(count, 0, stack.getCount());
                count-= amountToTake;
                if(amountToTake == stack.getCount())
                    inventory.setStack(i, ItemStack.EMPTY);
                else
                    stack.decrement(amountToTake);
            }
        }
        return true;
    }

    /**
     * Places a given item stack in the inventory. Will not place if there's no room for every item.
     * @return Whether the stack was placed in the inventory. If false was returned nothing was placed.
     */
    public static boolean PutItemStack(Inventory inventory, ItemStack stack)
    {
        int amountToMerge = stack.getCount();
        Item mergeItem = stack.getItem();
        List<Pair<Integer,Integer>> mergeOrders = new ArrayList<>();
        //First pass, looking for stacks to add to
        for(int i = 0; i < inventory.size() && amountToMerge > 0; i++)
        {
            ItemStack inventoryStack = inventory.getStack(i);
            if(ItemMatches(stack, inventoryStack) && inventoryStack.getCount() != inventoryStack.getMaxCount())
            {
                //Calculate the amount that can fit in this slot
                int amountToPlace = MathUtil.clamp(amountToMerge, 0, inventoryStack.getMaxCount() - inventoryStack.getCount());
                //Define the orders
                mergeOrders.add(new Pair<Integer,Integer>(i,amountToPlace));
                //Update the pending merge count
                amountToMerge -= amountToPlace;
            }
        }
        //Second pass, checking for empty slots to place them in
        for(int i = 0; i < inventory.size() && amountToMerge > 0; i++)
        {
            ItemStack inventoryStack = inventory.getStack(i);
            if(inventoryStack.isEmpty())
            {
                //Calculate the amount that can fit in this slot
                int amountToPlace = MathUtil.clamp(amountToMerge, 0, stack.getMaxCount());
                //Define the orders
                mergeOrders.add(new Pair<Integer,Integer>(i,amountToPlace));
                //Update the pending merge count
                amountToMerge -= amountToPlace;
            }
        }
        //Confirm that all items have a placed to be placed
        if(amountToMerge > 0)
            return false;
        //Execute item placement/addition
        mergeOrders.forEach(order ->
        {
            ItemStack itemStack = inventory.getStack(order.getFirst());
            if(itemStack.isEmpty())
            {
                ItemStack newStack = new ItemStack(mergeItem, order.getSecond());
                if(stack.hasNbt())
                    newStack.setNbt(stack.getNbt().copy());
                inventory.setStack(order.getFirst(), newStack);
            }
            else
            {
                itemStack.setCount(itemStack.getCount() + order.getSecond());
            }
        });

        return true;
    }

    /**
     * Places as much of the given item stack as possible into the inventory.
     * @return The remaining items that were unable to be placed.
     */
    public static ItemStack TryPutItemStack(Inventory inventory, ItemStack stack)
    {
        int amountToMerge = stack.getCount();
        Item mergeItem = stack.getItem();
        //First pass, looking for stacks to add to
        for(int i = 0; i < inventory.size() && amountToMerge > 0; i++)
        {
            ItemStack inventoryStack = inventory.getStack(i);
            if(ItemMatches(stack, inventoryStack) && inventoryStack.getCount() < inventoryStack.getMaxCount())
            {
                //Calculate the amount that can fit in this slot
                int amountToPlace = MathUtil.clamp(amountToMerge, 0, inventoryStack.getMaxCount() - inventoryStack.getCount());
                //Add the items to the stack
                inventoryStack.increment(amountToPlace);
                //Update the pending merge count
                amountToMerge -= amountToPlace;
            }
        }
        //Second pass, checking for empty slots to place them in
        for(int i = 0; i < inventory.size() && amountToMerge > 0; i++)
        {
            ItemStack inventoryStack = inventory.getStack(i);
            if(inventoryStack.isEmpty())
            {
                //Calculate the amount that can fit in this slot
                int amountToPlace = MathUtil.clamp(amountToMerge, 0, stack.getMaxCount());
                //Place a new stack in the empty slot
                ItemStack newStack = new ItemStack(mergeItem, amountToPlace);
                if(stack.hasNbt())
                    newStack.setNbt(stack.getNbt().copy());
                inventory.setStack(i, newStack);
                //Update the pending merge count
                amountToMerge -= amountToPlace;
            }
        }

        if(amountToMerge > 0)
        {
            ItemStack leftovers = stack.copy();
            leftovers.setCount(amountToMerge);
            return leftovers;
        }
        return ItemStack.EMPTY;
    }

    /**
     * Determines whether there is enough room in the inventory to place the requested item stacks
     * @param inventory
     * @param stack
     * @return
     */
    public static boolean CanPutItemStack(Inventory inventory, ItemStack stack)
    {
        if(stack.isEmpty())
            return true;
        int amountToMerge = stack.getCount();
        //First pass, looking for stacks to add to
        for(int i = 0; i < inventory.size() && amountToMerge > 0; i++)
        {
            ItemStack inventoryStack = inventory.getStack(i);
            if(ItemMatches(stack, inventoryStack) && inventoryStack.getCount() != inventoryStack.getMaxCount())
            {
                //Calculate the amount that can fit in this slot
                int amountToPlace = MathUtil.clamp(amountToMerge, 0, inventoryStack.getMaxCount() - inventoryStack.getCount());
                //Update the pending merge count
                amountToMerge -= amountToPlace;
            }
        }
        //Second pass, checking for empty slots to place them in
        for(int i = 0; i < inventory.size() && amountToMerge > 0; i++)
        {
            ItemStack inventoryStack = inventory.getStack(i);
            if(inventoryStack.isEmpty())
            {
                //Calculate the amount that can fit in this slot
                int amountToPlace = MathUtil.clamp(amountToMerge, 0, stack.getMaxCount());
                //Update the pending merge count
                amountToMerge -= amountToPlace;
            }
        }
        return amountToMerge <= 0;
    }

    public static boolean CanPutItemStacks(Inventory inventory, ItemStack... stacks) { return CanPutItemStacks(inventory, Lists.newArrayList(stacks)); }

    public static boolean CanPutItemStacks(Inventory inventory, List<ItemStack> stacks)
    {
        Inventory copyInventory = new SimpleInventory(inventory.size());
        for(int i = 0; i < inventory.size(); ++i)
            copyInventory.setStack(i, inventory.getStack(i).copy());
        for(int i = 0; i < stacks.size(); ++i)
        {
            if(!InventoryUtil.PutItemStack(copyInventory, stacks.get(i)))
                return false;
        }
        return true;
    }

    /**
     * Merges item stacks of the same type together (e.g. 2 stacks of 32 cobblestone will become 1 stack of 64 cobblestone and an extra empty slot)
     * @param inventory
     */
    public static void MergeStacks(Inventory inventory)
    {
        for(int i = 0; i < inventory.size(); i++)
        {
            ItemStack thisStack = inventory.getStack(i);
            if(!thisStack.isEmpty())
            {
                int amountWanted = thisStack.getMaxCount() - thisStack.getCount();
                if(amountWanted > 0)
                {
                    //Steal from further stacks
                    for(int j = i + 1; j < inventory.size(); j++)
                    {
                        ItemStack nextStack = inventory.getStack(j);
                        if(!nextStack.isEmpty() && nextStack.getItem() == thisStack.getItem() && ItemStackHelper.TagEquals(thisStack, nextStack))
                        {
                            while(amountWanted > 0 && !nextStack.isEmpty())
                            {
                                nextStack.setCount(nextStack.getCount() - 1);
                                thisStack.setCount(thisStack.getCount() + 1);
                                amountWanted--;
                            }
                        }
                    }
                }
            }
        }
    }

    public static SimpleInventory loadAllItems(String key, NbtCompound compound, int inventorySize)
    {
        DefaultedList<ItemStack> tempInventory = DefaultedList.ofSize(inventorySize, ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(key, compound, tempInventory);
        return buildInventory(tempInventory);
    }

    public static void saveAllItems(String key, NbtCompound compound, Inventory inventory)
    {
        ItemStackHelper.saveAllItems(key, compound, buildList(inventory));
    }

    public static void dumpContents(World level, BlockPos pos, Inventory inventory)
    {
        if(level.isClient)
            return;
        for(int i = 0; i < inventory.size(); i++)
            dumpContents(level, pos, inventory.getStack(i));
    }

    public static void dumpContents(World level, BlockPos pos, List<ItemStack> inventory)
    {
        if(level.isClient)
            return;
        for(int i = 0; i < inventory.size(); i++)
            dumpContents(level, pos, inventory.get(i));
    }

    public static void dumpContents(World level, BlockPos pos, ItemStack stack)
    {
        if(level.isClient)
            return;
        if(!stack.isEmpty())
        {
            ItemEntity entity = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            level.spawnEntity(entity);
        }
    }

    public static List<ItemStack> combineQueryItems(ItemStack... items)
    {
        List<ItemStack> itemList = new ArrayList<>();
        for(ItemStack item : items)
        {
            boolean addNew = true;
            for(int i = 0; i < itemList.size() && addNew; ++i)
            {
                if(ItemMatches(item, itemList.get(i)))
                    itemList.get(i).increment(item.getCount());
            }
            if(addNew && !item.isEmpty())
                itemList.add(item.copy());
        }
        return itemList;
    }

    public static List<ItemStack> combineQueryItems(List<ItemStack> items)
    {
        List<ItemStack> itemList = new ArrayList<>();
        for(ItemStack item : items)
        {
            boolean addNew = true;
            for(int i = 0; i < itemList.size() && addNew; ++i)
            {
                if(ItemMatches(item, itemList.get(i)))
                    itemList.get(i).increment(item.getCount());
            }
            if(addNew && !item.isEmpty())
                itemList.add(item.copy());
        }
        return itemList;
    }

    /**
     * Determines whether the two item stacks are the same item/nbt. Ignores quantity of the items in the stack
     */
    public static boolean ItemMatches(ItemStack stack1, ItemStack stack2)
    {
        if(stack1.getItem() == stack2.getItem())
            return ItemStackHelper.TagEquals(stack1, stack2);
        return false;
    }

    public static boolean ItemHasTag(ItemStack item, Identifier tag) {
        for(TagKey<Item> itemTag : item.streamTags().toList())
        {
            if(itemTag.id().equals(tag))
                return true;
        }
        return false;
    }

    public static int safeGiveToPlayer(PlayerInventory inv, ItemStack stack) {

        int i = inv.getOccupiedSlotWithRoomForStack(stack);
        if (i == -1)
            i = inv.getEmptySlot();

        if(i >= 0)
        {
            ItemStack stackInSlot = inv.getStack(i);
            int putCount = Math.min(stack.getCount(), stackInSlot.isEmpty() ? stack.getMaxCount() : stackInSlot.getMaxCount() - stackInSlot.getCount());
            if(putCount > 0)
            {
                if(stackInSlot.isEmpty())
                {
                    stackInSlot = stack.copy();
                    stackInSlot.setCount(putCount);
                }
                else
                    stackInSlot.increment(putCount);
                stack.decrement(putCount);
                inv.setStack(i, stackInSlot);
                inv.markDirty();
            }
            return putCount;
        }
        else
            return 0;
    }

    public static void GiveToPlayer(PlayerEntity player, ItemStack stack) {
        if(stack.isEmpty())
            return;
        player.getInventory().offerOrDrop(stack);
    }
    public static void GiveToPlayer(PlayerEntity player, List<ItemStack> stacks) {
        for(ItemStack stack : stacks)
            GiveToPlayer(player, stack);
    }

}