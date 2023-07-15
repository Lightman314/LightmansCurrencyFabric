package io.github.lightman314.lightmanscurrency.fluid;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class FluidStack implements FluidVariant {

    public static final FluidStack EMPTY = new FluidStack(Fluids.EMPTY, 0);

    private final Fluid fluid;
    private long quantity = 0;
    private NbtCompound nbt;

    public FluidStack(Fluid fluid, long quantity) { this(fluid, quantity, null); }
    public FluidStack(Fluid fluid, long quantity, NbtCompound nbt) { this.fluid = fluid == null ? Fluids.EMPTY : fluid; this.quantity = Math.max(quantity, 0); this.nbt = nbt; }

    public final Fluid getFluid() { return this.fluid; }

    public final boolean isEmpty() { return this.fluid == Fluids.EMPTY || this.quantity <= 0; }

    public long getAmount() { return this.quantity; }
    public void increment(long amount) { this.quantity += quantity; }
    public void decrement(long amounnt) { this.quantity -= quantity; }

    @Override
    public boolean isBlank() { return this.isEmpty(); }
    @Override
    public Fluid getObject() { return this.fluid;}

    public NbtCompound getNbt() { return this.nbt; }
    @Override
    public NbtCompound toNbt() {
        NbtCompound compound = new NbtCompound();
        compound.putString("Fluid", Registries.FLUID.getId(this.fluid).toString());
        compound.putLong("Quantity", Math.max(this.quantity, 0));
        if(this.nbt != null)
            compound.put("Tag", this.nbt);
        return compound;
    }

    public static FluidStack fromNbt(NbtCompound compound) {
        Fluid fluid = Registries.FLUID.get(new Identifier(compound.getString("Fluid")));
        long quantity = compound.getLong("Quantity");
        NbtCompound nbt = null;
        if(compound.contains("Tag", NbtElement.COMPOUND_TYPE))
            nbt = compound.getCompound("Tag");
        FluidStack stack = new FluidStack(fluid, quantity, nbt);
        return stack.isEmpty() ? FluidStack.EMPTY : stack;
    }

    @Override
    public void toPacket(PacketByteBuf buffer) {
        buffer.writeBoolean(this.isEmpty());
        if(!this.isEmpty())
        {
            buffer.writeString(Registries.FLUID.getId(this.fluid).toString());
            buffer.writeLong(Math.max(this.quantity, 0));
            buffer.writeBoolean(this.nbt != null);
            if(this.nbt != null)
                buffer.writeNbt(this.nbt);
        }
    }

    public static FluidStack fromPacket(PacketByteBuf buffer) {
        if(buffer.readBoolean())
            return FluidStack.EMPTY;
        Fluid fluid = Registries.FLUID.get(new Identifier(buffer.readString()));
        long quantity = buffer.readLong();
        NbtCompound nbt = buffer.readBoolean() ? buffer.readUnlimitedNbt() : null;
        FluidStack stack = new FluidStack(fluid, quantity, nbt);
        return stack.isEmpty() ? FluidStack.EMPTY : stack;
    }

    public boolean matches(FluidStack other) {
        return other.fluid == this.fluid && this.nbtMatches(other.nbt);
    }

    public NbtCompound getOrCreateNbt() { if(this.nbt == null) this.nbt = new NbtCompound(); return this.nbt; }

}
