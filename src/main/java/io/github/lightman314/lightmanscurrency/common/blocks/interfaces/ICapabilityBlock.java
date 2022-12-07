package io.github.lightman314.lightmanscurrency.common.blocks.interfaces;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ICapabilityBlock {

    public BlockEntity getCapabilityBlockEntity(BlockState state, World level, BlockPos pos);

}
