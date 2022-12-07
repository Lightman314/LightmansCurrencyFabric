package io.github.lightman314.lightmanscurrency.common.blocks.util;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

public class TickerUtil {

    @SuppressWarnings("unchecked")
    @Nullable
    public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<?> type1, BlockEntityType<?> type2, BlockEntityTicker<? super E> ticker) { return type1 == type2 ? (BlockEntityTicker<A>)ticker : null; }

}