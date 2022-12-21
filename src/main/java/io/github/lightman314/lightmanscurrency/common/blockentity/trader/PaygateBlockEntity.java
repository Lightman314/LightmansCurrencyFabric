package io.github.lightman314.lightmanscurrency.common.blockentity.trader;

import io.github.lightman314.lightmanscurrency.common.blocks.PaygateBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.common.ownership.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.tradedata.PaygateTradeData;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class PaygateBlockEntity extends TraderBlockEntity<PaygateTraderData> {

    private int timer = 0;

    public PaygateBlockEntity(BlockPos pos, BlockState state) { this(ModBlockEntities.PAYGATE, pos, state); }

    protected PaygateBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }

    @Override
    public void writeNbt(NbtCompound compound) {
        super.writeNbt(compound);
        this.saveTimer(compound);
    }

    public final void saveTimer(NbtCompound compound) { compound.putInt("Timer", Math.max(this.timer, 0)); }

    @Override
    public void readNbt(NbtCompound compound) {
        //Load the timer
        if(compound.contains("Timer", NbtElement.INT_TYPE))
            this.timer = Math.max(compound.getInt("Timer"), 0);
        super.readNbt(compound);
    }

    public boolean isActive() { return this.timer > 0; }

    public void activate(int duration) {
        this.timer = duration;
        if(this.world != null)
            this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).with(PaygateBlock.POWERED, true));
        this.markDirty();
    }

    @Override
    public void serverTick()
    {
        super.serverTick();
        if(this.timer > 0)
        {
            this.timer--;
            this.markDirty();
            if(this.timer <= 0 && this.world != null)
            {
                this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).with(PaygateBlock.POWERED, false));
            }
        }
    }

    @Override
    public void onLoad() {
        PaygateTraderData data = this.getTraderData();
        if(data != null)
            data.setCachedBE(this);
    }

    public int getValidTicketTrade(PlayerEntity player, ItemStack heldItem) {
        PaygateTraderData trader = this.getTraderData();
        if(heldItem.getItem() == ModItems.TICKET)
        {
            UUID ticketID = TicketItem.GetTicketID(heldItem);
            if(ticketID != null)
            {
                for(int i = 0; i < trader.getTradeCount(); ++i)
                {
                    PaygateTradeData trade = trader.getTrade(i);
                    if(trade.isTicketTrade() && trade.getTicketID().equals(ticketID))
                    {
                        //Confirm that the player is allowed to access the trade
                        if(!trader.runPreTradeEvent(PlayerReference.of(player), trade).isCanceled())
                            return i;
                    }
                }
            }
        }
        return -1;
    }

    @Override
    protected PaygateTraderData buildNewTrader() { return new PaygateTraderData(this.world, this.pos); }

}