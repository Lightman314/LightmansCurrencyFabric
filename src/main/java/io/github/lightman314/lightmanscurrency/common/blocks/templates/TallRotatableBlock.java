package io.github.lightman314.lightmanscurrency.common.blocks.templates;

import java.util.function.BiFunction;

import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.ITallBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.util.LazyShapes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;


public class TallRotatableBlock extends RotatableBlock implements ITallBlock{

	public static final BooleanProperty ISBOTTOM = Properties.BOTTOM;
	private final BiFunction<Direction,Boolean,VoxelShape> shape;
	
	protected TallRotatableBlock(Settings properties) { this(properties, LazyShapes.TALL_BOX_SHAPE); }
	
	protected TallRotatableBlock(Settings properties, VoxelShape shape) { this(properties, LazyShapes.lazyTallSingleShape(shape)); }
	
	protected TallRotatableBlock(Settings properties, BiFunction<Direction,Boolean,VoxelShape> shape)
	{
		super(properties.pistonBehavior(PistonBehavior.BLOCK));
		this.shape = shape;
		this.setDefaultState(
			this.getDefaultState()
				.with(FACING, Direction.NORTH)
				.with(ISBOTTOM, true)
		);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context)  { return this.shape.apply(this.getFacing(state), this.getIsBottom(state)); }
	
	@Override
	protected void appendProperties(Builder<Block, BlockState> builder)
    {
		super.appendProperties(builder);
        builder.add(ISBOTTOM);
    }
	
	@Override
	public BlockState getPlacementState(ItemPlacementContext context) { return super.getPlacementState(context).with(ISBOTTOM,true); }
	
	@Override
	public void onPlaced(World level, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
	{
		if(level.getBlockState(pos.up()).getBlock() == Blocks.AIR)
			level.setBlockState(pos.up(), this.getDefaultState().with(ISBOTTOM, false).with(FACING, state.get(FACING)));
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
		
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neightborPos)
	{
		if((direction == Direction.UP && state.get(ISBOTTOM)) || (direction == Direction.DOWN && !state.get(ISBOTTOM)))
		{
			if(neighborState.isOf(this))
				return state;
			else
				return Blocks.AIR.getDefaultState();
		}
		return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neightborPos);
	}
	
	@Override
	public boolean getIsBottom(BlockState state) { return state.get(ISBOTTOM); }
	
}