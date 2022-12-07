package io.github.lightman314.lightmanscurrency.common.traders.item.storage;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

public class TraderItemInventory implements SidedInventory {

    private final TraderItemStorage storage;
    private final Runnable markDirty;
    private final Function<Direction,Boolean> allowInputs;
    private final Function<Direction,Boolean> allowOutputs;

    public TraderItemInventory(TraderItemStorage storage, @Nullable Runnable markDirty, Function<Direction,Boolean> allowInputs, Function<Direction,Boolean> allowOutputs)
    {
        this.storage = storage;
        this.markDirty = markDirty == null ? () -> {} : markDirty;
        this.allowInputs = allowInputs;
        this.allowOutputs = allowOutputs;
    }
    private final List<ItemStack> getStorageItems() { return this.storage.getContents(); }
    @Override
    public int size() { return this.getStorageItems().size() + 1; }
    @Override
    public boolean isEmpty() { return this.getStorageItems().size() == 0; }
    @Override
    public ItemStack getStack(int slot) { return slot == 0 ? ItemStack.EMPTY : this.getStorageItems().get(slot - 1); }
    @Override
    public ItemStack removeStack(int slot, int amount) {
        if(slot == 0)
            return ItemStack.EMPTY;
        ItemStack removalStack = this.getStack(slot);
        ItemStack removed = removalStack.copy();
        removed.setCount(Math.min(removalStack.getCount(), amount));
        if(removed.isEmpty())
            return ItemStack.EMPTY;
        return removed;
    }
    @Override
    public ItemStack removeStack(int slot) { return this.removeStack(slot, 64); }
    @Override
    public void setStack(int slot, ItemStack stack) {
        if(slot == 0 && this.storage.allowExternalInput(stack))
            this.storage.tryAddItem(stack);
        else if(slot != 0)
            LightmansCurrency.LogWarning("Attempted to place item in a non-zero slot of the TraderItemInventory extension. This should not be happening as input is only allowed on slot 0");
    }
    @Override
    public void markDirty() { this.markDirty.run(); }
    @Override
    public boolean canPlayerUse(PlayerEntity player) { return true; }
    @Override
    public void clear() {}
    @Override
    public int[] getAvailableSlots(Direction side) {
        boolean input = this.allowInputs.apply(side);
        boolean output = this.allowOutputs.apply(side);
        if(input && output)
            return IntStream.range(0, this.size() - 1).toArray();
        if(input)
            return new int[]{1};
        if(output)
            return IntStream.range(1, this.size() - 1).toArray();
        return new int[0];
    }
    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction side) { return slot == 0 && this.storage.allowExternalInput(stack) && this.allowInputs.apply(side); }
    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction side) { return slot > 0 && this.storage.allowExternalOutput(stack) && this.allowOutputs.apply(side); }

}
