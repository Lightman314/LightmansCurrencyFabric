package io.github.lightman314.lightmanscurrency.common.blockentity.trader;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces.IItemTraderBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ItemTraderBlockEntity extends TraderBlockEntity<ItemTraderData> {

    protected long rotationTime = 0;
    protected int tradeCount;
    protected boolean networkTrader = false;

    public ItemTraderBlockEntity(BlockPos pos, BlockState state) { this(ModBlockEntities.ITEM_TRADER, pos, state, 1, false); }

    public ItemTraderBlockEntity(BlockPos pos, BlockState state, int tradeCount) { this(ModBlockEntities.ITEM_TRADER, pos, state, tradeCount, false); }

    public ItemTraderBlockEntity(BlockPos pos, BlockState state, int tradeCount, boolean networkTrader) { this(ModBlockEntities.ITEM_TRADER, pos, state, tradeCount, networkTrader); }

    protected ItemTraderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { this(type, pos, state, 1, false);}

    protected ItemTraderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int tradeCount) { this(type, pos, state, tradeCount, false); }

    protected ItemTraderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int tradeCount, boolean networkTrader)
    {
        super(type, pos, state);
        this.tradeCount = tradeCount;
        this.networkTrader = networkTrader;
    }

    public ItemTraderData buildNewTrader() {
        ItemTraderData trader = new ItemTraderData(this.tradeCount, this.world, this.pos);
        if(this.networkTrader)
            trader.setAlwaysShowOnTerminal();
        return trader;
    }

    @Environment(EnvType.CLIENT)
    public List<Vector3f> GetStackRenderPos(int tradeSlot, boolean isDoubleTrade)
    {
        Block block = this.getCachedState().getBlock();
        if(block instanceof IItemTraderBlock)
        {
            IItemTraderBlock traderBlock = (IItemTraderBlock)block;
            return traderBlock.GetStackRenderPos(tradeSlot, this.getCachedState(), isDoubleTrade);
        }
        else
        {
            List<Vector3f> posList = new ArrayList<>();
            posList.add(new Vector3f(0.0f, 0.0f, 0.0f));
            return posList;
        }
    }

    @Environment(EnvType.CLIENT)
    public List<Quaternionf> GetStackRenderRot(int tradeSlot, float partialTicks)
    {
        Block block = this.getCachedState().getBlock();
        if(block instanceof IItemTraderBlock)
        {
            IItemTraderBlock traderBlock = (IItemTraderBlock)block;
            List<Quaternionf> rotation = traderBlock.GetStackRenderRot(tradeSlot, this.getCachedState());
            //If null received. Rotate item based on world time
            if(rotation == null)
            {
                rotation = new ArrayList<>();
                rotation.add(MathUtil.getRotationDegrees((this.rotationTime + partialTicks) * 2.0F));
            }
            return rotation;
        }
        else
        {
            List<Quaternionf> rotation = new ArrayList<>();
            rotation.add(MathUtil.getRotationDegrees(0f));
            return rotation;
        }
    }

    @Environment(EnvType.CLIENT)
    public float GetStackRenderScale(int tradeSlot)
    {
        Block block = this.getCachedState().getBlock();
        if(block instanceof IItemTraderBlock)
        {
            IItemTraderBlock traderBlock = (IItemTraderBlock)block;
            return traderBlock.GetStackRenderScale(tradeSlot, this.getCachedState());
        }
        else
            return 0f;
    }

    @Environment(EnvType.CLIENT)
    public int maxRenderIndex()
    {
        Block block = this.getCachedState().getBlock();
        if(block instanceof IItemTraderBlock)
        {
            IItemTraderBlock traderBlock = (IItemTraderBlock)block;
            return traderBlock.maxRenderIndex();
        }
        else
            return 0;
    }

    @Override
    public void writeNbt(NbtCompound compound)
    {
        super.writeNbt(compound);
        compound.putInt("TradeCount", this.tradeCount);
        compound.putBoolean("NetworkTrader", this.networkTrader);
    }

    @Override
    public void readNbt(NbtCompound compound)
    {
        super.readNbt(compound);
        this.tradeCount = compound.getInt("TradeCount");
        this.networkTrader = compound.getBoolean("NetworkTrader");
    }

    @Override
    public void clientTick() {
        super.clientTick();
        this.rotationTime++;
    }

}