package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces.IItemTraderBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.templates.TraderBlockTallWideRotatable;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.joml.Quaternionf;
import org.joml.Vector3f;


public class VendingMachineLargeBlock extends TraderBlockTallWideRotatable implements IItemTraderBlock {

    public static final int TRADECOUNT = 12;

    public VendingMachineLargeBlock(Settings properties) { super(properties); }

    @Override
    public BlockEntity makeTrader(BlockPos pos, BlockState state) { return new ItemTraderBlockEntity(pos, state, TRADECOUNT); }

    @Override
    public BlockEntityType<?> traderType() { return ModBlockEntities.ITEM_TRADER; }

    @Override
    @Environment(EnvType.CLIENT)
    public List<Vector3f> GetStackRenderPos(int tradeSlot, BlockState state, boolean isDoubleTrade) {
        //Get facing
        Direction facing = this.getFacing(state);
        //Define directions for easy positional handling
        Vector3f forward = IRotatableBlock.getForwardVect(facing);
        Vector3f right = IRotatableBlock.getRightVect(facing);
        Vector3f up = MathUtil.YP();
        Vector3f offset = IRotatableBlock.getOffsetVect(facing);

        Vector3f forwardOffset = MathUtil.VectorMult(forward, 6f/16f);
        Vector3f firstPosition = null;

        if(tradeSlot == 0)
        {
            Vector3f rightOffset = MathUtil.VectorMult(right, 3.5f/16f);
            Vector3f vertOffset = MathUtil.VectorMult(up, 27f/16f);
            firstPosition = MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
        }
        else if(tradeSlot == 1)
        {
            Vector3f rightOffset = MathUtil.VectorMult(right, 10.5f/16f);
            Vector3f vertOffset = MathUtil.VectorMult(up, 27f/16f);
            firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
        }
        else if(tradeSlot == 2)
        {
            Vector3f rightOffset = MathUtil.VectorMult(right, 17.5f/16f);
            Vector3f vertOffset = MathUtil.VectorMult(up, 27f/16f);
            firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
        }
        else if(tradeSlot == 3)
        {
            Vector3f rightOffset = MathUtil.VectorMult(right, 24.5f/16f);
            Vector3f vertOffset = MathUtil.VectorMult(up, 27f/16f);
            firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
        }
        else if(tradeSlot == 4)
        {
            Vector3f rightOffset = MathUtil.VectorMult(right, 3.5f/16f);
            Vector3f vertOffset = MathUtil.VectorMult(up, 20f/16f);
            firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
        }
        else if(tradeSlot == 5)
        {
            Vector3f rightOffset = MathUtil.VectorMult(right, 10.5f/16f);
            Vector3f vertOffset = MathUtil.VectorMult(up, 20f/16f);
            firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
        }
        else if(tradeSlot == 6)
        {
            Vector3f rightOffset = MathUtil.VectorMult(right, 17.5f/16f);
            Vector3f vertOffset = MathUtil.VectorMult(up, 20f/16f);
            firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
        }
        else if(tradeSlot == 7)
        {
            Vector3f rightOffset = MathUtil.VectorMult(right, 24.5f/16f);
            Vector3f vertOffset = MathUtil.VectorMult(up, 20f/16f);
            firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
        }
        else if(tradeSlot == 8)
        {
            Vector3f rightOffset = MathUtil.VectorMult(right, 3.5f/16f);
            Vector3f vertOffset = MathUtil.VectorMult(up, 13f/16f);
            firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
        }
        else if(tradeSlot == 9)
        {
            Vector3f rightOffset = MathUtil.VectorMult(right, 10.5f/16f);
            Vector3f vertOffset = MathUtil.VectorMult(up, 13f/16f);
            firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
        }
        else if(tradeSlot == 10)
        {
            Vector3f rightOffset = MathUtil.VectorMult(right, 17.5f/16f);
            Vector3f vertOffset = MathUtil.VectorMult(up, 13f/16f);
            firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
        }
        else if(tradeSlot == 11)
        {
            Vector3f rightOffset = MathUtil.VectorMult(right, 24.5f/16f);
            Vector3f vertOffset = MathUtil.VectorMult(up, 13f/16f);
            firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
        }

        List<Vector3f> posList = new ArrayList<>(3);
        if(firstPosition != null)
        {
            posList.add(firstPosition);
            for(float distance = 3.2f; distance < 7; distance += 3.2f)
                posList.add(MathUtil.VectorAdd(firstPosition, MathUtil.VectorMult(forward, distance/16F)));
        }
        else
        {
            posList.add(new Vector3f(0F, 1f, 0F));
        }
        return posList;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public List<Quaternionf> GetStackRenderRot(int tradeSlot, BlockState state)
    {
        List<Quaternionf> rotation = new ArrayList<>();
        int facing = this.getFacing(state).getHorizontal();
        rotation.add(MathUtil.getRotationDegrees(facing * -90f));
        return rotation;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public float GetStackRenderScale(int tradeSlot, BlockState state){ return 0.3f; }

    @Override
    @Environment(EnvType.CLIENT)
    public int maxRenderIndex() { return TRADECOUNT; }

    @Override
    protected Supplier<List<Text>> getItemTooltips() { return LCTooltips.ITEM_TRADER; }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
        if(state.get(ISBOTTOM))
            return VoxelShapes.empty();
        return super.getCullingShape(state, world, pos);
    }

}