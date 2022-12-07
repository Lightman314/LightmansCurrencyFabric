package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.blockentity.trader.FreezerTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces.IItemTraderBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.templates.TraderBlockTallRotatable;
import io.github.lightman314.lightmanscurrency.common.blocks.util.LazyShapes;
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
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.shape.VoxelShape;

public class FreezerBlock extends TraderBlockTallRotatable implements IItemTraderBlock {

    public static final int TRADECOUNT = 8;

    public static final VoxelShape SHAPE_SOUTH = createCuboidShape(0d, 0d, 3d, 16d, 32d, 16d);
    public static final VoxelShape SHAPE_NORTH = createCuboidShape(0d,0d,0d,16d,32d,13d);
    public static final VoxelShape SHAPE_EAST = createCuboidShape(3d,0d,0d,16d,32d,16d);
    public static final VoxelShape SHAPE_WEST = createCuboidShape(0d,0d,0d,13d,32d,16d);

    public FreezerBlock(Settings properties) { super(properties, LazyShapes.lazyTallDirectionalShape(SHAPE_NORTH, SHAPE_EAST, SHAPE_SOUTH, SHAPE_WEST)); }

    @Override
    public BlockEntity makeTrader(BlockPos pos, BlockState state) { return new FreezerTraderBlockEntity(pos, state, TRADECOUNT); }

    @Override
    public BlockEntityType<?> traderType() { return ModBlockEntities.FREEZER_TRADER; }

    @Override
    @Environment(EnvType.CLIENT)
    public List<Vec3f> GetStackRenderPos(int tradeSlot, BlockState state, boolean isDoubleTrade) {
        //Get facing
        Direction facing = this.getFacing(state);
        //Define directions for easy positional handling
        Vec3f forward = IRotatableBlock.getForwardVect(facing);
        Vec3f right = IRotatableBlock.getRightVect(facing);
        Vec3f up = Vec3f.POSITIVE_Y;
        Vec3f offset = IRotatableBlock.getOffsetVect(facing);

        Vec3f forwardOffset = MathUtil.VectorMult(forward, 6f/16f);

        Vec3f firstPosition = null;

        if(tradeSlot == 0)
        {
            Vec3f rightOffset = MathUtil.VectorMult(right, 5f/16f);
            Vec3f vertOffset = MathUtil.VectorMult(up, 28f/16f);
            firstPosition = MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
        }
        else if(tradeSlot == 1)
        {
            Vec3f rightOffset = MathUtil.VectorMult(right, 11f/16f);
            Vec3f vertOffset = MathUtil.VectorMult(up, 28f/16f);
            firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
        }
        else if(tradeSlot == 2)
        {
            Vec3f rightOffset = MathUtil.VectorMult(right, 5f/16f);
            Vec3f vertOffset = MathUtil.VectorMult(up, 21f/16f);
            firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
        }
        else if(tradeSlot == 3)
        {
            Vec3f rightOffset = MathUtil.VectorMult(right, 11f/16f);
            Vec3f vertOffset = MathUtil.VectorMult(up, 21f/16f);
            firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
        }
        else if(tradeSlot == 4)
        {
            Vec3f rightOffset = MathUtil.VectorMult(right, 5f/16f);
            Vec3f vertOffset = MathUtil.VectorMult(up, 14f/16f);
            firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
        }
        else if(tradeSlot == 5)
        {
            Vec3f rightOffset = MathUtil.VectorMult(right, 11f/16f);
            Vec3f vertOffset = MathUtil.VectorMult(up, 14f/16f);
            firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
        }
        else if(tradeSlot == 6)
        {
            Vec3f rightOffset = MathUtil.VectorMult(right, 5f/16f);
            Vec3f vertOffset = MathUtil.VectorMult(up, 7f/16f);
            firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
        }
        else if(tradeSlot == 7)
        {
            Vec3f rightOffset = MathUtil.VectorMult(right, 11f/16f);
            Vec3f vertOffset = MathUtil.VectorMult(up, 7f/16f);
            firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
        }

        List<Vec3f> posList = new ArrayList<>(3);
        if(firstPosition != null)
        {
            posList.add(firstPosition);
            for(float distance = 3.2f; distance < 7; distance += 3.2f)
            {
                posList.add(MathUtil.VectorAdd(firstPosition, MathUtil.VectorMult(forward, distance/16F)));
            }
        }
        else
        {
            posList.add(new Vec3f(0F, 1f, 0F));
        }
        return posList;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public List<Quaternion> GetStackRenderRot(int tradeSlot, BlockState state)
    {
        List<Quaternion> rotation = new ArrayList<>();
        int facing = this.getFacing(state).getHorizontal();
        rotation.add(Vec3f.POSITIVE_Y.getDegreesQuaternion(facing * -90f));
        return rotation;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public float GetStackRenderScale(int tradeSlot, BlockState state){ return 0.4f; }

    @Override
    @Environment(EnvType.CLIENT)
    public int maxRenderIndex() { return TRADECOUNT; }

    @Override
    protected Supplier<List<Text>> getItemTooltips() { return LCTooltips.ITEM_TRADER; }

}