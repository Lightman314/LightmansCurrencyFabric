package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces;

import io.github.lightman314.lightmanscurrency.common.blockentity.interfaces.IOwnableBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.interfaces.ICapabilityBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.interfaces.IOwnableBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public interface ITraderBlock extends IOwnableBlock, ICapabilityBlock {

    public BlockEntity getBlockEntity(BlockState state, WorldAccess level, BlockPos pos);

    @Override
    default boolean canBreak(PlayerEntity player, WorldAccess level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = this.getBlockEntity(state, level, pos);
        if(blockEntity instanceof IOwnableBlockEntity)
        {
            IOwnableBlockEntity ownableBlockEntity = (IOwnableBlockEntity)blockEntity;
            return ownableBlockEntity.canBreak(player);
        }
        return true;
    }

    default ItemStack getDropBlockItem(World level, BlockPos pos, BlockState state) { return state != null ? new ItemStack(state.getBlock()) : ItemStack.EMPTY; }

    public default BlockEntity getCapabilityBlockEntity(BlockState state, World level, BlockPos pos) { return this.getBlockEntity(state, level, pos); }

}
