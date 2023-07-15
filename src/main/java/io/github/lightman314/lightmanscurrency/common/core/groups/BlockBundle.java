package io.github.lightman314.lightmanscurrency.common.core.groups;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import java.util.Collection;
import java.util.Comparator;

public class BlockBundle<L> extends ObjectBundle<BlockItemPair,L> implements BlockConvertible {

    @Override
    public Iterable<Block> asBlock() { return BlockItemPair.asBlocks(this.getAll()); }

    public Collection<ItemStack> asItemStack(Comparator<L> sorter) { return this.getAllSorted(sorter).stream().map(ItemStack::new).toList(); }

}
