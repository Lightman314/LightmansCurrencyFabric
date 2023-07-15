package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.templates;

import io.github.lightman314.lightmanscurrency.common.blockentity.trader.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.ITallBlock;
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
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;

import java.util.function.BiFunction;

public abstract class TraderBlockTallRotatable extends TraderBlockRotatable implements ITallBlock {

    protected static final BooleanProperty ISBOTTOM = Properties.BOTTOM;
    private final BiFunction<Direction,Boolean, VoxelShape> shape;

    protected TraderBlockTallRotatable(Settings properties) {this(properties, LazyShapes.TALL_BOX_SHAPE);}

    protected TraderBlockTallRotatable(Settings properties, VoxelShape shape) { this(properties, LazyShapes.lazyTallSingleShape(shape)); }

    protected TraderBlockTallRotatable(Settings properties, BiFunction<Direction,Boolean,VoxelShape> shape)
    {
        super(properties);
        this.shape = shape;
        this.setDefaultState(
                this.getDefaultState()
                        .with(FACING, Direction.NORTH)
                        .with(ISBOTTOM, true)
        );
    }

    @Override
    protected boolean shouldMakeTrader(BlockState state) { return this.getIsBottom(state); }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context)  { return this.shape.apply(this.getFacing(state), this.getIsBottom(state)); }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
    {
        super.appendProperties(builder);
        builder.add(ISBOTTOM);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) { return super.getPlacementState(context).with(ISBOTTOM,true); }

    @Override
    public void onPlaced(World level, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
    {
        if(this.getReplacable(level, pos.up(), state, player, stack))
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

        this.onPlacedBase(level, pos, state, player, stack);

    }

    public boolean getReplacable(World level, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack) {
        if(player instanceof PlayerEntity)
        {
            ItemPlacementContext context = new ItemPlacementContext(level, (PlayerEntity)player, Hand.MAIN_HAND, stack, new BlockHitResult(Vec3d.ZERO, Direction.UP, pos, true));
            return level.getBlockState(pos).canReplace(context);
        }
        else
        {
            return level.getBlockState(pos).getBlock() == Blocks.AIR;
        }
    }

    @Override
    public void onBreak(World level, BlockPos pos, BlockState state, PlayerEntity player)
    {

        //Run base functionality first to prevent the removal of the block containing the block entity
        this.onBreakBase(level, pos, state, player);

        BlockEntity blockEntity = this.getBlockEntity(state, level, pos);
        if(blockEntity instanceof TraderBlockEntity<?>)
        {
            TraderBlockEntity<?> trader = (TraderBlockEntity<?>)blockEntity;
            if(!trader.canBreak(player))
                return;
        }

        //Destroy the other half of the Tall Block
        setAir(level, this.getOtherHeight(pos, state), player);

    }

    @Override
    protected void onInvalidRemoval(BlockState state, World level, BlockPos pos, TraderData trader) {
        super.onInvalidRemoval(state, level, pos, trader);
        //Destroy the other half of the Tall Block
        setAir(level, this.getOtherHeight(pos, state), null);
    }

    protected final void setAir(World level, BlockPos pos, PlayerEntity player)
    {
        BlockState state = level.getBlockState(pos);
        if(state.getBlock() == this)
        {
            level.setBlockState(pos, Blocks.AIR.getDefaultState(), 35);
            if(player != null)
                level.emitGameEvent(player, GameEvent.BLOCK_DESTROY, pos);
        }
    }

    @Override
    public BlockEntity getBlockEntity(BlockState state, WorldAccess level, BlockPos pos)
    {
        if(level == null)
            return null;
        if(this.getIsTop(state))
            return level.getBlockEntity(pos.down());
        return level.getBlockEntity(pos);
    }

    @Override
    public boolean getIsBottom(BlockState state) { return state.get(ISBOTTOM); }

}