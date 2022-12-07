package io.github.lightman314.lightmanscurrency.common.blocks.templates;

import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.util.LazyShapes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class RotatableBlock extends Block implements IRotatableBlock{
	
	private final Function<Direction,VoxelShape> shape;
	
	public RotatableBlock(Settings properties) { this(properties, LazyShapes.BOX_SHAPE_T); }
	
	public RotatableBlock(Settings properties, VoxelShape shape) { this(properties, LazyShapes.lazySingleShape(shape)); }
	
	public RotatableBlock(Settings properties, Function<Direction,VoxelShape> shape) { super(properties); this.shape = shape; }
	
	protected boolean transparent(BlockState state) { return true; }
	
	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
	
	@Override
	public BlockState getPlacementState(ItemPlacementContext context) { return super.getPlacementState(context).with(FACING, context.getPlayerLookDirection()); }
	
	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation) { return state.with(FACING, rotation.rotate(state.get(FACING))); }
	
	@Override
	protected void appendProperties(Builder<Block, BlockState> builder)
    {
        super.appendProperties(builder);
        builder.add(FACING);
    }
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext contect) { return shape.apply(this.getFacing(state)); }
	
	@Override
	public Direction getFacing(BlockState state) { return state.get(FACING); }
	
}