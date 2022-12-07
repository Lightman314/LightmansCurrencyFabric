package io.github.lightman314.lightmanscurrency.common.core.groups;

import net.minecraft.block.Block;

public class BlockBundle<L> extends ObjectBundle<BlockItemPair,L> implements BlockConvertible {

    @Override
    public Iterable<Block> asBlock() { return BlockItemPair.asBlocks(this.getAll()); }
}
