package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces.IItemTraderBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.templates.TraderBlockBase;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class DisplayCaseBlock extends TraderBlockBase implements IItemTraderBlock {

    public static final int TRADECOUNT = 1;

    public DisplayCaseBlock(Settings properties)  {super(properties); }

    @Override
    protected BlockEntity makeTrader(BlockPos pos, BlockState state) { return new ItemTraderBlockEntity(pos, state, TRADECOUNT); }

    @Override
    public BlockEntityType<?> traderType() { return ModBlockEntities.ITEM_TRADER; }

    @Override
    @Environment(EnvType.CLIENT)
    public List<Vector3f> GetStackRenderPos(int tradeSlot, BlockState state, boolean isDoubleTrade) {
        List<Vector3f> posList = new ArrayList<>(1);
        posList.add(new Vector3f(0.5F, 0.5F + 2F/16F, 0.5F));
        return posList;
    }


    @Override
    @Environment(EnvType.CLIENT)
    public List<Quaternionf> GetStackRenderRot(int tradeSlot, BlockState state)
    {
        //Return null for automatic rotation
        return null;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public float GetStackRenderScale(int tradeSlot, BlockState state){ return 0.75f; }

    @Override
    @Environment(EnvType.CLIENT)
    public int maxRenderIndex() {return TRADECOUNT; }

    @Override
    protected Supplier<List<Text>> getItemTooltips() { return LCTooltips.ITEM_TRADER; }

    @Override
    protected void onInvalidRemoval(BlockState state, World level, BlockPos pos, TraderData trader) { }

}