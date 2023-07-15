package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.templates;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blockentity.SidedStorageExtensionBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.TickableBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces.ITraderBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.util.LazyShapes;
import io.github.lightman314.lightmanscurrency.common.blocks.util.TickerUtil;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionSaveData;
import io.github.lightman314.lightmanscurrency.common.items.TooltipItem;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public abstract class TraderBlockBase extends Block implements ITraderBlock, BlockEntityProvider {

    private final VoxelShape shape;

    public TraderBlockBase(Settings properties)  { this(properties, LazyShapes.BOX); }

    public TraderBlockBase(Settings properties, VoxelShape shape)  { super(properties.pistonBehavior(PistonBehavior.BLOCK)); this.shape = shape != null ? shape : LazyShapes.BOX; }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context)   {return this.shape; }

    protected boolean shouldMakeTrader(BlockState state) { return true; }
    protected abstract BlockEntity makeTrader(BlockPos pos, BlockState state);
    protected BlockEntity makeDummy(BlockPos pos, BlockState state) { return new SidedStorageExtensionBlockEntity(pos, state); }
    protected abstract BlockEntityType<?> traderType();

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World level, BlockState state, BlockEntityType<T> type)
    {
        BlockEntityTicker<T> ticker = TickerUtil.createTickerHelper(type, this.traderType(), TickableBlockEntity::tickHandler);
        if(ticker != null)
            return ticker;
        return null;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
    {
        if(this.shouldMakeTrader(state))
            return this.makeTrader(pos, state);
        return this.makeDummy(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult result)
    {
        if(!level.isClient)
        {
            BlockEntity blockEntity = this.getBlockEntity(state, level, pos);
            if(blockEntity instanceof TraderBlockEntity<?> traderSource)
            {
                TraderData trader = traderSource.getTraderData();
                if(trader == null)
                {
                    LightmansCurrency.LogWarning("Trader Data for block at " + pos.getX() + "," + pos.getY() + "," + pos.getZ() + " had to be re-initialized on interaction.");
                    player.sendMessage(Text.translatable("trader.warning.reinitialized").formatted(Formatting.RED));
                    traderSource.initialize(player, ItemStack.EMPTY);
                    trader = traderSource.getTraderData();
                }
                if(trader != null) //Open the trader menu
                {
                    if(trader.shouldAlwaysShowOnTerminal())
                        trader.openStorageMenu(player);
                    else
                        trader.openTraderMenu(player);
                }
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void onPlaced(World level, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
    {
        this.onPlacedBase(level, pos, state, player, stack);
    }

    public final void onPlacedBase(World level, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack)
    {
        if(!level.isClient && entity instanceof PlayerEntity player)
        {
            BlockEntity blockEntity = this.getBlockEntity(state, level, pos);
            if(blockEntity instanceof TraderBlockEntity<?> traderSource)
            {
                traderSource.initialize(player, stack);
            }
            else
            {
                LightmansCurrency.LogError("Trader Block returned block entity of type '" + (blockEntity == null ? "null" : blockEntity.getClass().getName()) + "' when placing the block.");
            }
        }
    }

    @Override
    public void onBreak(World level, BlockPos pos, BlockState state, PlayerEntity player)  { this.onBreakBase(level, pos, state, player); }

    public final void onBreakBase(World level, BlockPos pos, BlockState state, PlayerEntity player)
    {
        BlockEntity blockEntity = this.getBlockEntity(state, level, pos);
        if(blockEntity instanceof TraderBlockEntity<?>)
        {
            TraderBlockEntity<?> traderSource = (TraderBlockEntity<?>)blockEntity;
            if(!traderSource.canBreak(player))
                return;
            else
            {
                traderSource.flagAsLegitBreak();
                TraderData trader = traderSource.getTraderData();
                if(trader != null)
                    InventoryUtil.dumpContents(level, pos, trader.getContents(level, pos, state, !player.isCreative()));
                //Trigger on-break code to delete the trader from the trader data.
                traderSource.deleteTrader();
            }
        }
        else
        {
            LightmansCurrency.LogError("Trader Block returned block entity of type '" + (blockEntity == null ? "null" : blockEntity.getClass().getName()) + "' when destroying the block.");
        }
        super.onBreak(level, pos, state, player);
    }


    @Override
    @SuppressWarnings("deprecation")
    public void onStateReplaced(BlockState state, World level, BlockPos pos, BlockState newState, boolean flag) {

        //Ignore if the block is the same.
        if(state.getBlock() == newState.getBlock())
            return;

        if(!level.isClient)
        {
            BlockEntity blockEntity = this.getBlockEntity(state, level, pos);
            if(blockEntity instanceof TraderBlockEntity<?> traderSource)
            {
                if(!traderSource.legitimateBreak())
                {
                    traderSource.flagAsLegitBreak();
                    TraderData trader = traderSource.getTraderData();
                    if(trader != null)
                    {
                        LightmansCurrency.LogError("Trader block at " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " was broken by illegal means!");
                        LightmansCurrency.LogError("Activating emergency eject protocol.");
                        EjectionData data = EjectionData.create(level, pos, state, trader);
                        EjectionSaveData.HandleEjectionData(level, pos, data);
                        TraderSaveData.DeleteTrader(trader.getID());
                    }
                    //Remove the rest of the multi-block structure.
                    try {
                        this.onInvalidRemoval(state, level, pos, trader);
                    } catch(Throwable t) { t.printStackTrace(); }
                    //Trigger on-break code to delete the trader from the trader data.
                    traderSource.deleteTrader();
                }
                else
                    LightmansCurrency.LogDebug("Trader block was broken by legal means!");

                //Flag the block as broken, so that the trader gets deleted.
                traderSource.deleteTrader();
            }
        }

        super.onStateReplaced(state, level, pos, newState, flag);
    }

    protected abstract void onInvalidRemoval(BlockState state, World level, BlockPos pos, TraderData trader);

    public boolean canEntityDestroy(BlockState state, BlockView level, BlockPos pos, Entity entity) { return false; }

    @Override
    public BlockEntity getBlockEntity(BlockState state, WorldAccess level, BlockPos pos) { return level == null ? null : level.getBlockEntity(pos); }

    protected Supplier<List<Text>> getItemTooltips() { return () -> new ArrayList<>(); }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView level, List<Text> tooltip, TooltipContext flagIn)
    {
        TooltipItem.addTooltip(tooltip, this.getItemTooltips());
        super.appendTooltip(stack, level, tooltip, flagIn);
    }

}