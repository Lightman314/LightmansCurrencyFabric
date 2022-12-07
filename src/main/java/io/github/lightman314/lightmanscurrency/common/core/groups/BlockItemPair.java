package io.github.lightman314.lightmanscurrency.common.core.groups;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BlockItemPair implements ItemConvertible, BlockConvertible {

	public final Block block;
	public final Item item;
	
	public BlockItemPair(Block block, Item item) { this.block = block; this.item = item; }

	@Override
	public Item asItem() { return this.item; }

	@Override
	public Iterable<Block> asBlock() { return Lists.newArrayList(this.block); }

	public static List<Block> asBlocks(Collection<BlockItemPair> blockItemPairs) {
		List<Block> blocks = new ArrayList<>();
		for(BlockItemPair pair : blockItemPairs)
			blocks.add(pair.block);
		return blocks;
	}

	public static List<Item> asItems(Collection<BlockItemPair> blockItemPairs) {
		List<Item> items = new ArrayList<>();
		for(BlockItemPair pair : blockItemPairs)
			items.add(pair.item);
		return items;
	}

}
