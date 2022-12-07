package io.github.lightman314.lightmanscurrency.common.traders.item;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.ItemTradeRestriction;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.TicketKioskRestriction;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemTraderDataTicket extends ItemTraderData {

    public static final Identifier TYPE = new Identifier(LightmansCurrency.MODID, "item_trader_ticket");

    public ItemTraderDataTicket() { super(TYPE); }

    public ItemTraderDataTicket(int tradeCount, World level, BlockPos pos) { super(TYPE, tradeCount, level, pos); }

    @Override
    protected ItemTradeRestriction getTradeRestriction(int tradeIndex) { return TicketKioskRestriction.INSTANCE; }

}