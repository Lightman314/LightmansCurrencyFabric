package io.github.lightman314.lightmanscurrency.common.traders.auction;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.common.ownership.PlayerReference;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class AuctionPlayerStorage {

    PlayerReference owner;
    public PlayerReference getOwner() { return this.owner; }

    CoinValue storedCoins = new CoinValue();
    public CoinValue getStoredCoins() { return this.storedCoins; }
    List<ItemStack> storedItems = new ArrayList<>();
    public List<ItemStack> getStoredItems() { return this.storedItems; }

    public AuctionPlayerStorage(PlayerReference player) { this.owner = player; }

    public AuctionPlayerStorage(NbtCompound compound) { this.load(compound); }

    public NbtCompound save(NbtCompound compound) {

        compound.put("Owner", this.owner.save());

        this.storedCoins.save(compound, "StoredMoney");
        NbtList itemList = new NbtList();
        for(int i = 0; i < this.storedItems.size(); ++i)
        {
            itemList.add(this.storedItems.get(i).writeNbt(new NbtCompound()));
        }
        compound.put("StoredItems", itemList);

        return compound;
    }

    protected void load(NbtCompound compound) {

        this.owner = PlayerReference.load(compound.getCompound("Owner"));

        this.storedCoins.load(compound, "StoredMoney");

        this.storedItems.clear();
        NbtList itemList = compound.getList("StoredItems", NbtElement.COMPOUND_TYPE);
        for(int i = 0; i < itemList.size(); ++i)
        {
            ItemStack stack = ItemStack.fromNbt(itemList.getCompound(i));
            if(!stack.isEmpty())
                this.storedItems.add(stack);
        }

    }

    public void giveMoney(CoinValue amount) {
        this.storedCoins.addValue(amount);
    }

    /**
     * Removes the given amount of money from the stored money.
     * Returns the money amount that was unable to be removed.
     */
    public CoinValue takeMoney(CoinValue amount) {
        long newValue = this.storedCoins.getRawValue() - amount.getRawValue();
        if(newValue < 0)
        {
            this.storedCoins = new CoinValue();
            return new CoinValue(-newValue);
        }
        else
        {
            this.storedCoins.loadFromOldValue(newValue);
            return new CoinValue();
        }
    }

    public void collectedMoney(PlayerEntity player) {
        MoneyUtil.ProcessChange(null, player, this.storedCoins.copy());
        this.storedCoins = new CoinValue();
    }

    public void giveItem(ItemStack item) {
        if(!item.isEmpty())
        {
            this.storedItems.add(item);
        }
    }

    public void removePartial(int itemSlot, int count) {
        if(this.storedItems.size() >= itemSlot || itemSlot < 0)
            return;
        this.storedItems.get(itemSlot).decrement(count);
        if(this.storedItems.get(itemSlot).isEmpty())
            this.storedItems.remove(itemSlot);
    }

    public void collectItems(PlayerEntity player) {
        for(ItemStack stack : this.storedItems) InventoryUtil.GiveToPlayer(player, stack);
        this.storedItems = new ArrayList<>();
    }

}