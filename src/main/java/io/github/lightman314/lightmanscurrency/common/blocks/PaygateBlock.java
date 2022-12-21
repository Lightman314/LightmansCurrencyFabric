package io.github.lightman314.lightmanscurrency.common.blocks;

import io.github.lightman314.lightmanscurrency.common.blockentity.trader.PaygateBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.templates.TraderBlockRotatable;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.items.TooltipItem;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PaygateBlock extends TraderBlockRotatable {

    public static final BooleanProperty POWERED = Properties.POWERED;

    public PaygateBlock(Settings properties)
    {
        super(properties);
        this.setDefaultState(
                this.getDefaultState()
                        .with(POWERED, false)
        );
    }

    @Override
    public ActionResult onUse(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult result)
    {
        if(!level.isClient)
        {
            //Attempt to trigger ticket trade without opening the UI
            BlockEntity tileEntity = level.getBlockEntity(pos);
            if(tileEntity instanceof PaygateBlockEntity paygate)
            {
                int tradeIndex = paygate.getValidTicketTrade(player, player.getStackInHand(hand));
                if(tradeIndex >= 0)
                {
                    PaygateTraderData trader = paygate.getTraderData();
                    if(trader != null)
                    {
                        trader.ExecuteTrade(TradeContext.create(trader, player).build(), tradeIndex);
                        return ActionResult.SUCCESS;
                    }
                }
            }
        }
        return super.onUse(state, level, pos, player, hand, result);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
    {
        super.appendProperties(builder);
        builder.add(POWERED);
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) { return true; }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if(state.get(POWERED))
            return 15;
        return 0;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView level, List<Text> tooltip, TooltipContext flagIn)
    {
        TooltipItem.addTooltip(tooltip, LCTooltips.PAYGATE);
        super.appendTooltip(stack, level, tooltip, flagIn);
    }

    @Override
    protected BlockEntity makeTrader(BlockPos pos, BlockState state) { return new PaygateBlockEntity(pos, state); }

    @Override
    protected BlockEntityType<?> traderType() { return ModBlockEntities.PAYGATE; }

}