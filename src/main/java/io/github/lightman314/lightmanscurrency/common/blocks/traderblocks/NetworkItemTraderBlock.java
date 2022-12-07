package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks;

import java.util.List;

import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.templates.TraderBlockRotatable;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.items.TooltipItem;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class NetworkItemTraderBlock extends TraderBlockRotatable {

    public static final int TRADE_COUNT_T1 = 4;
    public static final int TRADE_COUNT_T2 = 8;
    public static final int TRADE_COUNT_T3 = 12;
    public static final int TRADE_COUNT_T4 = 16;

    private final int tradeCount;

    public NetworkItemTraderBlock(Settings properties, int tradeCount) { super(properties); this.tradeCount = tradeCount; }

    @Override
    protected BlockEntity makeTrader(BlockPos pos, BlockState state) { return new ItemTraderBlockEntity(pos, state, this.tradeCount, true); }

    @Override
    protected BlockEntityType<?> traderType() { return ModBlockEntities.ITEM_TRADER; }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView level, List<Text> tooltip, TooltipContext flagIn)
    {
        TooltipItem.addTooltip(tooltip, LCTooltips.ITEM_NETWORK_TRADER);
        super.appendTooltip(stack, level, tooltip, flagIn);
    }

}