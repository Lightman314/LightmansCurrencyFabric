package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks;

import io.github.lightman314.lightmanscurrency.common.blockentity.trader.TicketTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.templates.TraderBlockTallRotatable;
import io.github.lightman314.lightmanscurrency.common.blocks.util.LazyShapes;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;

import java.util.List;
import java.util.function.Supplier;


public class TicketKioskBlock extends TraderBlockTallRotatable {

    public static final int TRADECOUNT = 4;

    private static final VoxelShape HORIZ_SHAPE = createCuboidShape(3d,0d,1d,13d,32d,15d);
    private static final VoxelShape VERT_SHAPE = createCuboidShape(1d,0d,3d,15d,32d,13d);

    public TicketKioskBlock(Settings properties) { super(properties, LazyShapes.lazyTallDirectionalShape(VERT_SHAPE, HORIZ_SHAPE, VERT_SHAPE, HORIZ_SHAPE)); }

    @Override
    public BlockEntity makeTrader(BlockPos pos, BlockState state) { return new TicketTraderBlockEntity(pos, state, TRADECOUNT); }

    @Override
    public BlockEntityType<?> traderType() { return ModBlockEntities.TICKET_TRADER; }

    @Override
    protected Supplier<List<Text>> getItemTooltips() { return LCTooltips.ITEM_TRADER_TICKET; }

}