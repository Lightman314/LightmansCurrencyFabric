package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks;

import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ArmorDisplayTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces.IItemTraderBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.templates.TraderBlockTallRotatable;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ArmorDisplayBlock extends TraderBlockTallRotatable {

    public static final int TRADECOUNT = 4;

    public ArmorDisplayBlock(Settings properties) { super(properties); }

    @Override
    public BlockEntity makeTrader(BlockPos pos, BlockState state) {
        ArmorDisplayTraderBlockEntity trader = new ArmorDisplayTraderBlockEntity(pos, state);
        trader.flagAsLoaded();
        return trader;
    }

    @Override
    public BlockEntityType<?> traderType() { return ModBlockEntities.ARMOR_TRADER; }

    @Override
    public void onBreak(World level, BlockPos pos, BlockState state, PlayerEntity player)
    {
        BlockEntity blockEntity = this.getBlockEntity(state, level, pos);
        if(blockEntity instanceof ArmorDisplayTraderBlockEntity)
        {
            ArmorDisplayTraderBlockEntity trader = (ArmorDisplayTraderBlockEntity)blockEntity;
            if(trader.canBreak(player))
                trader.destroyArmorStand();
        }
        super.onBreak(level, pos, state, player);

    }

    @Override
    public void onStateReplaced(BlockState state, World level, BlockPos pos, BlockState newState, boolean isMoving) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if(blockEntity instanceof ArmorDisplayTraderBlockEntity)
            ((ArmorDisplayTraderBlockEntity)blockEntity).destroyArmorStand();
        super.onStateReplaced(state, level, pos, newState, isMoving);
    }

    @Override
    protected Supplier<List<Text>> getItemTooltips() { return LCTooltips.ITEM_TRADER_ARMOR; }

}