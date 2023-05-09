package io.github.lightman314.lightmanscurrency.common.emergency_ejection;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.common.commands.CommandLCAdmin;
import io.github.lightman314.lightmanscurrency.common.ownership.OwnerData;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EjectionData implements Inventory, IClientTracker {

    private final OwnerData owner = new OwnerData(this, o -> {});
    MutableText traderName = Text.empty();
    public MutableText getTraderName() { return this.traderName; }
    List<ItemStack> items = new ArrayList<>();

    private boolean isClient = false;
    public void flagAsClient() { this.isClient = true; }
    public boolean isClient() { return this.isClient; }

    private EjectionData() {}

    private EjectionData(OwnerData owner, MutableText traderName, List<ItemStack> items) {
        this.owner.copyFrom(owner);
        this.traderName = traderName;
        this.items = items;
    }

    public boolean canAccess(PlayerEntity player) {
        if(CommandLCAdmin.isAdminPlayer(player))
            return true;
        return this.owner.isMember(player);
    }

    public NbtCompound save() {

        NbtCompound compound = new NbtCompound();

        compound.put("Owner", this.owner.save());

        compound.putString("Name", Text.Serializer.toJson(this.traderName));

        NbtList itemList = new NbtList();
        for (ItemStack item : this.items)
            itemList.add(item.writeNbt(new NbtCompound()));
        compound.put("Items", itemList);

        return compound;
    }

    public void load(NbtCompound compound) {
        if(compound.contains("Owner"))
            this.owner.load(compound.getCompound("Owner"));
        if(compound.contains("Name"))
            this.traderName = Text.Serializer.fromJson(compound.getString("Name"));
        if(compound.contains("Items"))
        {
            NbtList itemList = compound.getList("Items", NbtCompound.COMPOUND_TYPE);
            this.items = new ArrayList<>();
            for(int i = 0; i < itemList.size(); ++i)
            {
                this.items.add(ItemStack.fromNbt(itemList.getCompound(i)));
            }
        }

    }

    public static EjectionData create(World level, BlockPos pos, BlockState state, IDumpable trader) {
        return create(level, pos, state, trader, true);
    }

    public static EjectionData create(World level, BlockPos pos, BlockState state, IDumpable trader, boolean dropBlock) {

        OwnerData owner = trader.getOwner();

        MutableText traderName = trader.getName();

        List<ItemStack> items = trader.getContents(level, pos, state, dropBlock);

        return new EjectionData(owner, traderName, items);

    }

    public static EjectionData loadData(NbtCompound compound) {
        EjectionData data = new EjectionData();
        data.load(compound);
        return data;
    }

    @Override
    public void clear() { this.items.clear(); }

    @Override
    public int size() { return this.items.size(); }

    @Override
    public boolean isEmpty() {
        for(ItemStack stack : this.items)
        {
            if(!stack.isEmpty())
                return false;
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        if(slot >= this.items.size() || slot < 0)
            return ItemStack.EMPTY;
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int count) {
        if(slot >= this.items.size() || slot < 0)
            return ItemStack.EMPTY;
        return this.items.get(slot).split(count);
    }

    @Override
    public ItemStack removeStack(int slot) {
        if(slot >= this.items.size() || slot < 0)
            return ItemStack.EMPTY;
        ItemStack stack = this.items.get(slot);
        this.items.set(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setStack(int slot, ItemStack item) {
        if(slot >= this.items.size() || slot < 0)
            return;
        this.items.set(slot, item);
    }

    private void clearEmptySlots() { this.items.removeIf(ItemStack::isEmpty); }

    @Override
    public void markDirty() {
        if(this.isClient)
            return;
        this.clearEmptySlots();
        if(this.isEmpty())
            EjectionSaveData.RemoveEjectionData(this);
        else
            EjectionSaveData.MarkEjectionDataDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {return this.canAccess(player); }

}