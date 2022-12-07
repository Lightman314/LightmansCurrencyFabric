package io.github.lightman314.lightmanscurrency.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.collection.DefaultedList;

public class ItemStackHelper {

    public static NbtCompound saveAllItems(String key, NbtCompound tag, DefaultedList<ItemStack> list)
    {
        NbtList listTag = new NbtList();
        for(int i = 0; i < list.size(); ++i)
        {
            ItemStack stack = list.get(i);
            if(!stack.isEmpty())
            {
                NbtCompound itemCompound = new NbtCompound();
                itemCompound.putByte("Slot", (byte)i);
                stack.writeNbt(itemCompound);
                listTag.add(itemCompound);
            }
        }
        tag.put(key, listTag);
        return tag;
    }

    public static void loadAllItems(String key, NbtCompound tag, DefaultedList<ItemStack> list)
    {
        NbtList listTag = tag.getList(key, NbtElement.COMPOUND_TYPE);
        for(int i = 0; i < listTag.size(); i++)
        {
            NbtCompound slotCompound = listTag.getCompound(i);
            int index = slotCompound.getByte("Slot") & 255;
            if(index < list.size())
            {
                list.set(index, ItemStack.fromNbt(slotCompound));
            }
        }
    }

    public static boolean TagEquals(ItemStack stack1, ItemStack stack2)
    {
        return stack1.hasNbt() == stack2.hasNbt() && (!stack1.hasNbt() && !stack2.hasNbt() || stack1.getNbt().equals(stack2.getNbt()));
    }

}