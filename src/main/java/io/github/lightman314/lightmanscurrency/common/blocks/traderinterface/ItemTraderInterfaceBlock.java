package io.github.lightman314.lightmanscurrency.common.blocks.traderinterface;

import java.util.List;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.blockentity.traderinterface.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.traderinterface.item.ItemTraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.traderinterface.templates.TraderInterfaceBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemTraderInterfaceBlock extends TraderInterfaceBlock {

    public ItemTraderInterfaceBlock(AbstractBlock.Settings properties) { super(properties); }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ItemTraderInterfaceBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<?> interfaceType() {
        return ModBlockEntities.ITEM_TRADER_INTERFACE;
    }

    @Override
    protected Supplier<List<Text>> getItemTooltips() { return LCTooltips.ITEM_TRADER_INTERFACE; }

    @Override
    protected void onInvalidRemoval(BlockState state, World level, BlockPos pos, TraderInterfaceBlockEntity trader) { }

}