package io.github.lightman314.lightmanscurrency.common.blocks.traderinterface.templates;


import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blockentity.TickableBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.traderinterface.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.interfaces.IOwnableBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.RotatableBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.util.TickerUtil;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionSaveData;
import io.github.lightman314.lightmanscurrency.common.items.TooltipItem;
import io.github.lightman314.lightmanscurrency.network.util.BlockEntityUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class TraderInterfaceBlock extends RotatableBlock implements BlockEntityProvider, IOwnableBlock {

    protected TraderInterfaceBlock(Settings properties) { super(properties); }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World level, BlockState state, BlockEntityType<T> type) { return TickerUtil.createTickerHelper(type, this.interfaceType(), TickableBlockEntity::tickHandler); }

    @Override
    public ActionResult onUse(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult result)
    {
        if(!level.isClient)
        {
            TraderInterfaceBlockEntity blockEntity = this.getBlockEntity(level, pos, state);
            if(blockEntity != null)
            {
                //Send update packet for safety, and open the menu
                BlockEntityUtil.sendUpdatePacket(blockEntity);
                blockEntity.openMenu(player);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void onPlaced(World level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
    {
        if(!level.isClient && placer instanceof PlayerEntity player)
        {
            TraderInterfaceBlockEntity blockEntity = this.getBlockEntity(level, pos, state);
            if(blockEntity != null)
            {
                blockEntity.initOwner(player);
            }
        }
    }

    @Override
    public void onBreak(World level, BlockPos pos, BlockState state, PlayerEntity player)
    {
        TraderInterfaceBlockEntity blockEntity = this.getBlockEntity(level, pos, state);
        if(blockEntity != null)
        {
            if(!blockEntity.isOwner(player))
                return;
            InventoryUtil.dumpContents(level, pos, blockEntity.getContents(level, pos, state, !player.isCreative()));
            blockEntity.flagAsRemovable();
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
            TraderInterfaceBlockEntity blockEntity = this.getBlockEntity(level, pos, state);
            if(blockEntity != null)
            {
                if(!blockEntity.allowRemoval())
                {
                    LightmansCurrency.LogError("Trader block at " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " was broken by illegal means!");
                    LightmansCurrency.LogError("Activating emergency eject protocol.");
                    EjectionData data = EjectionData.create(level, pos, state, blockEntity);
                    EjectionSaveData.HandleEjectionData(level, pos, data);
                    blockEntity.flagAsRemovable();
                    //Remove the rest of the multi-block structure.
                    try {
                        this.onInvalidRemoval(state, level, pos, blockEntity);
                    } catch(Throwable t) { t.printStackTrace(); }
                }
                else
                    LightmansCurrency.LogDebug("Trader block was broken by legal means!");
            }
        }

        super.onStateReplaced(state, level, pos, newState, flag);
    }

    protected abstract void onInvalidRemoval(BlockState state, World level, BlockPos pos, TraderInterfaceBlockEntity trader);

    @Override
    public boolean canBreak(PlayerEntity player, WorldAccess level, BlockPos pos, BlockState state) {
        TraderInterfaceBlockEntity be = this.getBlockEntity(level, pos, state);
        if(be == null)
            return true;
        return be.isOwner(player);
    }

    protected abstract BlockEntityType<?> interfaceType();

    protected final TraderInterfaceBlockEntity getBlockEntity(WorldAccess level, BlockPos pos, BlockState state) {
        BlockEntity be = level.getBlockEntity(pos);
        if(be instanceof TraderInterfaceBlockEntity)
            return (TraderInterfaceBlockEntity)be;
        return null;
    }

    protected Supplier<List<Text>> getItemTooltips() { return () -> new ArrayList<>(); }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView level, List<Text> tooltip, TooltipContext flag)
    {
        TooltipItem.addTooltip(tooltip, this.getItemTooltips());
        super.appendTooltip(stack, level, tooltip, flag);
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) { return true; }

    public ItemStack getDropBlockItem(BlockState state, TraderInterfaceBlockEntity traderInterface) { return new ItemStack(state.getBlock()); }

}