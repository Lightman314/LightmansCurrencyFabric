package io.github.lightman314.lightmanscurrency.common.blockentity.trader;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blockentity.TickableBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.interfaces.IOwnableBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.ownership.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.network.util.BlockEntityUtil;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public abstract class TraderBlockEntity<D extends TraderData> extends TickableBlockEntity implements IOwnableBlockEntity, SidedStorageBlockEntity {

    private long traderID = -1;
    public long getTraderID() { return this.traderID; }
    @Deprecated
    public void setTraderID(long traderID) { this.traderID = traderID; }

    private NbtCompound customTrader = null;
    private boolean ignoreCustomTrader = false;

    private boolean legitimateBreak = false;
    public void flagAsLegitBreak() { this.legitimateBreak = true; }
    public boolean legitimateBreak() { return this.legitimateBreak; }

    public final boolean isClient() { return this.world != null && this.world.isClient; }

    public TraderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }

    private D buildTrader(PlayerEntity owner, ItemStack placementStack)
    {
        if(this.customTrader != null)
        {
            D newTrader = this.fullyBuildCustomTrader();
            if(newTrader != null)
                return newTrader;
        }
        D newTrader = this.buildNewTrader();
        newTrader.getOwner().SetOwner(PlayerReference.of(owner));
        if(placementStack.hasCustomName())
            newTrader.setCustomName(null, placementStack.getName().getString());
        return newTrader;
    }

    protected final D initCustomTrader()
    {
        try {
            return (D)TraderData.Deserialize(false, this.customTrader);
        } catch(Throwable t) { LightmansCurrency.LogError("Error while attempting to load the custom trader!", t); }
        return null;
    }


    protected final D fullyBuildCustomTrader()
    {
        try {
            D newTrader = this.initCustomTrader();
            this.moveCustomTrader(newTrader);
            return newTrader;
        } catch(Throwable t) { LightmansCurrency.LogError("Error while attempting to load the custom trader!", t); }
        return null;
    }

    protected final void moveCustomTrader(D customTrader)
    {
        if(customTrader != null)
            customTrader.move(this.world, this.pos);
    }

    protected abstract D buildNewTrader();

    public final void saveCurrentTraderAsCustomTrader() {
        TraderData trader = this.getTraderData();
        if(trader != null)
        {
            this.customTrader = trader.save();
            this.ignoreCustomTrader = true;
            this.markDirty();
        }
    }

    public void initialize(PlayerEntity owner, ItemStack placementStack)
    {
        if(this.getTraderData() != null)
            return;

        D newTrader = this.buildTrader(owner, placementStack);
        //Register to the trading office
        this.traderID = TraderSaveData.RegisterTrader(newTrader, owner);
        //Send update packet to connected clients, so that they'll have the new trader id.
        this.markDirty();
    }

    private TraderData getRawTraderData() { return TraderSaveData.GetTrader(this.isClient(), this.traderID); }

    @SuppressWarnings("unchecked")
    public D getTraderData()
    {
        //Get from trading office
        TraderData rawData = this.getRawTraderData();
        try {
            return (D)rawData;
        } catch(Throwable t) { t.printStackTrace(); return null; }
    }

    @Override
    public void writeNbt(NbtCompound compound) {
        super.writeNbt(compound);
        compound.putLong("TraderID", this.traderID);
        if(this.customTrader != null)
            compound.put("CustomTrader", this.customTrader);
    }

    public void readNbt(NbtCompound compound)
    {
        super.readNbt(compound);
        if(compound.contains("TraderID", NbtElement.LONG_TYPE))
            this.traderID = compound.getLong("TraderID");
        if(compound.contains("CustomTrader"))
            this.customTrader = compound.getCompound("CustomTrader");
    }

    @Override
    public void serverTick() {
        if(this.world == null)
            return;
        if(this.customTrader != null && !this.ignoreCustomTrader)
        {
            //Build the custom trader
            D customTrader = this.initCustomTrader();
            if(customTrader == null)
            {
                LightmansCurrency.LogWarning("The trader block at " + this.pos.toShortString() + " could not properly load it's custom trader.");
                this.customTrader = null;
                this.markDirty();
                return;
            }
            //Check if the custom trader is this position & dimension
            if(customTrader.getLevel() == this.world.getRegistryKey() && this.pos.equals(customTrader.getPos()))
                this.ignoreCustomTrader = true;
            else
            {
                //If the dimension and position don't match exactly, assume it's been moved and load the custom trader
                this.moveCustomTrader(customTrader);
                this.traderID = TraderSaveData.RegisterTrader(customTrader, null);
                this.customTrader = null;
                this.ignoreCustomTrader = true;
                this.markDirty();
                LightmansCurrency.LogInfo("Successfully loaded custom trader at " + this.pos.toShortString());
            }
        }
    }

    public final void markDirty() {
        super.markDirty();
        if(!this.isClient())
            BlockEntityUtil.sendUpdatePacket(this);
    }

    @Override
    public void onLoad()
    {
        if(this.world.isClient)
            BlockEntityUtil.requestUpdatePacket(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() { return this.createNbt(); }

    public boolean canBreak(PlayerEntity player)
    {
        TraderData trader = this.getTraderData();
        if(trader != null)
            return trader.hasPermission(player, Permissions.BREAK_TRADER);
        return true;
    }

    public void deleteTrader() {
        if(this.getTraderData() != null)
            TraderSaveData.DeleteTrader(this.traderID);
    }

    @Nullable
    @Override
    public Storage<ItemVariant> getItemStorage(Direction side) {
        D trader = this.getTraderData();
        if(trader != null)
            return trader.getItemStorage(this.getRelativeSide(side));
        return null;
    }

    @Nullable
    @Override
    public Storage<FluidVariant> getFluidStorage(Direction side) {
        D trader = this.getTraderData();
        if(trader != null)
            return trader.getFluidStorage(this.getRelativeSide(side));
        return null;
    }

    public final Direction getRelativeSide(Direction side) {
        if(this.getCachedState().getBlock() instanceof IRotatableBlock block)
            return IRotatableBlock.getRelativeSide(block.getFacing(this.getCachedState()), side);
        return side;
    }

    /*@Override
    public AABB getRenderBoundingBox()
    {
        if(this.getBlockState() != null)
            return this.getBlockState().getCollisionShape(this.level, this.worldPosition).bounds().move(this.worldPosition);
        return super.getRenderBoundingBox();
    }*/


}