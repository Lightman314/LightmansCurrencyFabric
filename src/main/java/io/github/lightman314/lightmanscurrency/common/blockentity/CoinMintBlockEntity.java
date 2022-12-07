package io.github.lightman314.lightmanscurrency.common.blockentity;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.crafting.CoinMintRecipe;
import io.github.lightman314.lightmanscurrency.common.crafting.RecipeValidator;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CoinMintBlockEntity extends BlockEntity implements SidedInventory {

    SimpleInventory storage = new SimpleInventory(2);
    public SimpleInventory getStorage() { return this.storage; }

    private final List<CoinMintRecipe> getCoinMintRecipes()
    {
        if(this.world != null)
            return getCoinMintRecipes(this.world);
        return new ArrayList<>();
    }

    public static final List<CoinMintRecipe> getCoinMintRecipes(World level) {
        return RecipeValidator.getValidRecipes(level).getCoinMintRecipes();
    }

    public CoinMintBlockEntity(BlockPos pos, BlockState state) { this(ModBlockEntities.COIN_MINT, pos, state); }

    protected CoinMintBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
        this.storage.addListener(container -> this.markDirty());
    }

    @Override
    public void writeNbt(NbtCompound compound)
    {
        InventoryUtil.saveAllItems("Storage", compound, this.storage);
        super.writeNbt(compound);
    }

    @Override
    public void readNbt(NbtCompound compound)
    {
        super.readNbt(compound);

        this.storage = InventoryUtil.loadAllItems("Storage", compound, 2);
        this.storage.addListener(container -> this.markDirty());

    }

    public void dumpContents(World world, BlockPos pos) { InventoryUtil.dumpContents(world, pos, this.storage); }

    //Coin Minting Functions
    public boolean validMintInput()
    {
        return !getMintOutput().isEmpty();
    }

    public boolean validMintInput(ItemStack item)
    {
        Inventory tempInv = new SimpleInventory(1);
        tempInv.setStack(0, item);
        for(CoinMintRecipe recipe : this.getCoinMintRecipes())
        {
            if(recipe.matches(tempInv, this.world))
                return true;
        }
        return false;
    }

    /**
     * Returns the amount of available empty space the output slot has.
     * Returns 0 if the mint input does not create the same item currently in the output slot.
     */
    public int validOutputSpace()
    {
        //Determine how many more coins can fit in the output slot based on the input item
        ItemStack mintOutput = getMintOutput();
        ItemStack currentOutputSlot = this.getStorage().getStack(1);
        if(currentOutputSlot.isEmpty())
            return 64;
        else if(currentOutputSlot.getItem() != mintOutput.getItem())
            return 0;
        return 64 - currentOutputSlot.getCount();
    }

    /**
     * Returns the current item that would result from a single minting of the current input item
     */
    public ItemStack getMintOutput()
    {
        ItemStack mintInput = this.getStorage().getStack(0);
        if(mintInput.isEmpty())
            return ItemStack.EMPTY;
        for(CoinMintRecipe recipe : this.getCoinMintRecipes())
        {
            if(recipe.matches(this.storage, this.world))
                return recipe.craft(this.storage).copy();
        }

        return ItemStack.EMPTY;
    }

    /**
     * Returns the maximum result item stack that can fit into the output slots.
     */
    public ItemStack getMintableOutput() {
        ItemStack output = getMintOutput();
        int countPerMint = output.getCount();
        int outputSpace = validOutputSpace();
        //Shrink by 1, as the first input item is consumed in the starting output item count
        int inputCount = this.storage.getStack(0).getCount() - 1;
        while(output.getCount() + countPerMint <= outputSpace && inputCount > 0)
        {
            output.increment(countPerMint);
            inputCount--;
        }
        return output;
    }

    public void mintCoins(int mintCount)
    {
        //Ignore if no valid input is present
        if(!validMintInput())
            return;

        //Determine how many to mint based on the input count & whether a fullStack input was given.
        if(mintCount > this.getStorage().getStack(0).getCount())
        {
            mintCount = this.getStorage().getStack(0).getCount();
        }

        //Confirm that the output slot has enough room for the expected outputs
        if(mintCount > validOutputSpace())
            mintCount = validOutputSpace();
        if(mintCount <= 0)
            return;

        //Get the output items
        ItemStack mintOutput = getMintOutput();
        mintOutput.setCount(mintCount);

        //Place the output item(s)
        if(this.getStorage().getStack(1).isEmpty())
        {
            this.getStorage().setStack(1, mintOutput);
        }
        else
        {
            this.getStorage().getStack(1).setCount(this.getStorage().getStack(1).getCount() + mintOutput.getCount());
        }

        //Remove the input item(s)
        this.getStorage().getStack(0).setCount(this.getStorage().getStack(0).getCount() - mintCount);

        //Job is done!
        this.markDirty();

    }

    @Override
    public NbtCompound toInitialChunkDataNbt() { return this.createNbt(); }

    @Override
    public int[] getAvailableSlots(Direction side) { return side == Direction.DOWN ? new int[]{1} : new int[]{0}; }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) { return dir != Direction.DOWN; }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) { return dir == Direction.DOWN; }

    @Override
    public int size() { return this.storage.size(); }

    @Override
    public boolean isEmpty() { return this.storage.isEmpty(); }

    @Override
    public ItemStack getStack(int slot) {
        if(slot == 1 && this.storage.getStack(1).isEmpty())
            return this.getMintableOutput();
        return this.storage.getStack(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        if(slot != 1)
            return ItemStack.EMPTY;
        else
        {
            ItemStack outputItem = this.storage.getStack(1);
            //Mint coins until we have the requested amount if the output stack is empty
            if(outputItem.isEmpty() || InventoryUtil.ItemMatches(outputItem, this.getMintOutput()))
                this.mintCoins(amount - outputItem.getCount());
            return this.storage.removeStack(slot, amount);
        }
    }

    @Override
    public ItemStack removeStack(int slot) { return removeStack(slot, 64); }

    @Override
    public void setStack(int slot, ItemStack stack) { this.storage.setStack(slot, stack); }

    @Override
    public boolean canPlayerUse(PlayerEntity player) { return false; }

    @Override
    public void clear() { this.storage.clear(); }

}