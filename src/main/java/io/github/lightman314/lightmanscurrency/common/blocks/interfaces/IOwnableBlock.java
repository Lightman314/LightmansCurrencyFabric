package io.github.lightman314.lightmanscurrency.common.blocks.interfaces;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

public interface IOwnableBlock {

    public boolean canBreak(PlayerEntity player, WorldAccess level, BlockPos pos, BlockState state);

}
