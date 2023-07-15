package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public interface IItemTraderBlock extends ITraderBlock {

    @Environment(EnvType.CLIENT)
    List<Vector3f> GetStackRenderPos(int tradeSlot, BlockState state, boolean isDoubleTrade);

    @Environment(EnvType.CLIENT)
    List<Quaternionf> GetStackRenderRot(int tradeSlot, BlockState state);

    @Environment(EnvType.CLIENT)
    float GetStackRenderScale(int tradeSlot, BlockState state);

    @Environment(EnvType.CLIENT)
    int maxRenderIndex();

}
