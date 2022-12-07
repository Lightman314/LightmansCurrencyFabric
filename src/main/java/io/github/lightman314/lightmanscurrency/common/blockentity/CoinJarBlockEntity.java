package io.github.lightman314.lightmanscurrency.common.blockentity;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.network.util.BlockEntityUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;

public class CoinJarBlockEntity extends BlockEntity
{

    public static final int COIN_LIMIT = 64;

    List<ItemStack> storage = new ArrayList<>();
    public List<ItemStack> getStorage() { return storage; }

    public CoinJarBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.COIN_JAR, pos, state); }

    public boolean addCoin(ItemStack coin)
    {
        if(getCurrentCount() >= COIN_LIMIT)
            return false;
        if(!MoneyUtil.isCoin(coin, false))
            return false;

        boolean foundStack = false;
        for(int i = 0; i < storage.size() && !foundStack; i++)
        {
            if(InventoryUtil.ItemMatches(coin, storage.get(i)))
            {
                if(storage.get(i).getCount() < storage.get(i).getMaxCount())
                {
                    storage.get(i).increment(1);
                    foundStack = true;
                }
            }
        }
        if(!foundStack)
        {
            ItemStack newCoin = coin.copy();
            newCoin.setCount(1);
            this.storage.add(newCoin);
        }

        if(!this.world.isClient)
        {
            BlockEntityUtil.sendUpdatePacket(this, this.writeStorage(new NbtCompound()));
        }
        return true;
    }

    protected int getCurrentCount()
    {
        int count = 0;
        for(int i = 0; i < storage.size(); i++)
            count += storage.get(i).getCount();
        return count;
    }

    @Override
    public void writeNbt(NbtCompound compound)
    {
        this.writeStorage(compound);

        super.writeNbt(compound);
    }

    protected NbtCompound writeStorage(NbtCompound compound)
    {
        NbtList storageList = new NbtList();
        for(int i = 0; i < storage.size(); i++)
            storageList.add(storage.get(i).writeNbt(new NbtCompound()));
        compound.put("Coins", storageList);

        return compound;
    }

    @Override
    public void readNbt(NbtCompound compound)
    {
        if(compound.contains("Coins"))
        {
            storage = new ArrayList<>();
            NbtList storageList = compound.getList("Coins", NbtElement.COMPOUND_TYPE);
            for(int i = 0; i < storageList.size(); i++)
            {
                NbtCompound thisItem = storageList.getCompound(i);
                storage.add(ItemStack.fromNbt(thisItem));
            }
        }
        super.readNbt(compound);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() { return this.createNbt(); }

    //For reading/writing the storage when silk touched.
    public void writeItemTag(ItemStack item)
    {
        NbtCompound compound = item.getOrCreateNbt();
        compound.put("JarData", this.writeStorage(new NbtCompound()));
    }

    public void readItemTag(ItemStack item)
    {
        if(item.hasNbt())
        {
            NbtCompound compound = item.getNbt();
            if(compound.contains("JarData", NbtElement.COMPOUND_TYPE))
            {
                NbtCompound jarData = compound.getCompound("JarData");
                if(jarData.contains("Coins"))
                {
                    storage = new ArrayList<>();
                    NbtList storageList = jarData.getList("Coins", NbtElement.COMPOUND_TYPE);
                    for(int i = 0; i < storageList.size(); i++)
                    {
                        NbtCompound thisItem = storageList.getCompound(i);
                        storage.add(ItemStack.fromNbt(thisItem));
                    }
                }
            }
        }
    }

}