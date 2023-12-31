package io.github.lightman314.lightmanscurrency.common.blockentity.trader;

import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class SlotMachineTraderBlockEntity extends TraderBlockEntity<SlotMachineTraderData> {

    public SlotMachineTraderBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.SLOT_MACHINE, pos, state); }

    @Override
    protected SlotMachineTraderData buildNewTrader() { return new SlotMachineTraderData(this.world, this.pos); }
}
