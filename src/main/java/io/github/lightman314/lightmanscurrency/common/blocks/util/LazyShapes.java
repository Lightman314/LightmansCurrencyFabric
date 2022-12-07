package io.github.lightman314.lightmanscurrency.common.blocks.util;

import java.util.function.BiFunction;
import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IRotatableBlock;
import net.minecraft.block.Block;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.shape.VoxelShape;

public class LazyShapes {

	//Functions for directional only
	public static Function<Direction,VoxelShape> lazySingleShape(VoxelShape shape) { return (facing) -> shape; }
	public static Function<Direction,VoxelShape> lazyDirectionalShape(VoxelShape north, VoxelShape east, VoxelShape south, VoxelShape west) { return new LazyDirectionShapeHandler(north,east,south,west); }
	//BiFunctions for tall only
	public static BiFunction<Direction,Boolean,VoxelShape> lazyTallSingleShape(VoxelShape shape) { return (facing,isBottom) -> { if(isBottom) return shape; return moveDown(shape);}; }
	public static BiFunction<Direction,Boolean,VoxelShape> lazyTallDirectionalShape(VoxelShape north, VoxelShape east, VoxelShape south, VoxelShape west) { return new LazyDirectionTallShapeHandler(north,east,south,west); }
	//BiFunctions for wide only (Wide must interface with direction, so no lazySingleShape variant for it)
	public static BiFunction<Direction,Boolean,VoxelShape> lazyWideDirectionalShape(VoxelShape north, VoxelShape east, VoxelShape south, VoxelShape west) { return new LazyDirectionWideShapeHandler(north,east,south,west); }
	//TriFunctions for tall and wide (Wide must interface with direction, so no lazySingleShape variant for it)
	public static TriFunction<Direction,Boolean,Boolean,VoxelShape> lazyTallWideDirectionalShape(BiFunction<Direction,Boolean,VoxelShape> tallShape) { return new LazyDirectionTallWideShapeHandler(tallShape); }
	public static TriFunction<Direction,Boolean,Boolean,VoxelShape> lazyTallWideDirectionalShape(VoxelShape north, VoxelShape east, VoxelShape south, VoxelShape west) { return new LazyDirectionTallWideShapeHandler(north,east,south,west); }
	
	//Half Box
	public static final VoxelShape SHORT_BOX = Block.createCuboidShape(0d,0d,0d,16d,8d,16d);
	public static final VoxelShape SHORT_BOX_T = Block.createCuboidShape(0.01d,0d,0.01d,15.99d,8d,15.99d);
	public static final Function<Direction,VoxelShape> SHORT_BOX_SHAPE = lazySingleShape(SHORT_BOX);
	public static final Function<Direction,VoxelShape> SHORT_BOX_SHAPE_T = lazySingleShape(SHORT_BOX_T);
	//Full Box
	public static final VoxelShape BOX = Block.createCuboidShape(0d,0d,0d,16d,16d,16d);
	public static final VoxelShape BOX_T = Block.createCuboidShape(0.01d,0d,0.01d,15.99d,16d,15.99d);
	public static final Function<Direction,VoxelShape> BOX_SHAPE = lazySingleShape(BOX);
	public static final Function<Direction,VoxelShape> BOX_SHAPE_T = lazySingleShape(BOX_T);
	//Tall Box
	public static final VoxelShape TALL_BOX = Block.createCuboidShape(0d,0d,0d,16d,32d,16d);
	public static final VoxelShape TALL_BOX_T = Block.createCuboidShape(0.01d,0d,0.01d,15.99d,32d,15.99d);
	public static final BiFunction<Direction,Boolean,VoxelShape> TALL_BOX_SHAPE = lazyTallSingleShape(TALL_BOX);
	public static final BiFunction<Direction,Boolean,VoxelShape> TALL_BOX_SHAPE_T = lazyTallSingleShape(TALL_BOX_T);
	//Wide Box
	public static final VoxelShape WIDE_BOX_NORTH = Block.createCuboidShape(0d,0d,0d,32d,16d,16d);
	public static final VoxelShape WIDE_BOX_EAST = Block.createCuboidShape(0d,0d,0d,16d,16d,32d);
	public static final VoxelShape WIDE_BOX_SOUTH = Block.createCuboidShape(-16d,0d,0d,16d,16d,16d);
	public static final VoxelShape WIDE_BOX_WEST = Block.createCuboidShape(0d,0d,-16d,16d,16d,16d);
	public static final BiFunction<Direction,Boolean,VoxelShape> WIDE_BOX_SHAPE = lazyWideDirectionalShape(WIDE_BOX_NORTH,WIDE_BOX_EAST,WIDE_BOX_SOUTH,WIDE_BOX_WEST);
	//Tall & Wide Box
	public static final VoxelShape TALL_WIDE_BOX_NORTH = Block.createCuboidShape(0d,0d,0d,32d,32d,16d);
	public static final VoxelShape TALL_WIDE_BOX_EAST = Block.createCuboidShape(0d,0d,0d,16d,32d,32d);
	public static final VoxelShape TALL_WIDE_BOX_SOUTH = Block.createCuboidShape(-16d,0d,0d,16d,32d,16d);
	public static final VoxelShape TALL_WIDE_BOX_WEST = Block.createCuboidShape(0d,0d,-16d,16d,32d,16d);
	public static final VoxelShape TALL_WIDE_BOX_NORTH_T = Block.createCuboidShape(0.01d,0d,0.01d,31.99d,32d,15.99d);
	public static final VoxelShape TALL_WIDE_BOX_EAST_T = Block.createCuboidShape(0.01d,0d,0.01d,15.99d,32d,31.99d);
	public static final VoxelShape TALL_WIDE_BOX_SOUTH_T = Block.createCuboidShape(-15.99d,0d,0.01d,15.99d,32d,15.99d);
	public static final VoxelShape TALL_WIDE_BOX_WEST_T = Block.createCuboidShape(0.01d,0d,-15.99d,15.99d,32d,15.99d);
	public static final TriFunction<Direction,Boolean,Boolean,VoxelShape> TALL_WIDE_BOX_SHAPE = lazyTallWideDirectionalShape(TALL_WIDE_BOX_NORTH,TALL_WIDE_BOX_EAST,TALL_WIDE_BOX_SOUTH,TALL_WIDE_BOX_WEST);
	public static final TriFunction<Direction,Boolean,Boolean,VoxelShape> TALL_WIDE_BOX_SHAPE_T = lazyTallWideDirectionalShape(TALL_WIDE_BOX_NORTH_T,TALL_WIDE_BOX_EAST_T,TALL_WIDE_BOX_SOUTH_T,TALL_WIDE_BOX_WEST_T);
	
	public static VoxelShape moveDown(VoxelShape shape) { return shape.offset(0f, -1d, 0d); }
	
	protected static class LazyDirectionShapeHandler implements Function<Direction, VoxelShape>
	{
		private final VoxelShape north;
		private final VoxelShape east;
		private final VoxelShape south;
		private final VoxelShape west;
		
		public LazyDirectionShapeHandler(VoxelShape north, VoxelShape east, VoxelShape south, VoxelShape west)
		{
			this.north = north;
			this.east = east;
			this.south = south;
			this.west = west;
		}
		
		@Override
		public VoxelShape apply(Direction facing) {
			return switch (facing) {
				case EAST -> east;
				case SOUTH -> south;
				case WEST -> west;
				default -> north;
			};
		}
	}
	
	protected static class LazyDirectionTallShapeHandler implements BiFunction<Direction,Boolean,VoxelShape>
	{
		private final Function<Direction,VoxelShape> lazyShape;
		
		public LazyDirectionTallShapeHandler(VoxelShape north, VoxelShape east, VoxelShape south, VoxelShape west)
		{
			this.lazyShape = lazyDirectionalShape(north,east,south,west);
		}
		
		@Override
		public VoxelShape apply(Direction facing, Boolean isBottom)
		{
			VoxelShape shape = lazyShape.apply(facing);
			if(isBottom)
				return shape;
			else
				return moveDown(shape);
		}
	}
	
	protected static class LazyDirectionWideShapeHandler implements BiFunction<Direction,Boolean,VoxelShape>
	{
		private final Function<Direction,VoxelShape> lazyShape;
		
		public LazyDirectionWideShapeHandler(VoxelShape north, VoxelShape east, VoxelShape south, VoxelShape west)
		{
			this.lazyShape = lazyDirectionalShape(north,east,south,west);
		}
		
		@Override
		public VoxelShape apply(Direction facing, Boolean isLeft)
		{
			VoxelShape shape = lazyShape.apply(facing);
			if(isLeft)
				return shape;
			else
			{
				Vec3f offset = IRotatableBlock.getLeftVect(facing);
				return shape.offset(offset.getX(), offset.getY(), offset.getZ());
			}
		}
		
	}
	
	protected static class LazyDirectionTallWideShapeHandler implements TriFunction<Direction,Boolean,Boolean,VoxelShape>{
		
		private final BiFunction<Direction,Boolean,VoxelShape> lazyShape;
		
		public LazyDirectionTallWideShapeHandler(BiFunction<Direction,Boolean,VoxelShape> tallShape) {
			this.lazyShape = tallShape;
		}
		
		public LazyDirectionTallWideShapeHandler(VoxelShape north, VoxelShape east, VoxelShape south, VoxelShape west) {
			this.lazyShape = lazyTallDirectionalShape(north,east,south,west);
		}
		
		@Override
		public VoxelShape apply(Direction facing, Boolean isBottom, Boolean isLeft)
		{
			VoxelShape shape = lazyShape.apply(facing, isBottom);
			if(isLeft)
				return shape;
			else
			{
				Vec3f offset = IRotatableBlock.getLeftVect(facing);
				return shape.offset(offset.getX(), offset.getY(), offset.getZ());
			}
		}
		
	}
	
	public interface TriFunction<T,U,V,W> { W apply(T t, U u, V v); }
	
}