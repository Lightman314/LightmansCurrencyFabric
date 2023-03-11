package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

import java.util.List;

public interface IItemTraderBlock extends ITraderBlock {

    @Environment(EnvType.CLIENT)
    List<Vec3f> GetStackRenderPos(int tradeSlot, BlockState state, boolean isDoubleTrade);

    @Environment(EnvType.CLIENT)
    List<Quaternion> GetStackRenderRot(int tradeSlot, BlockState state);

    @Environment(EnvType.CLIENT)
    float GetStackRenderScale(int tradeSlot, BlockState state);

    @Environment(EnvType.CLIENT)
    int maxRenderIndex();

}
