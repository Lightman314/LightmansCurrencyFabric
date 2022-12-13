package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.templates;

import io.github.lightman314.lightmanscurrency.common.blockentity.trader.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IWideBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.util.LazyShapes;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import java.util.function.BiFunction;

public abstract class TraderBlockTallWideRotatable extends TraderBlockTallRotatable implements IWideBlock {

    protected static final BooleanProperty ISLEFT = Properties.ATTACHED;
    private final LazyShapes.TriFunction<Direction,Boolean,Boolean, VoxelShape> shape;

    protected TraderBlockTallWideRotatable(Settings properties) { this(properties, LazyShapes.TALL_WIDE_BOX_SHAPE); }

    protected TraderBlockTallWideRotatable(Settings properties, VoxelShape north, VoxelShape east, VoxelShape south, VoxelShape west)
    {
        this(properties, LazyShapes.lazyTallWideDirectionalShape(north, east, south, west));
    }

    protected TraderBlockTallWideRotatable(Settings properties, BiFunction<Direction,Boolean,VoxelShape> tallShape)
    {
        this(properties, LazyShapes.lazyTallWideDirectionalShape(tallShape));
    }

    protected TraderBlockTallWideRotatable(Settings properties, LazyShapes.TriFunction<Direction,Boolean,Boolean,VoxelShape> shape)
    {
        super(properties);
        this.shape = shape;
        this.setDefaultState(
                this.getDefaultState()
                        .with(FACING, Direction.NORTH)
                        .with(ISBOTTOM, true)
                        .with(ISLEFT, true)
        );
    }

    @Override
    protected boolean shouldMakeTrader(BlockState state) { return this.getIsBottom(state) && this.getIsLeft(state); }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context)  { return this.shape.apply(this.getFacing(state), this.getIsBottom(state), this.getIsLeft(state)); }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
    {
        super.appendProperties(builder);
        builder.add(ISLEFT);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) { return super.getPlacementState(context).with(ISLEFT,true); }

    @Override
    public void onPlaced(World level, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
    {
        //Attempt to place the other three blocks
        BlockPos rightPos = IRotatableBlock.getRightPos(pos, this.getFacing(state));
        if(this.getReplacable(level, rightPos, state, player, stack) && this.getReplacable(level, rightPos.up(), state, player, stack) && this.getReplacable(level, pos.up(), state, player, stack))
        {
            level.setBlockState(pos.up(), this.getDefaultState().with(ISBOTTOM, false).with(FACING, state.get(FACING)).with(ISLEFT, true));
            level.setBlockState(rightPos, this.getDefaultState().with(ISBOTTOM, true).with(FACING, state.get(FACING)).with(ISLEFT, false));
            level.setBlockState(rightPos.up(), this.getDefaultState().with(ISBOTTOM, false).with(FACING, state.get(FACING)).with(ISLEFT, false));
        }
        else
        {
            //Failed placing the top block. Abort placement
            level.setBlockState(pos, Blocks.AIR.getDefaultState(), 35);
            if(player instanceof PlayerEntity)
            {
                ItemStack giveStack = stack.copy();
                giveStack.setCount(1);
                ((PlayerEntity)player).getInventory().insertStack(giveStack);
            }
        }

        this.onPlacedBase(level, pos, state, player, stack);

    }

    @Override
    public void onBreak(World level, BlockPos pos, BlockState state, PlayerEntity player)
    {

        //Run base functionality first to prevent the removal of the block containing the block entity
        this.onBreakBase(level, pos, state, player);

        BlockEntity blockEntity = this.getBlockEntity(state, level, pos);
        if(blockEntity instanceof TraderBlockEntity<?> trader)
        {
            if(!trader.canBreak(player))
                return;
        }

        if(this.getIsBottom(state))
        {
            setAir(level, pos.up(), player);
            BlockPos otherPos = this.getOtherSide(pos, state, this.getFacing(state));
            setAir(level, otherPos, player);
            setAir(level, otherPos.up(), player);
        }
        else
        {
            setAir(level, pos.down(), player);
            BlockPos otherPos = this.getOtherSide(pos, state, this.getFacing(state));
            setAir(level, otherPos, player);
            setAir(level, otherPos.down(), player);
        }

    }

    @Override
    protected void onInvalidRemoval(BlockState state, World level, BlockPos pos, TraderData trader) {
        super.onInvalidRemoval(state, level, pos, trader);
        BlockPos otherPos = this.getOtherSide(pos, state, this.getFacing(state));
        setAir(level, otherPos, null);
        setAir(level, this.getOtherHeight(otherPos, state), null);
    }

    @Override
    public BlockEntity getBlockEntity(BlockState state, WorldAccess level, BlockPos pos)
    {
        if(level == null)
            return null;
        BlockPos getPos = pos;
        if(this.getIsRight(state))
            getPos = IRotatableBlock.getLeftPos(getPos, this.getFacing(state));
        if(this.getIsTop(state))
            return level.getBlockEntity(getPos.down());
        return level.getBlockEntity(getPos);
    }

    @Override
    public boolean getIsLeft(BlockState state) { return state.get(ISLEFT); }

}