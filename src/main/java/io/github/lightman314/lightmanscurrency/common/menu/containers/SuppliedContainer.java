package io.github.lightman314.lightmanscurrency.common.menu.containers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class SuppliedContainer implements Inventory {

    public final Supplier<Inventory> source;

    @Nullable
    private List<InventoryChangedListener> listeners = new ArrayList<>();

    public SuppliedContainer(Supplier<Inventory> source)  { this.source = source; }

    public void addListener(InventoryChangedListener listener) {
        if(!this.listeners.contains(listener))
            this.listeners.add(listener);
    }

    public void removeListener(InventoryChangedListener listener) {
        if (this.listeners.contains(listener))
            this.listeners.remove(listener);
    }

    @Override
    public void clear() { source.get().clear(); }

    @Override
    public int size() { return source.get().size(); }

    @Override
    public boolean isEmpty() { return source.get().isEmpty(); }

    @Override
    public ItemStack getStack(int slot) { return source.get().getStack(slot); }

    @Override
    public ItemStack removeStack(int slot, int amount) { return source.get().removeStack(slot, amount); }

    @Override
    public ItemStack removeStack(int slot) { return source.get().removeStack(slot); }

    @Override
    public void setStack(int slot, ItemStack stack) { source.get().setStack(slot, stack); }

    @Override
    public int getMaxCountPerStack() { return source.get().getMaxCountPerStack(); }

    @Override
    public void markDirty() {
        source.get().markDirty();
        if (this.listeners != null) {
            for(InventoryChangedListener listener : this.listeners) {
                listener.onInventoryChanged(this);
            }
        }
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) { return source.get().canPlayerUse(player); }

    @Override
    public void onOpen(PlayerEntity player) { source.get().onOpen(player); }

    @Override
    public void onClose(PlayerEntity player) { source.get().onClose(player); }

    @Override
    public boolean isValid(int p_18952_, ItemStack p_18953_) { return source.get().isValid(p_18952_, p_18953_); }

    @Override
    public int count(Item item) { return source.get().count(item); }

    @Override
    public boolean containsAny(Set<Item> item) { return source.get().containsAny(item); }

}