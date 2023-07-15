package io.github.lightman314.lightmanscurrency.common.blocks;

import net.minecraft.registry.tag.FluidTags;
import org.jetbrains.annotations.Nullable;

import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.util.LazyShapes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;


public class CoinpileBlock extends CoinBlock implements IRotatableBlock, Waterloggable{
	
	private final VoxelShape shape;
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	protected static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
	
	public CoinpileBlock(Settings properties, ItemConvertible coinItem)
	{
		this(properties, coinItem, LazyShapes.SHORT_BOX);
	}
	
	public CoinpileBlock(Settings properties, ItemConvertible coinItem, VoxelShape shape)
	{
		super(properties, coinItem);
		this.shape = shape != null ? shape : LazyShapes.SHORT_BOX;
		this.setDefaultState(this.getDefaultState().with(WATERLOGGED, false));
	}
	
	@Override
	protected int getCoinCount() { return 9; }
	
	@Nullable
	@Override
	public BlockState getPlacementState(ItemPlacementContext context) {
		BlockPos blockpos = context.getBlockPos();
		FluidState fluidstate = context.getWorld().getFluidState(blockpos);
		return super.getPlacementState(context).with(FACING, context.getHorizontalPlayerFacing()).with(WATERLOGGED, fluidstate.isOf(Fluids.WATER));
	}
	
	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation)
	{
		return state.with(FACING, rotation.rotate(state.get(FACING)));
	}
	
	@Override
    protected void appendProperties(Builder<Block, BlockState> builder)
    {
		super.appendProperties(builder);
        builder.add(FACING);
        builder.add(WATERLOGGED);
    }
	
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neightborPos) {
		if (state.get(WATERLOGGED)) {
			world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}
		return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neightborPos);
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext contect) { return shape; }
	
	@Override
	public Direction getFacing(BlockState state) { return state.get(FACING); }
	
	@SuppressWarnings("deprecation")
	@Override
	public FluidState getFluidState(BlockState state) { return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state); }
	
	@Override
	public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
		if(type == NavigationType.WATER)
			return world.getFluidState(pos).isIn(FluidTags.WATER);
		return false;
	}
	
}