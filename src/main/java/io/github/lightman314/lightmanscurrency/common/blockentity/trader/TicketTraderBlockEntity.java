package io.github.lightman314.lightmanscurrency.common.blockentity.trader;

import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderDataTicket;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;


public class TicketTraderBlockEntity extends ItemTraderBlockEntity {


    public TicketTraderBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.TICKET_TRADER, pos, state);
    }

    public TicketTraderBlockEntity(BlockPos pos, BlockState state, int tradeCount)
    {
        super(ModBlockEntities.TICKET_TRADER, pos, state, tradeCount);
    }

    @Override
    public ItemTraderData buildNewTrader() { return new ItemTraderDataTicket(this.tradeCount, this.world, this.pos); }

}