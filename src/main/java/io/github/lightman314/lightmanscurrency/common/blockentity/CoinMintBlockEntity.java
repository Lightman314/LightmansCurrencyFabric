package io.github.lightman314.lightmanscurrency.common.blockentity;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.crafting.CoinMintRecipe;
import io.github.lightman314.lightmanscurrency.common.crafting.RecipeValidator;
import io.github.lightman314.lightmanscurrency.network.util.BlockEntityUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CoinMintBlockEntity extends TickableBlockEntity implements SidedInventory {

    SimpleInventory storage = new SimpleInventory(2);
    public SimpleInventory getStorage() { return this.storage; }

    private CoinMintRecipe lastRelevantRecipe = null;
    private int mintTime = 0;
    public int getMintTime() { return this.mintTime; }
    public float getMintProgress() { return (float)this.mintTime/(float)this.getExpectedMintTime(); }
    public int getExpectedMintTime() { if(this.lastRelevantRecipe != null) return this.lastRelevantRecipe.getDuration(); return -1; }

    private List<CoinMintRecipe> getCoinMintRecipes()
    {
        if(this.world != null)
            return getCoinMintRecipes(this.world);
        return new ArrayList<>();
    }

    public static List<CoinMintRecipe> getCoinMintRecipes(World level) { return RecipeValidator.getValidMintRecipes(level); }

    public CoinMintBlockEntity(BlockPos pos, BlockState state) { this(ModBlockEntities.COIN_MINT, pos, state); }

    protected CoinMintBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
        this.storage.addListener(this::onInventoryChanged);
    }

    @Override
    public void writeNbt(NbtCompound compound)
    {
        InventoryUtil.saveAllItems("Storage", compound, this.storage);
        compound.putInt("MintTime", this.mintTime);
        super.writeNbt(compound);
    }

    @Override
    public void readNbt(NbtCompound compound)
    {
        super.readNbt(compound);

        if(compound.contains("Storage"))
        {
            this.storage = InventoryUtil.loadAllItems("Storage", compound, 2);
            this.storage.addListener(this::onInventoryChanged);
        }

        if(compound.contains("MintTime"))
            this.mintTime = compound.getInt("MintTime");

    }

    @Override
    public void onLoad() {
        if(this.world == null)
            return;
        if(this.world.isClient)
            BlockEntityUtil.requestUpdatePacket(this);
        this.lastRelevantRecipe = this.getRelevantRecipe();
    }

    private void onInventoryChanged(Inventory inventory)
    {
        if(inventory != this.storage)
            return;
        this.markDirty();
        this.checkRecipes();
    }

    public void checkRecipes() {
        CoinMintRecipe newRecipe = this.getRelevantRecipe();
        if(this.lastRelevantRecipe != newRecipe)
        {
            this.lastRelevantRecipe = newRecipe;
            this.mintTime = 0;
            this.markMintTimeDirty();
        }
    }

    @Override
    public void serverTick() {
        if(this.world == null)
            return;
        if(this.lastRelevantRecipe != null && this.storage.getStack(0).getCount() >= this.lastRelevantRecipe.ingredientCount && this.hasOutputSpace())
        {
            this.mintTime++;
            if(this.mintTime >= this.lastRelevantRecipe.getDuration())
            {
                this.mintTime = 0;
                this.mintCoin();
                this.world.playSound(null, this.pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 0.5f, 1f);
            }
            this.markMintTimeDirty();
        }
        else if(this.mintTime > 0)
        {
            this.mintTime = 0;
            this.markMintTimeDirty();
        }
    }

    private void markMintTimeDirty()
    {
        this.markDirty();
        NbtCompound updateTag = new NbtCompound();
        updateTag.putInt("MintTime", this.mintTime);
        BlockEntityUtil.sendUpdatePacket(this, updateTag);
    }

    public void dumpContents(World world, BlockPos pos) { InventoryUtil.dumpContents(world, pos, this.storage); }

    //Coin Minting Functions
    public boolean validMintInput(ItemStack item)
    {
        Inventory tempInv = new SimpleInventory(2);
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
     * Returns 0 if the mint input does not createTrue the same item currently in the output slot.
     */
    public boolean hasOutputSpace()
    {
        //Determine how many more coins can fit in the output slot based on the input item
        if(this.lastRelevantRecipe == null || this.world == null)
            return false;
        ItemStack mintOutput = this.lastRelevantRecipe.getOutput(this.world.getRegistryManager());
        ItemStack currentOutputSlot = this.getStorage().getStack(1);
        if(currentOutputSlot.isEmpty())
            return true;
        else if(!InventoryUtil.ItemMatches(currentOutputSlot, mintOutput))
            return false;
        return currentOutputSlot.getMaxCount() - currentOutputSlot.getCount() >= this.lastRelevantRecipe.getOutputItem().getCount();
    }

    @Nullable
    public CoinMintRecipe getRelevantRecipe()
    {
        if(this.getStorage().getStack(0).isEmpty())
            return null;
        for(CoinMintRecipe recipe : this.getCoinMintRecipes())
        {
            if(recipe.matches(this.storage, this.world))
                return recipe;
        }
        return null;
    }

    public void mintCoin()
    {
        this.lastRelevantRecipe = this.getRelevantRecipe();
        if(this.lastRelevantRecipe == null || this.world == null)
            return;
        ItemStack mintOutput = this.lastRelevantRecipe.getOutput(this.world.getRegistryManager());
        //Ignore if no valid input is present
        if(mintOutput.isEmpty())
            return;

        //Confirm that the output slot has enough room for the expected outputs
        if(!this.hasOutputSpace())
            return;

        //Confirm that we have the required inputs
        if(this.storage.getStack(0).getCount() < this.lastRelevantRecipe.ingredientCount)
            return;

        //Place the output item(s)
        if(this.getStorage().getStack(1).isEmpty())
        {
            this.getStorage().setStack(1, mintOutput);
        }
        else
        {
            this.getStorage().getStack(1).increment(mintOutput.getCount());
        }

        //Remove the input item(s)
        this.getStorage().removeStack(0, mintOutput.getCount());

        //Job is done!
        this.markDirty();

    }

    @Override
    public NbtCompound toInitialChunkDataNbt() { return this.createNbt(); }

    @Override
    public int[] getAvailableSlots(Direction side) {
        //Insert only from above
        if(side == Direction.UP)
            return new int[]{0};
        //Extract only from below
        else if(side == Direction.DOWN)
            return new int[]{1};
        //Insert and extract from the sides
        return new int[]{0,1};
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) { return slot == 0; }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) { return slot == 1; }

    @Override
    public int size() { return this.storage.size(); }

    @Override
    public boolean isEmpty() { return this.storage.isEmpty(); }

    @Override
    public ItemStack getStack(int slot) { return this.storage.getStack(slot); }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        if(slot != 1)
            return ItemStack.EMPTY;
        return this.storage.removeStack(slot,amount);
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