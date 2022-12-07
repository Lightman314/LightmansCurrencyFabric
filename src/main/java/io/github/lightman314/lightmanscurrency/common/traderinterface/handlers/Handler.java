package io.github.lightman314.lightmanscurrency.common.traderinterface.handlers;

import io.github.lightman314.lightmanscurrency.common.blockentity.traderinterface.TraderInterfaceBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public abstract class Handler{

    private TraderInterfaceBlockEntity parent;
    public TraderInterfaceBlockEntity getParent() { return this.parent; }
    public void setParent(TraderInterfaceBlockEntity parent) { if(this.parent == null) this.parent = parent; }

    public final boolean hasFluidStorage(Direction relativeSide) { return this.getFluidStorage(relativeSide) != null; }
    public Storage<FluidVariant> getFluidStorage(Direction relativeSide) { return null; }

    public final boolean hasItemStorage(Direction relativeSide) { return this.getItemStorage(relativeSide) != null; }
    public Storage<ItemVariant> getItemStorage(Direction relativeSide) { return null; }

    public abstract Identifier getType();
    public abstract String getTag();

    public abstract NbtCompound save();
    public abstract void load(NbtCompound compound);

    public void sendMessage(NbtCompound message) { this.parent.sendHandlerMessage(this.getType(), message); }

    protected final boolean isClient() { return this.parent.isClient(); }

    public final void markDirty() { this.parent.setHandlerDirty(this); }

    public abstract void receiveMessage(NbtCompound message);

}
